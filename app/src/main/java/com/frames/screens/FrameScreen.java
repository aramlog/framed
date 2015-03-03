package com.frames.screens;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.frames.R;
import com.frames.managers.AppManager;
import com.frames.utils.AndroidUtils;
import com.frames.utils.ImageUtils;
import com.frames.utils.widgets.CameraPreview;
import com.frames.utils.widgets.MultiTouchImage;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cn.Ragnarok.BitmapFilter;

public class FrameScreen extends BaseScreen implements Camera.ShutterCallback, Camera.PictureCallback {

    // original frame dimensions 640x854

    private int FRAME_WIDTH = 640;
    private int FRAME_HEIGHT = 854;

    // will reset runtime
    private int SCREEN_WIDTH = 1080;
    private int SCREEN_HEIGHT = 1760;

    private int GALLERY_PIC_REQUEST = 6000;

    private ImageView image;
    private ImageView frame;
    private ImageView preview;

    private CameraPreview cameraPreview;
    private FrameLayout cameraContainer;
    private RelativeLayout cameraControllers;

    private RelativeLayout filtersLayout;
    private LinearLayout filtersContainer;

    private MultiTouchImage multiTouchImage = new MultiTouchImage();

    private Bitmap frameBitmap;
    private Bitmap imageBitmap;
    private Bitmap filteredBitmap;
    private Bitmap imageBitmapThumb;
    private Bitmap savedBitmap;

    private int currentScreen = 1;

    private RelativeLayout screen1;
    private RelativeLayout screen2;
    private RelativeLayout screen3;

    private String frameURL;
    private File savedFile;
    private ProgressDialog progressDialog;

    private boolean loadFilters = true;

    private String saveImageName = "image_" + System.currentTimeMillis() + ".jpg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_frame);

        googleAnalytics("FrameScreen");

        adBannerFragment = (AdBannerFragment) getFragmentManager().findFragmentById(R.id.adFragmentBanner);
        adInterstitialFragment = (AdInterstitialFragment) getFragmentManager().findFragmentById(R.id.adFragmentInterstitial);
        adInterstitialFragment.setTimer(false);

        SCREEN_WIDTH = AndroidUtils.getScreenWidth(this);
        SCREEN_HEIGHT = AndroidUtils.getScreenHeight(this);

        frameURL = getIntent().getStringExtra("url");

        screen1 = (RelativeLayout) findViewById(R.id.screen_1);
        screen2 = (RelativeLayout) findViewById(R.id.screen_2);
        screen3 = (RelativeLayout) findViewById(R.id.screen_3);

        image = (ImageView) findViewById(R.id.image);
        frame = (ImageView) findViewById(R.id.frame);
        preview = (ImageView) findViewById(R.id.preview);

        cameraPreview = new CameraPreview(this);
        cameraControllers = (RelativeLayout) findViewById(R.id.camera_controllers);
        cameraContainer = ((FrameLayout) findViewById(R.id.camera_container));
        cameraContainer.addView(cameraPreview);

        filtersLayout = (RelativeLayout) findViewById(R.id.filters);
        filtersContainer = (LinearLayout) findViewById(R.id.filters_container);

