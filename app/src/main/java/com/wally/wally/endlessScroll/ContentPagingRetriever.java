package com.wally.wally.endlessScroll;

import android.os.Handler;
import android.util.Log;

import com.wally.wally.datacontroller.callbacks.FetchResultCallback;
import com.wally.wally.datacontroller.content.Content;
import com.wally.wally.datacontroller.fetchers.ContentFetcher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Meravici on 6/15/2016. yea
 */
public class ContentPagingRetriever {
    public static final String TAG = ContentPagingRetriever.class.getSimpleName();

    public int pageLength;

    private ContentFetcher contentFetcher;

    private List<Content> current;

    private List<ContentPageRetrieveListener> observers;

    private Handler handler;

    private boolean hasNext = true;

    public ContentPagingRetriever(ContentFetcher contentFetcher, Handler handler, int pageLength) {
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

            @Override
            public void onError(Exception e) {
                hasNext = false;
                for (ContentPageRetrieveListener observer : observers) {
                    observer.onNextPageFail();
                }
            }
        });
    }

//    private void fetch() {
//        contentFetcher.fetchNext(pageLength * 3, new FetchResultCallback() {
//            @Override
//            public void onResult(Collection<Content> result) {
//                if (result.size() != 0) {
//                    current.clear();
//
//                    current.addAll(result);
//
//                    for (ContentPageRetrieveListener observer : observers) {
//                        observer.onNextPageLoad(result.size());
//                    }
//                } else {
//                    for (ContentPageRetrieveListener observer : observers) {
//                        observer.onNextPageFail();
//                    }
//                }
//            }
//
//            @Override
//            public void onError(Exception e) {
//                for (ContentPageRetrieveListener observer : observers) {
//                    observer.onNextPageFail();
//                }
//            }
//        });
//    }

    public interface ContentPageRetrieveListener {
        void onNextPageLoad(int pageLength);

        void onNextPageFail();
    }
}
