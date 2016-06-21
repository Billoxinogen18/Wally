package com.wally.wally.endlessScroll;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.wally.wally.components.ContentListViewItem;
import com.wally.wally.datacontroller.content.Content;

/**
 * Created by Xato on 6/20/2016.
 */
public class MainListItemViewHolder extends RecyclerView.ViewHolder {
    private Context mContext;
    public ContentListViewItem contentListViewItem;

    public MainListItemViewHolder(View itemView) {
        super(itemView);
        contentListViewItem = (ContentListViewItem) itemView;
    }

    public MainListItemViewHolder(Context context, View itemView) {
        this(itemView);

        mContext = context;
    }
}
