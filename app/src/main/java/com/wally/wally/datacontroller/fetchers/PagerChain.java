package com.wally.wally.datacontroller.fetchers;

import com.wally.wally.datacontroller.callbacks.FetchResultCallback;
import com.wally.wally.datacontroller.content.Content;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class PagerChain implements ContentFetcher {
    private int currentPagerIndex;
    private List<ContentFetcher> pagerList;
    private Collection<Content> head;
    private Collection<Content> tail;

    public PagerChain(List<ContentFetcher> pagerList) {
        this.pagerList = pagerList;
        this.currentPagerIndex = 0;
        head = new ArrayList<>();
        tail = new ArrayList<>();
    }

    public PagerChain() {
        this(new ArrayList<ContentFetcher>());
    }

    public PagerChain addPager(ContentFetcher fetcher) {
        pagerList.add(fetcher);
        return this;
    }

    @Override
    public void fetchPrev(final int count, final FetchResultCallback callback) {
        if (currentPagerIndex == -1) {
            callback.onResult(new ArrayList<>(head));
            head.clear();
            return;
        }

        if (currentPagerIndex == pagerList.size()) {
            currentPagerIndex--;
        }

        pagerList.get(currentPagerIndex).fetchPrev(count, new FetchResultCallback() {
            @Override
            public void onResult(Collection<Content> result) {
                if (result.size() == count) {
                    List<Content> finalResult = new ArrayList<>();
                    finalResult.addAll(result);
                    finalResult.addAll(head);
                    head.clear();
                    callback.onResult(finalResult);
                } else {
                    currentPagerIndex--;
                    head.addAll(result);
                    fetchPrev(count - result.size(), callback);
                }
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }

    @Override
    public void fetchNext(final int count, final FetchResultCallback callback) {
        if (currentPagerIndex == pagerList.size()) {
            callback.onResult(new ArrayList<>(tail));
            tail.clear();
            return;
        }

        if (currentPagerIndex == -1) {
            currentPagerIndex++;
        }

        pagerList.get(currentPagerIndex).fetchNext(count, new FetchResultCallback() {
            @Override
            public void onResult(Collection<Content> result) {
                if (result.size() == count) {
                    List<Content> finalResult = new ArrayList<>();
                    finalResult.addAll(result);
                    finalResult.addAll(tail);
                    tail.clear();
                    callback.onResult(finalResult);
                } else {
                    currentPagerIndex++;
                    tail.addAll(result);
                    fetchNext(count - result.size(), callback);
                }
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });

    }

}