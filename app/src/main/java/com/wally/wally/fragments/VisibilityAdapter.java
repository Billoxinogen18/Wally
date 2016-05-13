package com.wally.wally.fragments;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.wally.wally.R;
import com.wally.wally.datacontroller.content.Visibility;

/**
 * Created by GioGoG on 5/9/2016.
 */
public class VisibilityAdapter extends BaseAdapter {


    private Context mContext;

    public VisibilityAdapter(Context context) {
        this.mContext = context;
    }

    @Override
    public int getCount() {
        return Visibility.SocialVisibility.getSize();
    }

    @Override
    public Integer getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        TextView tv;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            tv = (TextView) inflater.inflate(R.layout.support_simple_spinner_dropdown_item, parent, false);
            tv.setCompoundDrawablePadding(mContext.getResources().getDimensionPixelSize(R.dimen.drawable_padding));
        } else {
            tv = (TextView) view;
        }
        tv.setText(Visibility.SocialVisibility.toString(position));
        tv.setCompoundDrawablesWithIntrinsicBounds(Visibility.SocialVisibility.toDrawableRes(position), 0, 0, 0);
        return tv;
    }
}
