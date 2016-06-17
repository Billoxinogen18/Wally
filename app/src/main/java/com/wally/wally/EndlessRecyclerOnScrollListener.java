package com.wally.wally;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

public abstract class EndlessRecyclerOnScrollListener extends RecyclerView.OnScrollListener {
    public static String TAG = EndlessRecyclerOnScrollListener.class.getSimpleName();

    private boolean loading; // True if we are still waiting for the last set of data to load.
    private int numItemsOnPage = 5; // The minimum amount of items to have below your current scroll position before loading more.


    private LinearLayoutManager mLinearLayoutManager;

    public EndlessRecyclerOnScrollListener(LinearLayoutManager linearLayoutManager, int numItemsOnPage) {
        this.mLinearLayoutManager = linearLayoutManager;
        this.numItemsOnPage = numItemsOnPage;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        int lastVisibleItemPosition = mLinearLayoutManager.findLastVisibleItemPosition();
        int firstVisibleItemPosition = mLinearLayoutManager.findFirstVisibleItemPosition();
        int totalItems = mLinearLayoutManager.getItemCount();

//        Log.d(TAG, "onScrolled() called with: " + "dx = [" + dx + "], dy = [" + dy + "]");
//        Log.d(TAG, "onScrolled() called: " + "lastVisible = [" + lastVisibleItemPosition +
//                "], firstVisible = [" + firstVisibleItemPosition + "]" +
//                "], total = [" + totalItems + "]");

        if(!loading) {
            if (dy > 0 && lastVisibleItemPosition > totalItems - numItemsOnPage) {
                Log.d(TAG, "onScrolled: LoadNext");
                loading = true;
                onLoadNext();
            } else if (dy < 0 && firstVisibleItemPosition < numItemsOnPage) {
                Log.d(TAG, "onScrolled: LoadPrevious");
                loading = true;
                onLoadPrevious();
            }
        }
    }

    public void loadingFinished(){
        loading = false;
    }

    public abstract void onLoadNext();
    public abstract void onLoadPrevious();
}