package com.wally.wally.endlessScroll;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.wally.wally.R;
import com.wally.wally.components.ContentListViewItem;
import com.wally.wally.datacontroller.content.Content;

/**
 * Created by Meravici on 6/20/2016.
 */
public class MainListItemViewHolder extends RecyclerView.ViewHolder {
    public ContentListViewItem contentListViewItem;

    public MainListItemViewHolder(View itemView) {
        super(itemView);
        contentListViewItem = (ContentListViewItem) itemView.findViewById(R.id.card);
    }
}
