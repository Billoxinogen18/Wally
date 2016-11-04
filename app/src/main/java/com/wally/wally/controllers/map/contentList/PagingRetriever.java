package com.wally.wally.controllers.map.contentList;

import android.os.Handler;
import android.util.Log;

import com.wally.wally.datacontroller.DataController.*;
import com.wally.wally.objects.content.Content;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Meravici on 6/15/2016. yea
 */
public class PagingRetriever {
    private static final String TAG = PagingRetriever.class.getSimpleName();

    private int pageLength;

    private Fetcher contentFetcher;

    private List<Content> current;

    private List<ContentPageRetrieveListener> observers;

    private Handler handler;

    private boolean hasNext = true;

    public PagingRetriever(Fetcher contentFetcher, Handler handler, int pageLength) {
        this.contentFetcher = contentFetcher;
        this.pageLength = pageLength;

        observers = new ArrayList<>();
        current = new ArrayList<>();

        this.handler = handler;

        loadNext(pageLength * 3);
    }

    public Content get(int i) {
        return current.get(i);
    }

    public List<Content> getList() {
        return new ArrayList<>(current);
    }

    public int size() {
        return current.size();
    }

    public void loadNext() {
        if (hasNext) {
            loadNext(pageLength);
        }
    }

    public void registerLoadListener(ContentPageRetrieveListener contentPageRetrieveListener) {
        observers.add(contentPageRetrieveListener);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Content content : current) {
            sb.append(content.getTitle()).append(",");
        }

        return sb.toString();
    }


    private void loadNext(final int num) {
        contentFetcher.fetchNext(num, new FetchResultCallback() {
            @Override
            public void onResult(final Collection<Content> result) {
                Log.d(TAG, "onResult() called with: " + "result = [" + result + "]");
                if (!result.isEmpty()) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            current.addAll(result);
                            for (ContentPageRetrieveListener observer : observers) {
                                observer.onNextPageLoad(result.size());
                                if (result.size() < num) {
                                    observer.onNextPageFail();
                                }
                            }
                        }
                    });

                    if (result.size() < num) {
                        hasNext = false;
                    }
                } else {
                    hasNext = false;
                    for (ContentPageRetrieveListener observer : observers) {
                        observer.onNextPageFail();
                    }
                }
            }
        });
    }

    public interface ContentPageRetrieveListener {
        void onNextPageLoad(int pageLength);

        void onNextPageFail();
    }
}
