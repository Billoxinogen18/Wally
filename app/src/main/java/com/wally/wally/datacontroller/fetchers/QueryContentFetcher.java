package com.wally.wally.datacontroller.fetchers;

import com.wally.wally.datacontroller.DataController.*;
import com.wally.wally.datacontroller.content.Content;
import com.wally.wally.datacontroller.queries.ContentQuery;

import java.util.Collection;

public class QueryContentFetcher implements Fetcher {
    private final ContentQuery query;
    private ListPager fetcher;

    public QueryContentFetcher(ContentQuery query) {
        this.query = query;
    }

    @Override
    public void fetchNext(final int i, final FetchResultCallback callback) {
        if (fetcher == null) {
            query.fetch(new FetchResultCallback() {
                @Override
                public void onResult(Collection<Content> result) {
                    fetcher = new ListPager(result);
                    fetcher.fetchNext(i, callback);
                }

                @Override
                public void onError(Exception e) {
                    callback.onError(e);
                }
            });
        } else {
            fetcher.fetchNext(i, callback);
        }
    }
}
