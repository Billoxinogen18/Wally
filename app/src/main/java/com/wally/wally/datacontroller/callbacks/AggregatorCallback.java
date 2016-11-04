package com.wally.wally.datacontroller.callbacks;

import com.wally.wally.datacontroller.DBController.ResultCallback;
import com.wally.wally.objects.content.Content;

import java.util.Collection;
import java.util.HashSet;

public class AggregatorCallback implements ResultCallback {
    private Collection<Content> aggregatedContent;
    private ResultCallback callback;
    private int nUpdates;

    public AggregatorCallback(ResultCallback callback) {
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
}