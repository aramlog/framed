package com.frames.screens;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.frames.App;
import com.frames.R;
import com.frames.adapters.FrameAdapter;
import com.frames.adapters.MenuItemsAdapter;
import com.frames.items.FrameItem;
import com.frames.managers.ApiManager;
import com.frames.utils.AndroidUtils;
import com.frames.utils.widgets.HeaderGridView;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

import java.util.ArrayList;
import java.util.List;

public class HomeScreen extends BaseScreen {

    private MenuItemsAdapter menuItemsAdapter;
    private RelativeLayout menuLayout;
    private RelativeLayout landingPageLayout;
    private ListView menuItemsList;
    private TextView menuTitle;

    private FrameAdapter frameAdapter;
    private HeaderGridView framesGrid;

    private List<FrameItem> frames = new ArrayList<>();
    private int offset = 15;

    private boolean loaded = false;
    private boolean timeIsUp = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_home);

        googleAnalytics("HomeScreen");

        adBannerFragment = (AdBannerFragment) getFragmentManager().findFragmentById(R.id.adFragmentBanner);
        adInterstitialFragment = (AdInterstitialFragment) getFragmentManager().findFragmentById(R.id.adFragmentInterstitial);

        landingPageLayout = (RelativeLayout) findViewById(R.id.landing_page);
        menuLayout = (RelativeLayout) findViewById(R.id.menu);
        menuTitle = (TextView) findViewById(R.id.menu_title);

        //thumb 199x266
        int columnWidth = (AndroidUtils.getScreenWidth(this) - AndroidUtils.dpToPx(offset) * 3) / 2;
        int columnHeight = (columnWidth * 266) / 199;

        RelativeLayout framesGridHeader = new RelativeLayout(this);
        framesGridHeader.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, AndroidUtils.dpToPx(offset)));

        framesGrid = (HeaderGridView) findViewById(R.id.frames_grid);
        framesGrid.setColumnWidth(columnWidth);
        framesGrid.addHeaderView(framesGridHeader);
        frameAdapter = new FrameAdapter(this, columnWidth, columnHeight);

        ImageLoader.getInstance().init(new ImageLoaderConfiguration.Builder(this)
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .denyCacheImageMultipleSizesInMemory()
                .diskCacheFileNameGenerator(new Md5FileNameGenerator())
                .diskCacheSize(50 * 1024 * 1024)
                .tasksProcessingOrder(QueueProcessingType.FIFO)
                .writeDebugLogs()
                .build());

        loadFrames();

        menuItemsAdapter = new MenuItemsAdapter(this, getResources().getStringArray(R.array.categories));
        menuItemsList = (ListView) findViewById(R.id.menu_items_list);
        menuItemsList.setAdapter(menuItemsAdapter);

        menuItemsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                menuItemsAdapter.setSelectedItem(i);
                menuItemsAdapter.notifyDataSetChanged();
                menuTitle.setText(menuItemsAdapter.getItem(i));
                filterFrames(menuItemsAdapter.getItem(i));
                closeMenu();
            }
        });

        framesGrid.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int scrollState) {
                frameAdapter.allowLoading = false;
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                    frameAdapter.allowLoading = true;
                    frameAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i2, int i3) {

            }
        });

        AndroidUtils.changeChildrenFont(this, (ViewGroup) findViewById(android.R.id.content));
    }

    private void loadFrames() {
        new CountDownTimer(3000, 1000) {
            @Override
            public void onTick(long millisUnitFinished) {
            }

            @Override
            public void onFinish() {
                timeIsUp = true;
                if (loaded) {
                    renderFrames();
                }
            }
        }.start();

        new Thread() {
            @Override
            public void run() {
                try {
                    frames = ApiManager.getInstance().getAllFrames();
                    HomeScreen.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (frames == null) {
                                showNoNetworkDialog(new DialogHandler() {
                                    @Override
                                    public void positive() {
                                        loadFrames();
                                    }

                                    @Override
                                    public void negative() {
                                        finish();
                                    }
                                });
                                return;
                            }
                            loaded = true;
                            if (timeIsUp) {
                                renderFrames();
                            }
                        }
                    });
                } catch (Exception e) {
                    Log.e("Error occurred while requesting frames", e.getMessage());
                }
            }
        }.start();
    }

    private void renderFrames() {
        frameAdapter.setItems(frames);
        framesGrid.setAdapter(frameAdapter);
        landingPageLayout.setVisibility(View.GONE);
    }

    private void filterFrames(String category) {
        if (category.equals("All Frames")) {
            frameAdapter.setItems(frames);
        } else {
            List<FrameItem> filtered = new ArrayList<>();
            for (FrameItem frameItem : frames) {
                if (frameItem.getCategory().equals(category)) {
                    filtered.add(frameItem);
                }
            }
            frameAdapter.setItems(filtered);
        }

        framesGrid.smoothScrollToPosition(0);
    }

    public void onClickMenu(View v) {
        if (menuItemsList.getVisibility() == View.GONE) {
            openMenu();
        } else {
            closeMenu();
        }
    }

    private void openMenu() {
        menuLayout.setBackgroundColor(getResources().getColor(R.color.menu_bg_selected));
        menuItemsList.setVisibility(View.VISIBLE);
    }

    private void closeMenu() {
        menuLayout.setBackgroundColor(getResources().getColor(R.color.menu_bg));
        menuItemsList.setVisibility(View.GONE);
    }
}
