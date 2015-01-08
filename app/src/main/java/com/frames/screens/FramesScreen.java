package com.frames.screens;

import android.os.Bundle;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.frames.R;
import com.frames.adapters.FrameAdapter;
import com.frames.items.FrameItem;
import com.frames.managers.ApiManager;
import com.frames.utils.AndroidUtils;
import com.frames.utils.widgets.HeaderGridView;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

import java.util.List;

public class FramesScreen extends BaseScreen {

    private FrameAdapter frameAdapter;
    private HeaderGridView framesGrid;

    private int offset = 15;
    private int categoryId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_frames);

        ImageLoader.getInstance().init(new ImageLoaderConfiguration.Builder(this)
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .denyCacheImageMultipleSizesInMemory()
                .diskCacheFileNameGenerator(new Md5FileNameGenerator())
                .diskCacheSize(50 * 1024 * 1024)
                .tasksProcessingOrder(QueueProcessingType.FIFO)
                .writeDebugLogs()
                .build());

        categoryId = getIntent().getIntExtra("category_id", 1);

        ((TextView) findViewById(R.id.header).findViewById(R.id.title)).setText(categories.get(categoryId));

        //thumb 199x266
        int columnWidth = (AndroidUtils.getScreenWidth(this) - AndroidUtils.dpToPx(offset) * 3) / 2;
        int columnHeight = (columnWidth * 266) / 199;

        RelativeLayout framesGridHeader = new RelativeLayout(this);
        framesGridHeader.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, AndroidUtils.dpToPx(offset)));

        framesGrid = (HeaderGridView) findViewById(R.id.frames_grid);
        framesGrid.setColumnWidth(columnWidth);
        framesGrid.addHeaderView(framesGridHeader);
        frameAdapter = new FrameAdapter(this, columnWidth, columnHeight);

        loadFrames();
    }

    private void loadFrames() {
        new Thread() {
            @Override
            public void run() {
                try {
                    final List<FrameItem> frames = ApiManager.getInstance().getFrames(categoryId);
                    FramesScreen.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            frameAdapter.setItems(frames);
                            framesGrid.setAdapter(frameAdapter);
                        }
                    });
                } catch (Exception e) {
                    Log.e("Error occurred while requesting frames", e.getMessage());
                }
            }
        }.start();
    }
}
