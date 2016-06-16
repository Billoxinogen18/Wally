package com.wally.wally.components;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.wally.wally.R;
import com.wally.wally.activities.MapsActivity;

/**
 * Created by Meravici on 6/15/2016.
 */
public class ContentListView extends FrameLayout {

    private RecyclerView mRecycler;
    private View mEmptyContentView;
    private View mLoadingContentView;

    public ContentListView(Context context) {
        super(context);
        init();
    }

    public ContentListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();

    }

    public ContentListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.content_list_view, this);
        mRecycler = (RecyclerView) findViewById(R.id.recyclerview);
        mRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        mEmptyContentView = findViewById(R.id.empty_view);
        mLoadingContentView = findViewById(R.id.loading_view);
    }

    public void setAdapter(RecyclerView.Adapter adapter) {
        mRecycler.setAdapter(adapter);
    }

//    public void startLoading() {
//        mLoadingContentView.setVisibility(View.VISIBLE);
//        mEmptyContentView.setVisibility(View.GONE);
//        mRecycler.setVisibility(View.GONE);
//    }
//
//    public void setLoadingViewVisibility(int visibility) {
//        mLoadingContentView.setVisibility(visibility);
//    }
//
//    public void setListVisibility(int visibility) {
//        mRecycler.setVisibility(visibility);
//    }
//
//    public void setEmptyViewVisibility(int visibility) {
//        mEmptyContentView.setVisibility(visibility);
//    }
}
