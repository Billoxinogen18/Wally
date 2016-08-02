package com.wally.wally.datacontroller.fetchers;

import com.wally.wally.datacontroller.callbacks.FetchResultCallback;

public interface  ContentFetcher {

    void fetchNext(int i, FetchResultCallback callback);

}