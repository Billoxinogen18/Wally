package com.wally.wally.controllers.map.contentList;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

public abstract class OnScrollListener extends RecyclerView.OnScrollListener {
    public static String TAG = OnScrollListener.class.getSimpleName();
    private int firstVisibleItem;
    private int visibleItemCount;
    private int totalItemCount;
    private int lastVisibleItem;
    private int lastFirstVisibleItem = -1;
    private boolean loading = true;
    private int visibleThreshold = 5; // The minimum amount of items to have below your current scroll position before loading more.

    public OnScrollListener(int numItemsOnPage) {
        this.visibleThreshold = numItemsOnPage;
    }


    public void loadingNextFinished() {
        loading = false;
    }

    public abstract void onLoadNext();

    public abstract void onVisibleItemChanged(int previous, int position);

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

        int firstCompletelyVisibleItem = linearLayoutManager.findFirstCompletelyVisibleItemPosition();

        if(firstCompletelyVisibleItem != lastFirstVisibleItem){
            onVisibleItemChanged(lastFirstVisibleItem, firstCompletelyVisibleItem);
            lastFirstVisibleItem = firstCompletelyVisibleItem;
        }


        visibleItemCount = recyclerView.getChildCount();
        totalItemCount = linearLayoutManager.getItemCount();
        firstVisibleItem = linearLayoutManager.findFirstVisibleItemPosition();
        lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
        if (!loading && dy > 0 && (totalItemCount - visibleItemCount)
                <= (firstVisibleItem + visibleThreshold)) {
            // End has been reached
            onLoadNext();

            loading = true;
        }
    }
}