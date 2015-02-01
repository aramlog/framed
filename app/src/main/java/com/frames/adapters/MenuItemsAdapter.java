package com.frames.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.frames.R;
import com.frames.utils.AndroidUtils;


public class MenuItemsAdapter extends BaseAdapter {

    private LayoutInflater mInflater;

    private Context mContext;
    private String[] items;

    private int selectedItem;

    public MenuItemsAdapter(Context context, String[] items) {
        this.mContext = context;
        this.items = items;

        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    @Override
    public int getCount() {
        return items.length;
    }

    public String getItem(int position) {
        return items[position];
    }

    public long getItemId(int position) {
        return position;
    }


    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_category, null);
            convertView.setLayoutParams(new ListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, AndroidUtils.dpToPx(60)));
        }

        TextView title = (TextView) convertView.findViewById(R.id.title);

        if (position == selectedItem) {
            title.setTextColor(mContext.getResources().getColor(R.color.menu_title_selected));
        } else {
            title.setTextColor(mContext.getResources().getColor(R.color.menu_title));
        }

        title.setText(items[position]);
        return convertView;
    }

    public void setSelectedItem(int position) {
        selectedItem = position;
    }

}
