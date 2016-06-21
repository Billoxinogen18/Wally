package com.wally.wally;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

public abstract class EndlessRecyclerOnScrollListener extends RecyclerView.OnScrollListener {
    public static String TAG = EndlessRecyclerOnScrollListener.class.getSimpleName();

    private int numItemsOnPage; // The minimum amount of items to have below your current scroll position before loading more.

    private int previousTotal = 0; // The total number of items in the dataset after the last load
    private boolean loading = true; // True if we are still waiting for the last set of data to load.
    private int visibleThreshold = 5; // The minimum amount of items to have below your current scroll position before loading more.
    int firstVisibleItem, visibleItemCount, totalItemCount, lastVisibleItem;


    public EndlessRecyclerOnScrollListener(int numItemsOnPage) {
        this.numItemsOnPage = numItemsOnPage;
    }

//    @Override
//    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
//        super.onScrolled(recyclerView, dx, dy);
//
//        LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
//
//        int visibleItemCount = recyclerView.getChildCount();
//        int totalItemCount = linearLayoutManager.getItemCount();
//        int firstVisibleItem = linearLayoutManager.findFirstVisibleItemPosition();
//
//        if(!loading) {
//            if (dy > 0 && (totalItemCount - visibleItemCount)
//                    <= (firstVisibleItem + numItemsOnPage)) {
//                Log.d(TAG, "onScrolled: LoadNext");
//                loading = true;
//                onLoadNext();
////            } else if (dy < 0 && lastVisibleItemPosition <= totalItems - numItemsOnPage && firstVisibleItemPosition <= totalItems - numItemsOnPage) {
////                Log.d(TAG, "onScrolled: LoadPrevious");
////                loading = true;
////                onLoadPrevious();
//            }
//        }
//    }

    public void loadingFinished(){
        Log.d(TAG, "Endless: loading set false");
        loading = false;
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

        if (!loading && dy > 0 && (totalItemCount - visibleItemCount)
                <= (firstVisibleItem + visibleThreshold)) {
            // End has been reached

            onLoadNext();

            loading = true;
        }

        if (!loading && dy < 0 && visibleItemCount
                > (lastVisibleItem - visibleThreshold)) {
            // End has been reached

            onLoadPrevious();

            loading = true;
        }
    }
}