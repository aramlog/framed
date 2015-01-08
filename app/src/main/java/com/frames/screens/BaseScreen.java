package com.frames.screens;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.frames.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.HashMap;
import java.util.Map;

public class BaseScreen extends ActionBarActivity {

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

    public static class AdFragment extends Fragment {
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
}
