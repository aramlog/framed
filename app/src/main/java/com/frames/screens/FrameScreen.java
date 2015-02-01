package com.frames.screens;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.Ragnarok.BitmapFilter;

public class FrameScreen extends BaseScreen implements Camera.ShutterCallback, Camera.PictureCallback {

    // original frame dimensions
    private int FRAME_WIDTH = 640;
    private int FRAME_HEIGHT = 854;

    // will reset runtime
    private int SCREEN_WIDTH = 1080;
    private int SCREEN_HEIGHT = 1760;

    private int GALLERY_PIC_REQUEST = 6000;
    private boolean renderFilters = true;

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
    private Bitmap imageBitmapThumb;

//    private static Map<Integer, String> filters = new HashMap<>();
//
//    static {
//        filters.put(BitmapFilter.GRAY_STYLE, "gray scale");
//        filters.put(BitmapFilter.OIL_STYLE, "painting");
//        filters.put(BitmapFilter.BLOCK_STYLE, "block");
//        filters.put(BitmapFilter.SKETCH_STYLE, "sketch");
//        filters.put(BitmapFilter.OLD_STYLE, "aged");
//        filters.put(BitmapFilter.SHARPEN_STYLE, "sharpen");
//        filters.put(BitmapFilter.SHARPEN_STYLE, "sharpen");
//        filters.put(BitmapFilter.LIGHT_STYLE, "light");
//        filters.put(BitmapFilter.LOMO_STYLE, "lomo");
//        filters.put(BitmapFilter.HDR_STYLE, "hdr");
//    }
//
//    ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_frame);

        SCREEN_WIDTH = AndroidUtils.getScreenWidth(this);
        SCREEN_HEIGHT = AndroidUtils.getScreenHeight(this);

        actionBar = getSupportActionBar();

