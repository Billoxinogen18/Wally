package com.wally.wally.datacontroller.fetchers;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.wally.wally.datacontroller.callbacks.Callback;
import com.wally.wally.datacontroller.callbacks.FetchResultCallback;
import com.wally.wally.datacontroller.callbacks.FirebaseFetchResultCallback;
import com.wally.wally.datacontroller.content.Content;
import com.wally.wally.datacontroller.queries.FirebaseQuery;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class KeyPager implements ContentFetcher {

    private final DatabaseReference contents;

    private boolean hasNext;
    private String nextKey, prevKey;
    private String startKey, endKey;

    public KeyPager(DatabaseReference contents) {
        this(contents, null, null);
    }

    public KeyPager(DatabaseReference contents, String startKey, String endKey) {
        this.contents = contents;
        hasNext = true;
        prevKey = "";
        nextKey = "";
        this.startKey = startKey;
        this.endKey = endKey;
        if (startKey != null) {
            nextKey = startKey;
        }
    }

    @Override
    public void fetchPrev(final int count, final FetchResultCallback callback) {

        if (prevKey.equals("")) {
            callback.onResult(Collections.<Content>emptySet());
            return;
        }

        new FirebaseQuery() {
            @Override
            public Query getTarget(DatabaseReference ref) {
                Query target = ref.orderByKey().limitToLast(count + 1).endAt(prevKey);
                return startKey == null ? target : target.startAt(startKey);
            }
        }.fetch(contents, new FirebaseFetchResultCallback(
                new Callback<Collection<Content>>() {
                    @Override
                    public void onResult(Collection<Content> result) {
                        List<Content> contents = new ArrayList<>();
                        contents.addAll(result);
                        hasNext = true;
                        if (result.size() < 2) {
                            prevKey = "";
                            contents.clear();
                        } else {
                            nextKey = prevKey;
                            prevKey = contents.get(0).getId();
                            contents.remove(contents.size() - 1);
                        }
                        callback.onResult(contents);
                    }

                    @Override
                    public void onError(Exception e) {
                        callback.onError(e);
                    }
                })
        );

    }

    /**
     * Warning: external synchronization is needed!
     * @param count number of following entries
     * @param callback that should be called with result
     */
    @Override
    public void fetchNext(final int count, final FetchResultCallback callback) {

        if (!hasNext) {
            callback.onResult(Collections.<Content>emptySet());
            return;
        }

        prevKey = nextKey;

        new FirebaseQuery() {
            @Override
            public Query getTarget(DatabaseReference ref) {
                Query target = ref.orderByKey().limitToFirst(count + 1).startAt(nextKey);
                return endKey == null ? target : target.endAt(endKey);
            }
        }.fetch(contents, new FirebaseFetchResultCallback(
                new Callback<Collection<Content>>() {
                    @Override
                    public void onResult(Collection<Content> result) {
                        List<Content> contents = new ArrayList<>();
                        contents.addAll(result);
                        if (result.size() < count) {
                            hasNext = false;
                        } else {
                            nextKey = contents.remove(contents.size() - 1).getId();
                        }
                        callback.onResult(contents);
                    }

                    @Override
                    public void onError(Exception e) {
                        callback.onError(e);
                    }
                })
        );
    }


}