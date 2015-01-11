package com.frames.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.frames.R;
import com.frames.items.FrameItem;
import com.frames.managers.AppManager;
import com.frames.screens.FrameScreen;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.util.ArrayList;
import java.util.List;

public class FrameAdapter extends BaseAdapter {
    private List<FrameItem> items = new ArrayList<FrameItem>();
    private LayoutInflater mInflater;
    private Context mContext;
    private Resources mResources;

    private int columnWidth;
    private int columnHeight;

    public FrameAdapter(Context context, int columnWidth, int columnHeight) {
        this(context);
        this.columnWidth = columnWidth;
        this.columnHeight = columnHeight;
    }

    public FrameAdapter(Context context) {
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContext = context;
        mResources = context.getResources();
    }

    public void setItems(List<FrameItem> movies) {
        if (movies == null)
            movies = new ArrayList<FrameItem>();
        items = movies;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return items.size();
    }

    public FrameItem getItem(int position) {
        return items.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        final FrameItem frame = getItem(position);

        final ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();

            convertView = mInflater.inflate(R.layout.item_frame, null);
            convertView.setLayoutParams(new GridView.LayoutParams(columnWidth, columnHeight));

            holder.title = (TextView) convertView.findViewById(R.id.title);
            holder.image = (ImageView) convertView.findViewById(R.id.image);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.image.setImageBitmap(null);
        if (!frame.isLoaded) {
            holder.image.setBackgroundResource(R.drawable.loader_borders);
            ImageLoader.getInstance().loadImage(frame.getThumb(), new ImageLoadingListener() {
                @Override
                public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                    frame.isLoaded = true;
                    ImageLoader.getInstance().displayImage(frame.getThumb(), holder.image, AppManager.getInstance().options);
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
        } else {
            ImageLoader.getInstance().displayImage(frame.getThumb(), holder.image, AppManager.getInstance().options);
        }

        holder.title.setText(frame.getTitle());

        holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, FrameScreen.class);
                intent.putExtra("url", frame.getImage());
                intent.putExtra("title", frame.getTitle());
                mContext.startActivity(intent);
            }
        });

        return convertView;
    }

    private class ViewHolder {
        public ImageView image;
        public TextView title;
    }
}