        actionBar.setHomeButtonEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);

        String frameURL = getIntent().getStringExtra("url");
        actionBar.setTitle(getIntent().getStringExtra("title"));
        actionBar.setSubtitle(getIntent().getStringExtra("subtitle"));

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

        ImageLoader.getInstance().loadImage(frameURL, new ImageLoadingListener() {
            @Override
            public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                frameBitmap = bitmap;
            }

            @Override
            public void onLoadingStarted(String s, View view) {
            }

            @Override
            public void onLoadingFailed(String s, View view, FailReason failReason) {
            }

            @Override
            public void onLoadingCancelled(String s, View view) {
            }
        });
        ImageLoader.getInstance().displayImage(frameURL, frame, AppManager.getInstance().options);

        cameraContainer.setVisibility(View.GONE);
        cameraControllers.setVisibility(View.GONE);
        filtersLayout.setVisibility(View.GONE);
    }

    private void openCamera() {
        cameraMenu.setEnabled(false);
        cameraContainer.setVisibility(View.VISIBLE);
        cameraControllers.setVisibility(View.VISIBLE);
        cameraPreview.startCamera();
        frame.setAlpha(0.5f);
    }

    private void openGallery() {
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

    public void onClickCancelCamera(View v) {
        cameraPreview.releaseCamera();
        cameraContainer.setVisibility(View.GONE);
        cameraControllers.setVisibility(View.GONE);
        cameraMenu.setEnabled(true);
        frame.setAlpha(1.0f);
    }

    public void onClickCloseFilters(View v) {
        filtersLayout.setVisibility(View.GONE);
        effectsMenu.setEnabled(true);
    }

    @Override
    public void onShutter() {
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        camera.stopPreview();
        Matrix imageMatrix = new Matrix();
        imageBitmap = ImageUtils.decodeBitmap(data, SCREEN_WIDTH, SCREEN_HEIGHT);
        imageBitmapThumb = ImageUtils.decodeBitmap(data, 200, 200);
        if (cameraPreview.isFront()) {
            imageBitmap = ImageUtils.flipBitmap(ImageUtils.rotateBitmap(imageBitmap, 270));
            imageBitmapThumb = ImageUtils.flipBitmap(ImageUtils.rotateBitmap(imageBitmapThumb, 270));
        } else {
            imageBitmap = ImageUtils.rotateBitmap(imageBitmap, 90);
            imageBitmapThumb = ImageUtils.rotateBitmap(imageBitmapThumb, 90);
            imageMatrix.postTranslate(-Math.abs(SCREEN_WIDTH - imageBitmap.getWidth()) / 2, 0);
        }

        image.setImageBitmap(imageBitmap);
        image.setImageMatrix(imageMatrix);
        multiTouchImage.reset();
        multiTouchImage.setMatrix(imageMatrix);

        onClickCancelCamera(null);
        showSecondActionButtons();

        renderFilters = true;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == GALLERY_PIC_REQUEST) {
            try {
                Uri imageUri = data.getData();

                imageBitmap = ImageUtils.decodeBitmap(this, imageUri, SCREEN_WIDTH, SCREEN_HEIGHT);
                imageBitmapThumb = ImageUtils.decodeBitmap(this, imageUri, 200, 200);

                cameraContainer.setVisibility(View.GONE);
                cameraControllers.setVisibility(View.GONE);

                frame.setAlpha(1.0f);
                image.setImageBitmap(imageBitmap);
                image.setImageMatrix(new Matrix());
                multiTouchImage.reset();

                onClickCancelCamera(null);
                showSecondActionButtons();

                renderFilters = true;

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void showFirstActionButtons() {
        cameraMenu.setVisible(true);
        galleryMenu.setVisible(true);

        editMenu.setVisible(false);
        shareMenu.setVisible(false);
        effectsMenu.setVisible(false);
        doneMenu.setVisible(false);
        cancelMenu.setVisible(false);
    }

    private void showSecondActionButtons() {
        cameraMenu.setVisible(false);
        galleryMenu.setVisible(false);
        editMenu.setVisible(false);
        shareMenu.setVisible(false);

        effectsMenu.setVisible(true);
        doneMenu.setVisible(true);
        cancelMenu.setVisible(true);
    }

    private void showThirdActionButtons() {
        cameraMenu.setVisible(false);
        galleryMenu.setVisible(false);
        effectsMenu.setVisible(false);
        doneMenu.setVisible(false);
        cancelMenu.setVisible(false);

        editMenu.setVisible(true);
        shareMenu.setVisible(true);
    }

    private void showFilters() {
        if (renderFilters) {
            renderFilters = false;
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
                RelativeLayout itemFilterThumb = (RelativeLayout) getLayoutInflater().inflate(R.layout.item_filter_thumb, filtersContainer, false);
                itemFilterThumb.setLayoutParams(new LinearLayout.LayoutParams(AndroidUtils.dpToPx(70), AndroidUtils.dpToPx(70)));
                final ImageView filterImage = (ImageView) itemFilterThumb.findViewById(R.id.filter_image);

                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        final Bitmap bitmap = applyStyle(filter, imageBitmapThumb);
                        runOnUiThread(new Runnable() {
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
                        imageBitmap = applyStyle(filter, imageBitmap);
                        image.setImageBitmap(imageBitmap);
                    }
                });
                filtersContainer.addView(itemFilterThumb);
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

    private void saveBitmap() {
        try {
            Bitmap bitmap = Bitmap.createBitmap(image.getWidth(), image.getHeight(), Bitmap.Config.ARGB_8888);
            Bitmap watermark = BitmapFactory.decodeResource(getResources(), R.drawable.watermark);
            Canvas c = new Canvas(bitmap);

            Matrix imageMatrix = image.getImageMatrix();
            Matrix frameMatrix = frame.getImageMatrix();
            Matrix watermarkMatrix = new Matrix();
            watermarkMatrix.postTranslate(image.getWidth() - watermark.getWidth(), image.getHeight() - watermark.getHeight());

            c.drawBitmap(imageBitmap, imageMatrix, null);
            c.drawBitmap(frameBitmap, frameMatrix, null);
            c.drawBitmap(watermark, watermarkMatrix, null);

            preview.setImageBitmap(bitmap);
            preview.setVisibility(View.VISIBLE);

            File root = Environment.getExternalStorageDirectory();
            File dir = new File(root.getAbsolutePath() + "/Frames");
            if (!dir.exists()) dir.mkdirs();
            File file = new File(dir, "framed.jpg");
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();

        } catch (Exception e) {

        }
    }

    @Override
    public void onPause() {
        super.onPause();
        cameraPreview.releaseCamera();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_camera:
                openCamera();
                return true;
            case R.id.action_gallery:
                openGallery();
                return true;
            case R.id.action_effects:
                effectsMenu.setEnabled(false);
                showFilters();
                return true;
            case R.id.action_edit:
                showSecondActionButtons();
                onClickCloseFilters(null);
                preview.setImageBitmap(null);
                preview.setVisibility(View.GONE);
                return true;
            case R.id.action_share:
                Toast.makeText(this, "Share", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.action_cancel:
                showFirstActionButtons();
                onClickCloseFilters(null);
                image.setImageBitmap(null);
                return true;
            case R.id.action_done:
                saveBitmap();
                showThirdActionButtons();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        showFirstActionButtons();
        return true;
    }
}
