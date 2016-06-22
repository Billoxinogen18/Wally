package com.wally.wally.endlessScroll;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

public abstract class EndlessRecyclerOnScrollListener extends RecyclerView.OnScrollListener {
    public static String TAG = EndlessRecyclerOnScrollListener.class.getSimpleName();

    private boolean loadingNext = true;
    private boolean loadingPrevious = true;// True if we are still waiting for the last set of data to load.
    private int visibleThreshold = 5; // The minimum amount of items to have below your current scroll position before loading more.
    int firstVisibleItem, visibleItemCount, totalItemCount, lastVisibleItem;


    public EndlessRecyclerOnScrollListener(int numItemsOnPage) {
        this.visibleThreshold = numItemsOnPage;
    }


    public void loadingNextFinished() {
        loadingNext = false;
    }
    public void loadingPreviousFinished() {
        loadingPrevious = false;
    }

    public abstract void onLoadNext();

    public abstract void onLoadPrevious();


    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

        visibleItemCount = recyclerView.getChildCount();
        totalItemCount = linearLayoutManager.getItemCount();
        firstVisibleItem = linearLayoutManager.findFirstVisibleItemPosition();
        lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
        if (!loadingNext && dy > 0 && (totalItemCount - visibleItemCount)
                <= (firstVisibleItem + visibleThreshold)) {
            // End has been reached
            onLoadNext();

            loadingNext = true;
        }

        if (!loadingPrevious && dy < 0 && visibleItemCount > (lastVisibleItem - visibleThreshold)) {
            // End has been reached
            onLoadPrevious();

            loadingPrevious = true;
        }
    }
}