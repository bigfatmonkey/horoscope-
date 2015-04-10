package com.horoscope.view;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.horoscope.R;
import com.squareup.picasso.Picasso;

public class CustomList extends ArrayAdapter<String> {
    private final Activity context;
    private final String[] web;
    private final Integer[] imageId;

    public CustomList(Activity context,
                      String[] web, Integer[] imageId) {
        super(context, R.layout.list_single, web);
        this.context = context;
        this.web = web;
        this.imageId = imageId;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder holder;
        View rowView = view;
        if (rowView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            rowView = inflater.inflate(R.layout.list_single, null, true);
            holder = new ViewHolder();
            holder.textView = (TextView) rowView.findViewById(R.id.txt);
            holder.imageView = (ImageView) rowView.findViewById(R.id.img);
            rowView.setTag(holder);
        } else {
            holder = (ViewHolder) rowView.getTag();
        }

        holder.textView.setText(web[position]);

        Picasso.with(context).load("https://encrypted-tbn2.gstatic.com/images?q=tbn:ANd9GcQY2iPnsDd8w3AnGO8RAn2GVry0MGaOr2FYkx0R2CqqMF0M2JBSWaEfkCFS").into(holder.imageView);
      //  .setImageResource(imageId[position]);
        return rowView;
    }

    static class ViewHolder {
        public ImageView imageView;
        public TextView textView;
    }
}