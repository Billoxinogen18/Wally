package com.wally.wally.datacontroller.fetchers;

import com.wally.wally.datacontroller.callbacks.FetchResultCallback;
import com.wally.wally.datacontroller.content.Content;

import java.util.ArrayList;
import java.util.Collection;
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
            ArrayList<Content> finalResult = new ArrayList<>(head);
            head.clear();
            callback.onResult(finalResult);
            return;
        }

        if (currentPagerIndex == pagerList.size()) {
            currentPagerIndex--;
        }

        pagerList.get(currentPagerIndex).fetchPrev(count - head.size(), new FetchResultCallback() {
            @Override
            public void onResult(Collection<Content> result) {
                head.addAll(result);
                if (head.size() == count) {
                    List<Content> finalResult = new ArrayList<>(head);
                    head.clear();
                    callback.onResult(finalResult);
                } else {
                    currentPagerIndex--;
                    fetchPrev(count, callback);
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