package com.frames.screens;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.frames.R;
import com.frames.managers.AppManager;
import com.frames.utils.AndroidUtils;
import com.frames.utils.ImageUtils;
import com.frames.utils.widgets.CameraPreview;
import com.frames.utils.widgets.MultiTouchImage;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

import java.io.FileNotFoundException;
import java.io.IOException;

public class FrameScreen extends BaseScreen implements Camera.ShutterCallback, Camera.PictureCallback {

    private int MAX_HEIGHT = 1080;
    private int MAX_WIDTH = 1080;

    private int CAMERA_PIC_REQUEST = 5000;
    private int GALLERY_PIC_REQUEST = 6000;

    private ImageView image;
    private ImageView frame;

    private MenuItem cameraMenu;
    private MenuItem galleryMenu;

    private Button rotateCameraBtn;
    private Button snapshotBtn;

    private CameraPreview cameraPreview;
    private FrameLayout cameraContainer;
    private RelativeLayout buttonsContainer;

    private MultiTouchImage multiTouchImage = new MultiTouchImage();

    private Matrix matrixHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_frame);

        MAX_WIDTH = AndroidUtils.getScreenWidth(this);
        MAX_HEIGHT = AndroidUtils.getScreenHeight(this);

        ActionBar actionBar = getSupportActionBar();
        //actionBar.setDisplayHomeAsUpEnabled(true);

        ImageLoader.getInstance().init(new ImageLoaderConfiguration.Builder(this)
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .denyCacheImageMultipleSizesInMemory()
                .diskCacheFileNameGenerator(new Md5FileNameGenerator())
                .diskCacheSize(50 * 1024 * 1024)
                .tasksProcessingOrder(QueueProcessingType.FIFO)
                .writeDebugLogs()
                .build());

        String frameURL = getIntent().getStringExtra("url");

        actionBar.setTitle(getIntent().getStringExtra("title"));

        image = (ImageView) findViewById(R.id.image);
        frame = (ImageView) findViewById(R.id.frame);

        rotateCameraBtn = (Button) findViewById(R.id.rotate_camera_btn);
        snapshotBtn = (Button) findViewById(R.id.snapshot_btn);

        cameraPreview = new CameraPreview(this);
        buttonsContainer = (RelativeLayout) findViewById(R.id.buttons_container);
        cameraContainer = ((FrameLayout) findViewById(R.id.camera_container));
        cameraContainer.addView(cameraPreview);

        image.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return multiTouchImage.onTouchHandler(v, event);
            }
        });

        rotateCameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cameraPreview.switchCamera();
                if(cameraPreview.isFront()) {
                    rotateCameraBtn.setText("Back Camera");
                } else {
                    rotateCameraBtn.setText("Front Camera");
                }
            }
        });

        snapshotBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cameraPreview.getCamera().takePicture(FrameScreen.this, null, null, FrameScreen.this);
            }
        });

        ImageLoader.getInstance().loadImage(frameURL,null);
        ImageLoader.getInstance().displayImage(frameURL, frame, AppManager.getInstance().options);

        cameraContainer.setVisibility(View.GONE);
        buttonsContainer.setVisibility(View.GONE);

        matrixHolder = image.getMatrix();
    }

    @Override
    public void onPause() {
        super.onPause();
        cameraPreview.releaseCamera();
    }

    @Override
    public void onShutter() {
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        Bitmap bitmap = ImageUtils.decodeBitmap(data, MAX_WIDTH, MAX_HEIGHT);

        if (cameraPreview.isFront()) {
            image.setImageBitmap(ImageUtils.flipBitmap(ImageUtils.rotateBitmap(bitmap, 270)));
        } else {
            image.setImageBitmap(ImageUtils.rotateBitmap(bitmap, 90));
        }

        image.setImageMatrix(matrixHolder);
        multiTouchImage.reset();

        //camera.startPreview();
        cameraPreview.releaseCamera();
        cameraContainer.setVisibility(View.GONE);
        buttonsContainer.setVisibility(View.GONE);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == CAMERA_PIC_REQUEST) {
                Uri selectedImageUri = data.getData();
                loadExternalImage(selectedImageUri);
            } else if (requestCode == GALLERY_PIC_REQUEST) {
                Uri selectedImageUri = data.getData();
                loadExternalImage(selectedImageUri);
            }
        }
    }

    private void loadExternalImage(Uri imageUri) {
        try {
            Bitmap bitmap = ImageUtils.decodeBitmap(this, imageUri, MAX_WIDTH, MAX_HEIGHT);
            image.setImageBitmap(bitmap);
            image.setImageMatrix(matrixHolder);
            multiTouchImage.reset();

            cameraContainer.setVisibility(View.GONE);
            buttonsContainer.setVisibility(View.GONE);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void openCamera() {
        cameraContainer.setVisibility(View.VISIBLE);
        buttonsContainer.setVisibility(View.VISIBLE);

        cameraPreview.getCamera().startPreview();
//        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
//        startActivityForResult(cameraIntent, CAMERA_PIC_REQUEST);
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), GALLERY_PIC_REQUEST);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);

        cameraMenu = menu.findItem(R.id.action_camera);
        galleryMenu = menu.findItem(R.id.action_gallery);

        cameraMenu.setVisible(true);
        galleryMenu.setVisible(true);

        return super.onCreateOptionsMenu(menu);
    }

}
