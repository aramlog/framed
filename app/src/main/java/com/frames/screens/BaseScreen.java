package com.frames.screens;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.frames.App;
import com.frames.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.HashMap;
import java.util.Map;

public class BaseScreen extends Activity {

    protected static Map<Integer, String> categories = new HashMap<>();

    protected AdBannerFragment adBannerFragment;
    protected AdInterstitialFragment adInterstitialFragment;

    static {
        categories.put(1, "Adult Humor");
        categories.put(2, "Animals");
        categories.put(3, "Circus");
        categories.put(4, "Dreams");
        categories.put(5, "Fairy Tales");
        categories.put(6, "Holidays");
        categories.put(7, "Monsters And Dinosaurs");
        categories.put(8, "Music");
        categories.put(9, "Robots");
        categories.put(10, "Space");
        categories.put(11, "Sport");
        categories.put(12, "Superheroes");
        categories.put(13, "Trip Down Memory Lane");
        categories.put(14, "Under The Sea");
        categories.put(15, "When I Grow Up");
    }

    protected void googleAnalytics(String screenName) {
        try {
            Tracker tracker = ((App) getApplication()).getTracker();
            tracker.setScreenName(screenName);
            tracker.send(new HitBuilders.AppViewBuilder().build());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class AdBannerFragment extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.ad_banner, container, false);
        }

        @Override
        public void onActivityCreated(Bundle bundle) {
            super.onActivityCreated(bundle);

            AdView mAdView = (AdView) getView().findViewById(R.id.adView);
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
        }
    }

    public static class AdInterstitialFragment extends Fragment {
        private InterstitialAd mInterstitialAd;
        private CountDownTimer mCountDownTimer;
        private boolean timer = true;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.ad_interstitial, container, false);
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            initTimer();
            initAd();
        }

        @Override
        public void onResume() {
            super.onResume();
            if (mCountDownTimer == null) {
                initTimer();
            }
            loadAd();
            mCountDownTimer.start();
        }

        @Override
        public void onPause() {
            if (mCountDownTimer != null) {
                mCountDownTimer.cancel();
            }
            super.onPause();
        }

        private void initAd() {
            mInterstitialAd = new InterstitialAd(getActivity());
            mInterstitialAd.setAdUnitId(getString(R.string.interstitial_ad_unit_id));
        }

        public void loadAd() {
            AdRequest adRequest = new AdRequest.Builder().build();
            if (mInterstitialAd != null) {
                mInterstitialAd.loadAd(adRequest);
            }
        }

        public void displayAd() {
            if (mInterstitialAd != null && mInterstitialAd.isLoaded()) {
                mInterstitialAd.show();
                if (!timer) {
                    mInterstitialAd = null;
                    mCountDownTimer.cancel();
                }
            } else {
                loadAd();
                mCountDownTimer.start();
            }
        }

        private void initTimer() {
            mCountDownTimer = new CountDownTimer(60000, 1000) {
                @Override
                public void onTick(long millisUnitFinished) {
                }

                @Override
                public void onFinish() {
                    if (timer) {
                        displayAd();
                    }
                }
            };
        }

        public boolean isTimer() {
            return timer;
        }

        public void setTimer(boolean timer) {
            this.timer = timer;
        }
    }

    protected void showNoNetworkDialog(final DialogHandler handler) {
        new AlertDialog.Builder(this)
                .setTitle("Connection Error")
                .setMessage("Please check your network connection and try again.")
                .setCancelable(false)
                .setPositiveButton("Try again", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        handler.positive();
                    }
                })
                .setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        handler.negative();
                    }
                }).create().show();
    }

    public static interface DialogHandler {
        public void positive();

        public void negative();
    }

}
