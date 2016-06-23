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
    private static final String TAG = ContentPagingRetriever.class.getSimpleName();

    public int pageLength;

    private ContentFetcher contentFetcher;

    private List<Content> previous;
    private List<Content> current;
    private List<Content> next;

    private List<ContentPageRetrieveListener> observers;

    private ContentPagingEnumerator contentPagingEnumerator;

    private Handler handler;

    private boolean hasNext = true;
    private boolean hasPrevious = true;

    private boolean lastNextOne = true;
    private boolean lastNextTwo = true;
    private long lastRequestId = 0;

    public ContentPagingRetriever(ContentFetcher contentFetcher, Handler handler, int pageLength) {
        this.contentFetcher = contentFetcher;
        this.pageLength = pageLength;

        observers = new ArrayList<>();
        previous = new ArrayList<>();
        current = new ArrayList<>();
        next = new ArrayList<>();

        this.handler = handler;

        contentPagingEnumerator = new ContentPagingEnumerator(pageLength);

        fetch();
    }

    public Content get(int i) {
        if(i < size()) {
            if (i < pageLength) {
                return previous.get(i);
            } else if (i < 2 * pageLength) {
                return current.get(i % pageLength);
            } else if (i < 3 * pageLength) {
                return next.get(i % pageLength);
            }
        }
        return null;
    }

    public List<Content> getList() {
        List<Content> res = new ArrayList<>(previous);
        res.addAll(current);
        res.addAll(next);
        return res;
    }

    public int size() {
        return previous.size() + current.size() + next.size();
    }

    public void loadNext() {
        if (hasNext) {
            hasPrevious = true;
            loadNext(pageLength);
        }
    }

    public void loadPrevious() {
        if (hasPrevious) {
            hasNext = true;
            loadPrevious(pageLength);
        }
    }

    public ContentPagingEnumerator getContentPagingEnumerator(){
        return contentPagingEnumerator;
    }

    public void registerLoadListener(ContentPageRetrieveListener contentPageRetrieveListener) {
        observers.add(contentPageRetrieveListener);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Content content : previous) {
            sb.append(content.getTitle()).append(",");
        }

        for (Content content : current) {
            sb.append(content.getTitle()).append(",");
        }

        for (Content content : next) {
            sb.append(content.getTitle()).append(",");
        }

        return sb.toString();
    }


    private void loadNext(final int num) {
        lastRequestId = System.currentTimeMillis();
        contentFetcher.fetchNext(num, new LastRequestCallback(lastRequestId) {
            @Override
            public void onResult(Collection<Content> result) {
                if(this.requestId != lastRequestId){
                    for (ContentPageRetrieveListener observer : observers) {
                        observer.onNextPageFinish();
                    }
                    return;
                }

                if (!lastNextOne) {
                    lastNextOne = true;
                    loadNext(num);
                } else if (!lastNextTwo) {
                    lastNextTwo = true;
                    loadNext(num);
                } else {
                    if (!result.isEmpty()) {
                        contentPagingEnumerator.next(result.size());
                        final List<Content> contents;
                        if (result instanceof List) {
                            contents = (List<Content>) result;
                        } else {
                            contents = new ArrayList<>(result);
                        }

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                previous = current;
                                current = next;
                                next = contents;
                                for (ContentPageRetrieveListener observer : observers) {
                                    observer.onNextPageLoad(contents.size());
                                }

                                Log.d(TAG, "run: " + ContentPagingRetriever.this.toString());
                            }
                        });
                    } else {
                        hasNext = false;
                        for (ContentPageRetrieveListener observer : observers) {
                            observer.onNextPageFail();
                        }
                    }
                }
            }

            @Override
            public void onError(Exception e) {
                if(this.requestId != lastRequestId) {
                    for (ContentPageRetrieveListener observer : observers) {
                        observer.onNextPageFinish();
                    }
                    return;
                }

                hasNext = false;
                for (ContentPageRetrieveListener observer : observers) {
                    observer.onNextPageFail();
                }
            }
        });
    }

    private void loadPrevious(final int num) {
        lastRequestId = System.currentTimeMillis();
        contentFetcher.fetchPrev(num, new LastRequestCallback(lastRequestId) {
            @Override
            public void onResult(Collection<Content> result) {
                if(this.requestId != lastRequestId){
                    for (ContentPageRetrieveListener observer : observers) {
                        observer.onPreviousPageFinish();
                    }
                    return;
                }

                if (lastNextOne) {
                    lastNextOne = false;
                    loadPrevious(num);
                } else if (lastNextTwo) {
                    lastNextTwo = false;
                    loadPrevious(num);
                } else {
                    if (!result.isEmpty()) {
                        contentPagingEnumerator.prev(result.size());
                        final List<Content> contents;
                        if (result instanceof List) {
                            contents = (List<Content>) result;
                        } else {
                            contents = new ArrayList<>(result);
                        }

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                next = current;
                                current = previous;
                                previous = contents;

                                for (ContentPageRetrieveListener observer : observers) {
                                    observer.onPreviousPageLoad(contents.size());
                                }
                            }
                        });
                    } else {
                        hasPrevious = false;
                        for (ContentPageRetrieveListener observer : observers) {
                            observer.onPreviousPageFail();
                        }
                    }
                }
            }

            @Override
            public void onError(Exception e) {
                if(this.requestId != lastRequestId){
                    for (ContentPageRetrieveListener observer : observers) {
                        observer.onPreviousPageFinish();
                    }
                    return;
                }

                hasPrevious = false;
                for (ContentPageRetrieveListener observer : observers) {
                    observer.onPreviousPageFail();
                }
            }
        });
    }

    private void fetch() {
        contentFetcher.fetchNext(pageLength * 3, new FetchResultCallback() {
            @Override
            public void onResult(Collection<Content> result) {
                if (result.size() != 0) {
                    List<Content> contents;
                    if (result instanceof List) {
                        contents = (List<Content>) result;
                    } else {
                        contents = new ArrayList<>(result);
                    }
                    previous.clear();
                    current.clear();
                    next.clear();
                    int i = 0;
                    for (; i < Math.min(contents.size(), pageLength); i++)
                        previous.add(contents.get(i));

                    for (; i < Math.min(contents.size(), pageLength * 2); i++)
                        current.add(contents.get(i));

                    for (; i < Math.min(contents.size(), pageLength * 3); i++)
                        next.add(contents.get(i));

                    for (ContentPageRetrieveListener observer : observers) {
                        observer.onInit();
                    }
                } else {
                    for (ContentPageRetrieveListener observer : observers) {
                        observer.onFail();
                    }
                }
            }

            @Override
            public void onError(Exception e) {
                for (ContentPageRetrieveListener observer : observers) {
                    observer.onFail();
                }
            }
        });
    }


    private abstract class LastRequestCallback implements FetchResultCallback{
        public long requestId;

        public LastRequestCallback(long requestId){
            this.requestId = requestId;
        }

        @Override
        public void onResult(Collection<Content> result) {

        }

        @Override
        public void onError(Exception e) {

        }
    }

    public interface ContentPageRetrieveListener {
        void onInit();

        void onFail();

        void onNextPageLoad(int pageLength);

        void onNextPageFail();

        void onPreviousPageLoad(int pageLength);

        void onPreviousPageFail();

        void onNextPageFinish();

        void onPreviousPageFinish();
    }
}
