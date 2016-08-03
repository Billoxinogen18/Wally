package com.wally.wally.datacontroller.callbacks;

import com.wally.wally.datacontroller.DataController.FetchResultCallback;
import com.wally.wally.datacontroller.content.Content;

import java.util.Collection;
import java.util.HashSet;

public class AggregatorCallback implements FetchResultCallback {
    private Collection<Content> aggregatedContent;
    private FetchResultCallback callback;
    private int nUpdates;

    public AggregatorCallback(FetchResultCallback callback) {
        this.callback = callback;
        aggregatedContent = new HashSet<>();
    }

    public AggregatorCallback withExpectedCallbacks(int nExpected) {
        this.nUpdates = nExpected;
        return this;
    }

    private synchronized boolean update(Collection<Content> result) {
        aggregatedContent.addAll(result);
        return --nUpdates == 0;
    }

    @Override
    public void onResult(Collection<Content> result) {
        if (update(result)) {
            callback.onResult(aggregatedContent);
        }
    }

    @Override
    public void onError(Exception e) {
        callback.onError(e);
    }
}