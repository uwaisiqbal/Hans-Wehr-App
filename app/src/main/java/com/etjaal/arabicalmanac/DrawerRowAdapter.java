package com.etjaal.arabicalmanac;

import android.content.ContentValues;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Uwais on 09/01/2017.
 */
public class DrawerRowAdapter extends ArrayAdapter<String> {

    private String[] titles;
    private int[] icons;
    private Context context;

    public DrawerRowAdapter(Context context, String[] titles,int[] icons){
        super(context,-1,titles);
        this.context = context;
        this.titles = titles;
        this.icons = icons;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.drawer_item_row,parent,false);
        TextView textView = (TextView) rowView.findViewById(R.id.rowText);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.rowIcon);
        textView.setText(titles[position]);
        imageView.setImageResource(icons[position]);
        return rowView;
    }

}


