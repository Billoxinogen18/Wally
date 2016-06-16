package com.wally.wally;

import com.google.android.gms.maps.model.LatLng;
import com.wally.wally.activities.MapsActivity;
import com.wally.wally.datacontroller.callbacks.FetchResultCallback;
import com.wally.wally.datacontroller.content.Content;
import com.wally.wally.datacontroller.fetchers.ContentFetcher;
import com.wally.wally.userManager.SocialUser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by Meravici on 6/15/2016.
 */
public class ContentPagingRetriever {
    private static final int PAGE_LENGTH = 10;

    private ContentFetcher contentFetcher;

    private List<Content> previous;
    private List<Content> current;
    private List<Content> next;

    private List<ContentPageRetrieveListener> observers;

    public ContentPagingRetriever(ContentFetcher contentFetcher) {

        this.contentFetcher = contentFetcher;

        observers = new ArrayList<>();
        previous = new ArrayList<>();
        current = new ArrayList<>();
        next = new ArrayList<>();
    }

    public Content get(int i) {
        if (i < PAGE_LENGTH) {
            return previous.get(i);
        } else if (i < 2 * PAGE_LENGTH) {
            return current.get(i % 10);
        } else if (i < 3 * PAGE_LENGTH) {
            return next.get(i % 10);
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
        loadNext(PAGE_LENGTH);
    }

    private void loadNext(int num) {
        contentFetcher.fetchNext(num, new FetchResultCallback() {
            @Override
            public void onResult(Collection<Content> result) {
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
                    fireSuccess();
                } else {
                    fireFailure();
                }
            }

            @Override
            public void onError(Exception e) {
                fireFailure();
            }
        });
    }

    public void loadPrevious() {
        contentFetcher.fetchPrev(PAGE_LENGTH, new FetchResultCallback() {
            @Override
            public void onResult(Collection<Content> result) {
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
                    fireSuccess();
                } else {
                    fireFailure();
                }
            }

            @Override
            public void onError(Exception e) {
                fireFailure();
            }
        });
    }

    public void setContentFetcher(ContentFetcher contentFetcher) {
        this.contentFetcher = contentFetcher;
        fetch();
    }

    private void fetch() {
        contentFetcher.fetchNext(PAGE_LENGTH * 3, new FetchResultCallback() {
            @Override
            public void onResult(Collection<Content> result) {
                if (result.size() != 0) {
                    List<Content> contents;
                    if (result instanceof List) {
                        contents = (List<Content>) result;
                    } else {
                        contents = new ArrayList<>(result);
                    }
                    previous.clear(); current.clear(); next.clear();
                    int i = 0;
                    for (; i < Math.min(contents.size(), PAGE_LENGTH); i++)
                        previous.add(contents.get(i));

                    for (; i < Math.min(contents.size(), PAGE_LENGTH * 2); i++)
                        current.add(contents.get(i));

                    for (; i < Math.min(contents.size(), PAGE_LENGTH * 3); i++)
                        next.add(contents.get(i));

                    fireSuccess();
                } else {
                    fireFailure();
                }
            }

            @Override
            public void onError(Exception e) {
                fireFailure();
            }
        });
    }

    public void registerLoadListener(ContentPageRetrieveListener contentPageRetrieveListener) {
        observers.add(contentPageRetrieveListener);
    }

    public void fireSuccess() {
        for (ContentPageRetrieveListener observer : observers) {
            observer.onPageLoaded();
        }
    }

    public void fireFailure() {
        for (ContentPageRetrieveListener observer : observers) {
            observer.onPageFailed();
        }
    }


    public interface ContentPageRetrieveListener {
        void onPageLoaded();

        void onPageFailed();
    }
}
