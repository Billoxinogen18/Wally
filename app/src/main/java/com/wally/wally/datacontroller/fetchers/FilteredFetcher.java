package com.wally.wally.datacontroller.fetchers;

import com.wally.wally.datacontroller.DataController.Fetcher;
import com.wally.wally.datacontroller.DataController.FetchResultCallback;
import com.wally.wally.datacontroller.utils.Predicate;
import com.wally.wally.datacontroller.content.Content;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class FilteredFetcher implements Fetcher {
    private List<Content> nextContents;
    private final Fetcher fetcher;
    private final Predicate<Content> predicate;

    public FilteredFetcher(Fetcher fetcher, Predicate<Content> predicate) {
        this.fetcher = fetcher;
        this.predicate = predicate;
        nextContents = new ArrayList<>();
    }

    @Override
    public void fetchNext(final int count, final FetchResultCallback callback) {
        if (nextContents == null) {
            callback.onResult(Collections.<Content>emptySet());
            return;
        }

        if (nextContents.size() >= count) {
            callback.onResult(chopNextContentsList(count));
            return;
        }

        fetcher.fetchNext(count, new FetchResultCallback() {
            @Override
            public void onResult(Collection<Content> result) {
                List<Content> contents = filteredResult(result);
                nextContents.addAll(contents);
                if (result.size() < count) {
                    List<Content> tail = new ArrayList<>(nextContents);
                    nextContents.clear();
                    nextContents = null;
                    callback.onResult(tail);
                } else {
                    fetchNext(count, callback);
                }
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }

    private List<Content> filteredResult(Collection<Content> result) {
        ArrayList<Content> contents = new ArrayList<>();
        for (Content c : result) {
            if (predicate.test(c)) {
                contents.add(c);
            }
        }
        return contents;
    }

    private List<Content> chopNextContentsList(int count) {
        List<Content> chopped = nextContents.subList(0, count);
        nextContents = nextContents.subList(count, nextContents.size());
        return chopped;
    }
}