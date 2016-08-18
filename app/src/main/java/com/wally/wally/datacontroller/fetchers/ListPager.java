package com.wally.wally.datacontroller.fetchers;

import com.wally.wally.datacontroller.DataController.*;
import com.wally.wally.datacontroller.content.Content;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ListPager implements Fetcher {
    private final List<Content> contents;
    private int nextIndex;

    private ListPager(List<Content> contents) {
        this.contents = contents;
        this.nextIndex = 0;
    }

    public ListPager(Collection<Content> contents) {
        this(new ArrayList<>(contents));
    }

    @Override
    public void fetchNext(int i, FetchResultCallback callback) {
        if (nextIndex >= contents.size()) {
            callback.onResult(Collections.<Content>emptySet());
            return;
        }

        int start = nextIndex;
        int end = Math.min(contents.size(), nextIndex + i);
        nextIndex = end;
        callback.onResult(contents.subList(start, end));
    }
}