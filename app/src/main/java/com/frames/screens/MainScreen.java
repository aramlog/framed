package com.frames.screens;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.frames.R;

public class MainScreen extends BaseScreen {

    private ListView categoryListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_main);

        ActionBar actionBar = getSupportActionBar();

        categoryListView = (ListView) findViewById(R.id.category_list);
        categoryListView.setAdapter(new ArrayAdapter<String>(this, R.layout.item_category, R.id.title, getResources().getStringArray(R.array.categories)));

        categoryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(MainScreen.this, FramesScreen.class);
                intent.putExtra("category_id",i+1);
                startActivity(intent);
            }
        });
    }
}