        image.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return multiTouchImage.onTouchHandler(v, event);
            }
        });

        progressDialog = ProgressDialog.show(this, "Please wait ...", "Loading Frame ...", true);

        ImageLoader.getInstance().loadImage(frameURL, imageLoadingListener);
        ImageLoader.getInstance().displayImage(frameURL, frame, AppManager.getInstance().options);

        openScreen1();
    }

    private ImageLoadingListener imageLoadingListener = new ImageLoadingListener() {
        @Override
        public void onLoadingComplete(String s, View view, Bitmap bitmap) {
            frameBitmap = bitmap;
            progressDialog.dismiss();
        }

        @Override
        public void onLoadingFailed(String s, View view, FailReason failReason) {
            progressDialog.dismiss();
            showNoNetworkDialog(new DialogHandler() {
                @Override
                public void positive() {
                    ImageLoader.getInstance().loadImage(frameURL, imageLoadingListener);
                }

                @Override
                public void negative() {
                    finish();
                    android.os.Process.killProcess(android.os.Process.myPid());
                }
            });
        }

        @Override
        public void onLoadingCancelled(String s, View view) {
            progressDialog.dismiss();
        }

        @Override
        public void onLoadingStarted(String s, View view) {
        }
    };

    private void openScreen1() {
        screen1.setVisibility(View.VISIBLE);
        screen2.setVisibility(View.GONE);
        screen3.setVisibility(View.GONE);

        frame.setAlpha(0.5f);
        currentScreen = 1;
        openCamera();
    }

    private void openScreen2() {
        screen1.setVisibility(View.GONE);
        screen2.setVisibility(View.VISIBLE);
        screen3.setVisibility(View.GONE);

        frame.setAlpha(1.0f);
        currentScreen = 2;
        cancelCamera();
        showFilters();
    }

    private void openScreen3() {
        screen1.setVisibility(View.GONE);
        screen2.setVisibility(View.GONE);
        screen3.setVisibility(View.VISIBLE);

        currentScreen = 3;
    }

    private void openCamera() {
        cameraContainer.setVisibility(View.VISIBLE);
        cameraControllers.setVisibility(View.VISIBLE);
        cameraPreview.startCamera();
    }

    public void onClickEdit(View v) {
        openScreen2();
    }

    public void onClickOpenGallery(View v) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), GALLERY_PIC_REQUEST);
    }

    public void onClickRotateCamera(View v) {
        cameraPreview.switchCamera();
    }

    public void onClickCapture(View v) {
        cameraPreview.getCamera().takePicture(FrameScreen.this, null, null, FrameScreen.this);
    }

    public void onClickDeleteFilters(View v) {
        filteredBitmap = null;
        image.setImageBitmap(imageBitmap);
    }

    public void onClickOk(View v) {
        progressDialog = ProgressDialog.show(FrameScreen.this, "Please wait ...", "Saving Image ...", true);
        new Thread() {
            @Override
            public void run() {
                try {
                    int imageHeight = image.getHeight();
                    int imageWidth = image.getWidth();

                    savedBitmap = Bitmap.createBitmap(imageWidth, imageHeight, Bitmap.Config.ARGB_8888);
                    Bitmap google = BitmapFactory.decodeResource(getResources(), R.drawable.google);
                    Bitmap logo = BitmapFactory.decodeResource(getResources(), R.drawable.logo_small);

                    int logoHeight = logo.getHeight();
                    int logoWidth = logo.getWidth();

                    int googleHeight = google.getHeight();
                    int googleWidth = google.getWidth();

                    int margin = AndroidUtils.dpToPx(3);

                    Canvas canvas = new Canvas(savedBitmap);
                    canvas.drawColor(Color.WHITE);

                    Matrix imageMatrix = image.getImageMatrix();
                    Matrix frameMatrix = frame.getImageMatrix();

                    canvas.drawBitmap(filteredBitmap == null ? imageBitmap : filteredBitmap, imageMatrix, null);
                    canvas.drawBitmap(frameBitmap, frameMatrix, null);

                    Paint paint = new Paint();
                    paint.setColor(getResources().getColor(R.color.filter_bg));
                    canvas.drawRect(0, imageHeight, imageWidth, imageHeight - logoHeight - 2 * margin, paint);

                    paint.setColor(getResources().getColor(R.color.header_title));
                    paint.setTextSize(logoHeight - 2 * margin);
                    canvas.drawText(getString(R.string.app_name), logoWidth + 2 * margin, imageHeight - 2 * margin, paint);

                    Matrix googleMatrix = new Matrix();
                    googleMatrix.postTranslate(imageWidth - googleWidth - margin, imageHeight - googleHeight - margin);
                    canvas.drawBitmap(google, googleMatrix, null);

                    Matrix logoMatrix = new Matrix();
                    logoMatrix.postTranslate(margin, imageHeight - logoHeight - margin);
                    canvas.drawBitmap(logo, logoMatrix, null);

                    File root = Environment.getExternalStorageDirectory();
                    File dir = new File(root.getAbsolutePath() + "/Kids Frame Cam");
                    if (!dir.exists()) dir.mkdirs();
                    savedFile = new File(dir, saveImageName);
                    FileOutputStream out = new FileOutputStream(savedFile);
                    savedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                    out.flush();
                    out.close();

                } catch (Exception e) {

                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        preview.setImageBitmap(savedBitmap);
                        preview.setVisibility(View.VISIBLE);
                        progressDialog.dismiss();
                        savedBitmap = null;
                        adInterstitialFragment.displayAd();
                        openScreen3();
                        Toast.makeText(FrameScreen.this, "Image has been successfully saved in gallery.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }.start();
    }


    public void onClickHome(View v) {
        finish();
    }

    public void onClickShare(View v) {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("image/*");
        share.putExtra(Intent.EXTRA_TEXT, getString(R.string.app_name));
        share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(savedFile));
        startActivity(Intent.createChooser(share, "Share via"));
    }

    public void cancelCamera() {
        cameraPreview.releaseCamera();
        cameraContainer.setVisibility(View.GONE);
        cameraControllers.setVisibility(View.GONE);
    }

    @Override
    public void onShutter() {
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        camera.stopPreview();
        Matrix imageMatrix = new Matrix();
        imageBitmap = ImageUtils.decodeBitmap(data, FRAME_WIDTH, FRAME_HEIGHT);
        imageBitmapThumb = ImageUtils.decodeBitmap(data, AndroidUtils.dpToPx(73), AndroidUtils.dpToPx(73));
        if (cameraPreview.isFront()) {
            imageBitmap = ImageUtils.flipBitmap(ImageUtils.rotateBitmap(imageBitmap, 270));
            imageBitmapThumb = ImageUtils.flipBitmap(ImageUtils.rotateBitmap(imageBitmapThumb, 270));
        } else {
            imageBitmap = ImageUtils.rotateBitmap(imageBitmap, 90);
            imageBitmapThumb = ImageUtils.rotateBitmap(imageBitmapThumb, 90);

            float scale = (float)SCREEN_HEIGHT/imageBitmap.getHeight();
            imageMatrix.postTranslate(-Math.abs(SCREEN_WIDTH - scale * imageBitmap.getWidth()) / 2, 0);
            imageMatrix.postScale(scale, scale);
        }

        image.setImageBitmap(imageBitmap);
        image.setImageMatrix(imageMatrix);
        multiTouchImage.reset();
        multiTouchImage.setMatrix(imageMatrix);

        filteredBitmap = null;
        loadFilters = true;
        openScreen2();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == GALLERY_PIC_REQUEST) {
            try {
                Uri imageUri = data.getData();

                imageBitmap = ImageUtils.decodeBitmap(this, imageUri, FRAME_WIDTH, FRAME_HEIGHT);
                imageBitmapThumb = ImageUtils.decodeBitmap(this, imageUri, AndroidUtils.dpToPx(73), AndroidUtils.dpToPx(73));

                cameraContainer.setVisibility(View.GONE);
                cameraControllers.setVisibility(View.GONE);

                image.setImageBitmap(imageBitmap);
                image.setImageMatrix(new Matrix());
                multiTouchImage.reset();

                filteredBitmap = null;
                loadFilters = true;
                openScreen2();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void showFilters() {
        if (loadFilters) {
            loadFilters = false;
            filtersContainer.removeAllViews();

            List<Integer> filters = new ArrayList<>();
            filters.add(BitmapFilter.OLD_STYLE);
            filters.add(BitmapFilter.GRAY_STYLE);
            filters.add(BitmapFilter.BLOCK_STYLE);
            filters.add(BitmapFilter.SKETCH_STYLE);
            filters.add(BitmapFilter.OIL_STYLE);
            filters.add(BitmapFilter.SHARPEN_STYLE);
            filters.add(BitmapFilter.SHARPEN_STYLE);
            filters.add(BitmapFilter.LIGHT_STYLE);
            filters.add(BitmapFilter.LOMO_STYLE);
            filters.add(BitmapFilter.HDR_STYLE);

            for (final Integer filter : filters) {
                final ImageView filterImage = new ImageView(this);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(AndroidUtils.dpToPx(73), AndroidUtils.dpToPx(46));
                params.setMargins(0, 0, AndroidUtils.dpToPx(7), 0);
                filterImage.setLayoutParams(params);

                new Thread() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            final Bitmap bitmap = applyStyle(filter, imageBitmapThumb);

                            @Override
                            public void run() {
                                filterImage.setImageBitmap(bitmap);
                            }
                        });
                    }
                }.start();

                filterImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
                filterImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        progressDialog = ProgressDialog.show(FrameScreen.this, "Please wait ...", "Applying Effect ...", true);
                        new Thread() {
                            @Override
                            public void run() {
                                filteredBitmap = applyStyle(filter, imageBitmap);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        image.setImageBitmap(filteredBitmap);
                                        progressDialog.dismiss();
                                    }
                                });
                            }
                        }.start();

                    }
                });
                filtersContainer.addView(filterImage);
            }
            filtersLayout.setVisibility(View.VISIBLE);
        }
    }

    private Bitmap applyStyle(int styleNo, Bitmap originalBitmap) {
        Bitmap changeBitmap;
        switch (styleNo) {
            case BitmapFilter.AVERAGE_BLUR_STYLE:
                changeBitmap = BitmapFilter.changeStyle(originalBitmap, BitmapFilter.AVERAGE_BLUR_STYLE, 5); // maskSize, must odd
                break;
            case BitmapFilter.GAUSSIAN_BLUR_STYLE:
                changeBitmap = BitmapFilter.changeStyle(originalBitmap, BitmapFilter.GAUSSIAN_BLUR_STYLE, 1.2); // sigma
                break;
            case BitmapFilter.SOFT_GLOW_STYLE:
                changeBitmap = BitmapFilter.changeStyle(originalBitmap, BitmapFilter.SOFT_GLOW_STYLE, 0.6);
                break;
            case BitmapFilter.LIGHT_STYLE:
                int width = originalBitmap.getWidth();
                int height = originalBitmap.getHeight();
                changeBitmap = BitmapFilter.changeStyle(originalBitmap, BitmapFilter.LIGHT_STYLE, width / 3, height / 2, width / 2);
                break;
            case BitmapFilter.LOMO_STYLE:
                changeBitmap = BitmapFilter.changeStyle(originalBitmap, BitmapFilter.LOMO_STYLE, (originalBitmap.getWidth() / 2.0) * 95 / 100.0);
                break;
            case BitmapFilter.NEON_STYLE:
                changeBitmap = BitmapFilter.changeStyle(originalBitmap, BitmapFilter.NEON_STYLE, 200, 100, 50);
                break;
            case BitmapFilter.PIXELATE_STYLE:
                changeBitmap = BitmapFilter.changeStyle(originalBitmap, BitmapFilter.PIXELATE_STYLE, 10);
                break;
            case BitmapFilter.MOTION_BLUR_STYLE:
                changeBitmap = BitmapFilter.changeStyle(originalBitmap, BitmapFilter.MOTION_BLUR_STYLE, 10, 1);
                break;
            case BitmapFilter.OIL_STYLE:
                changeBitmap = BitmapFilter.changeStyle(originalBitmap, BitmapFilter.OIL_STYLE, 5);
                break;
            default:
                changeBitmap = BitmapFilter.changeStyle(originalBitmap, styleNo);
                break;
        }
        return changeBitmap;
    }

    @Override
    public void onPause() {
        super.onPause();
        cameraPreview.releaseCamera();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            switch (currentScreen) {
                case 1:
                    finish();
                    return true;
                case 2:
                    openScreen1();
                    return false;
                case 3:
                    openScreen2();
                    return false;
            }
        }
        return false;
    }
}
