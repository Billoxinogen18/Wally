package com.wally.wally.datacontroller.fetchers;

import com.wally.wally.datacontroller.DataController.*;
import com.wally.wally.datacontroller.content.Content;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PagerChain implements Fetcher {
    private int currentPagerIndex;
    private List<Fetcher> pagerList;
    private Collection<Content> tail;

    private PagerChain(List<Fetcher> pagerList) {
        this.pagerList = pagerList;
        this.currentPagerIndex = 0;
        tail = new ArrayList<>();
    }

    public PagerChain() {
        this(new ArrayList<Fetcher>());
    }

    public PagerChain addPager(Fetcher fetcher) {
        pagerList.add(fetcher);
        return this;
    }

    @Override
    public void fetchNext(final int count, final FetchResultCallback callback) {
        if (currentPagerIndex == pagerList.size()) {
            ArrayList<Content> finalResult = new ArrayList<>(tail);
            tail.clear();
            callback.onResult(finalResult);
            return;
        }

        if (currentPagerIndex == -1) {
            currentPagerIndex++;
        }

        pagerList.get(currentPagerIndex).fetchNext(count, new FetchResultCallback() {
            @Override
            public void onResult(Collection<Content> result) {
                tail.addAll(result);
                if (tail.size() == count) {
                    List<Content> finalResult = new ArrayList<>(tail);
                    tail.clear();
                    callback.onResult(finalResult);
                } else {
                    currentPagerIndex++;
                    fetchNext(count, callback);
                }
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }

}