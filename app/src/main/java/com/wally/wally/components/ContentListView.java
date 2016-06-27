package com.wally.wally.components;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.wally.wally.R;
import com.wally.wally.endlessScroll.ContentPagingRetriever;

/**
 * Created by Meravici on 6/15/2016. yea
 */
public class ContentListView extends FrameLayout implements ContentPagingRetriever.ContentPageRetrieveListener {

    private RecyclerView mRecycler;
    private View mEmptyContentView;
    private OnScrollSettleListener onScrollSettleListener;

    private boolean isFirstLoad = true;

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
        mRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if(newState == RecyclerView.SCROLL_STATE_IDLE && onScrollSettleListener != null){
                    onScrollSettleListener.onScrollSettled();
                }
            }
        });
        mEmptyContentView = findViewById(R.id.empty_view);
    }

    public void setAdapter(RecyclerView.Adapter adapter) {
        mRecycler.setAdapter(adapter);
        isFirstLoad = true;
    }

    public void addOnScrollListener(RecyclerView.OnScrollListener onScrollListener) {
        mRecycler.addOnScrollListener(onScrollListener);
    }

    public void setLayoutManager(RecyclerView.LayoutManager layoutManager){
        mRecycler.setLayoutManager(layoutManager);
    }

    @Override
    public void onNextPageLoad(int pageLength) {
        if(isFirstLoad){
            if(pageLength==0){
                mRecycler.setVisibility(GONE);
                mEmptyContentView.setVisibility(VISIBLE);
            }else{
                mRecycler.setVisibility(VISIBLE);
                mEmptyContentView.setVisibility(GONE);
            }
            isFirstLoad = false;
        }
    }

    public void startLoading(){
        mRecycler.setVisibility(VISIBLE);
        mEmptyContentView.setVisibility(GONE);
    }

    @Override
    public void onNextPageFail() {
        if(isFirstLoad){
            mRecycler.setVisibility(GONE);
            mEmptyContentView.setVisibility(VISIBLE);
            isFirstLoad = false;
        }
    }

    public void setOnScrollSettleListener(OnScrollSettleListener onScrollSettleListener) {
        this.onScrollSettleListener = onScrollSettleListener;
    }

    public interface OnScrollSettleListener{
        void onScrollSettled();
    }
}
