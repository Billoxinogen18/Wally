package com.wally.wally.fragments;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.wally.wally.R;
import com.wally.wally.datacontroller.content.Visibility;

public class VisibilityAdapter extends BaseAdapter {

    private static final String TAG = VisibilityAdapter.class.getSimpleName();
    private Context mContext;
    private View.OnClickListener mListener;

    public VisibilityAdapter(Context context, View.OnClickListener listener) {
        this.mContext = context;
        mListener = listener;
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
        final TextView tv;
        ImageView iv;
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.spinner_dropdown_item, parent, false);
        tv = (TextView) view.findViewById(R.id.text);
        iv = (ImageView) view.findViewById(R.id.image);


        tv.setTag(position);
        tv.setText(Visibility.SocialVisibility.getStringRepresentation(position));
        Log.wtf(TAG, "getView: " + iv);
        iv.setImageResource(Visibility.SocialVisibility.toDrawableRes(position));

        if (Visibility.SocialVisibility.PEOPLE == position) {
            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = (Integer) tv.getTag();
                    if (Visibility.SocialVisibility.PEOPLE == pos) {
                        mListener.onClick(v);
                    }
                }
            });
        }
        return tv;
    }
}
