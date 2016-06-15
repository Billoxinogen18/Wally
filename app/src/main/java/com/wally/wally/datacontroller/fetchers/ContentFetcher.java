package com.wally.wally.datacontroller.fetchers;

import com.wally.wally.datacontroller.callbacks.FetchResultCallback;

public interface  ContentFetcher {
    void fetchPrev(int i, FetchResultCallback callback);

    void fetchNext(int i, FetchResultCallback callback);
}
