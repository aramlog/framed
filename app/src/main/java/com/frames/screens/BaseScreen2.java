package com.frames.screens;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.frames.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;

import java.util.HashMap;
import java.util.Map;

public class BaseScreen2 extends Activity {

    protected static Map<Integer, String> categories = new HashMap<Integer, String>();

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

    public static class AdBannerFragment extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.layout_ad, container, false);
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

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.layout_ad2, container, false);
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
            mCountDownTimer.start();
        }

        @Override
        public void onPause() {
            if (mCountDownTimer != null) {
                mCountDownTimer.cancel();
            }
            AdRequest adRequest = new AdRequest.Builder().build();
            mInterstitialAd.loadAd(adRequest);
            super.onPause();
        }

        private void initAd() {
            mInterstitialAd = new InterstitialAd(getActivity());
            mInterstitialAd.setAdUnitId(getString(R.string.interstitial_ad_unit_id));
        }

        private void displayAd() {
            if (mInterstitialAd != null && mInterstitialAd.isLoaded()) {
                mInterstitialAd.show();
            } else {
                AdRequest adRequest = new AdRequest.Builder().build();
                mInterstitialAd.loadAd(adRequest);
                mCountDownTimer.start();
            }
        }

        private void initTimer() {
            mCountDownTimer = new CountDownTimer(10000, 1000) {
                @Override
                public void onTick(long millisUnitFinished) {
                }

                @Override
                public void onFinish() {
                    displayAd();
                }
            };
        }
    }
}
