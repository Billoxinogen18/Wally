package com.wally.wally.datacontroller.fetchers;


import com.wally.wally.datacontroller.callbacks.FetchResultCallback;
import com.wally.wally.datacontroller.content.Content;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ListPager implements ContentFetcher {
    private final List<Content> contents;
    private int nextIndex;
    private int prevIndex;

    public ListPager(List<Content> contents) {
        this.contents = contents;
        this.nextIndex = 0;
        this.prevIndex = -1;
    }

    public ListPager(Collection<Content> contents) {
        this(new ArrayList<>(contents));
    }

    @Override
    public void fetchPrev(int i, FetchResultCallback callback) {
        if (prevIndex < 0) {
            callback.onResult(Collections.<Content>emptySet());
            return;
        }

        int start = Math.max(0, prevIndex - i);
        int end = Math.min(prevIndex, contents.size());
        nextIndex = prevIndex;
        prevIndex = start;
        callback.onResult(contents.subList(start, end));
    }

    @Override
    public void fetchNext(int i, FetchResultCallback callback) {
        if (nextIndex >= contents.size()) {
            callback.onResult(Collections.<Content>emptySet());
            return;
        }

        int start = nextIndex;
        int end = Math.min(contents.size(), nextIndex + i);
        prevIndex = nextIndex;
        nextIndex = end;
        callback.onResult(contents.subList(start, end));
    }
}