package com.wally.wally;

import android.util.Log;

import com.wally.wally.datacontroller.callbacks.FetchResultCallback;
import com.wally.wally.datacontroller.content.Content;
import com.wally.wally.datacontroller.fetchers.ContentFetcher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Meravici on 6/15/2016.
 */
public class ContentPagingRetriever {
    private static final String TAG = ContentPagingRetriever.class.getSimpleName();

    private int pageLength;

    private ContentFetcher contentFetcher;

    private List<Content> previous;
    private List<Content> current;
    private List<Content> next;

    private List<ContentPageRetrieveListener> observers;

//    private boolean hasNext = true;
//    private boolean hasPrevious = false;

    private boolean lastNextOne = true;
    private boolean lastNextTwo = true;

    public ContentPagingRetriever(ContentFetcher contentFetcher, int pageLenght) {

        this.contentFetcher = contentFetcher;
        this.pageLength = pageLenght;

        observers = new ArrayList<>();
        previous = new ArrayList<>();
        current = new ArrayList<>();
        next = new ArrayList<>();
    }

    public Content get(int i) {
        if (i < pageLength) {
            return previous.get(i);
        } else if (i < 2 * pageLength) {
            return current.get(i % pageLength);
        } else if (i < 3 * pageLength) {
            return next.get(i % pageLength);
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
//        if (hasNext) {
//            hasPrevious = true;
            loadNext(pageLength);
//        }
    }

    private void loadNext(final int num) {
        contentFetcher.fetchNext(num, new FetchResultCallback() {
            @Override
            public void onResult(Collection<Content> result) {
                if (!lastNextOne) {
                    lastNextOne = true;
                    loadNext(num);
                } else if (!lastNextTwo) {
                    lastNextTwo = true;
                    loadNext(num);
                } else {
                    if (result.size() != 0) {
                        List<Content> contents;
                        if (result instanceof List) {
                            contents = (List<Content>) result;
                        } else {
                            contents = new ArrayList<>(result);
                        }

                        previous = current;
                        current = next;
                        next = contents;
                        for (ContentPageRetrieveListener observer : observers) {
                            observer.onNextPageLoaded();
                        }
                        Log.d(TAG, "onResult: next " + ContentPagingRetriever.this.toString());
                    } else {
//                        hasNext = false;
                        for (ContentPageRetrieveListener observer : observers) {
                            observer.onNextPageFailed();
                        }
                    }
                }
            }

            @Override
            public void onError(Exception e) {
//                hasNext = false;
                for (ContentPageRetrieveListener observer : observers) {
                    observer.onNextPageFailed();
                }
            }
        });
    }

    public void loadPrevious() {
//        if (hasPrevious) {
//            hasNext = true;
            loadPrevious(pageLength);
//        }
    }

    public void loadPrevious(final int num) {
        contentFetcher.fetchPrev(num, new FetchResultCallback() {
            @Override
            public void onResult(Collection<Content> result) {
                if (lastNextOne) {
                    lastNextOne = false;
                    loadPrevious(num);
                }else if(lastNextTwo){
                    lastNextTwo = false;
                    loadPrevious(num);
                } else {
                    if (result.size() != 0) {
                        List<Content> contents;
                        if (result instanceof List) {
                            contents = (List<Content>) result;
                        } else {
                            contents = new ArrayList<>(result);
                        }

                        next = current;
                        current = previous;
                        previous = contents;

                        for (ContentPageRetrieveListener observer : observers) {
                            observer.onPreviousPageLoaded();
                        }
                        Log.d(TAG, "onResult: prev " + ContentPagingRetriever.this.toString());
                    } else {
//                        hasPrevious = false;
                        for (ContentPageRetrieveListener observer : observers) {
                            observer.onPreviousPageFailed();
                        }
                    }
                }
            }

            @Override
            public void onError(Exception e) {
//                hasPrevious = false;
                for (ContentPageRetrieveListener observer : observers) {
                    observer.onPreviousPageFailed();
                }
            }
        });
    }

    public void setContentFetcher(ContentFetcher contentFetcher) {
        this.contentFetcher = contentFetcher;
        fetch();
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
                        observer.onNextPageLoaded();
                    }
                } else {
                    for (ContentPageRetrieveListener observer : observers) {
                        observer.onNextPageFailed();
                    }
                }
            }

            @Override
            public void onError(Exception e) {
                for (ContentPageRetrieveListener observer : observers) {
                    observer.onNextPageFailed();
                }
            }
        });
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

    public interface ContentPageRetrieveListener {
        void onNextPageLoaded();

        void onPreviousPageLoaded();

        void onNextPageFailed();

        void onPreviousPageFailed();
    }
}
