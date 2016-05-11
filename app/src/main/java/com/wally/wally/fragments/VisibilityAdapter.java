package com.wally.wally.fragments;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.wally.wally.R;
import com.wally.wally.datacontroller.content.Visibility;

/**
 * Created by Xato on 5/9/2016.
 */
public class VisibilityAdapter extends BaseAdapter {
    private static final int[] images = {
            R.drawable.ic_private_visibility_black_24dp,
            R.drawable.ic_friends_visibility_black_24dp,
            R.drawable.ic_public_visibility_24dp,
            R.drawable.ic_custom_visibility_black_24dp,
            R.drawable.ic_anonymous_visibility_black_24dp
    };

    private static final String[] titles = {
            "Private",
            "Friends",
            "Public",
            "Custom",
            "Anonymous"
    };

    private static final int[] values = {
            Visibility.SocialVisibility.PRIVATE,
            Visibility.SocialVisibility.FRIENDS,
            Visibility.SocialVisibility.PUBLIC,
            Visibility.SocialVisibility.CUSTOM,
            Visibility.SocialVisibility.ANONYMOUS
    };

    private Context mContext;

    public VisibilityAdapter(Context context){
        this.mContext = context;
    }

    @Override
    public int getCount() {
        return images.length;
    }

    @Override
    public Object getItem(int position) {
        return values[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView tv;
        if(convertView == null){
            tv = new TextView(mContext);
        }else{
            tv = (TextView) convertView;
        }

        tv.setText(titles[position]);
        tv.setCompoundDrawablesWithIntrinsicBounds(images[position], 0, 0, 0);

        return tv;
    }
}
