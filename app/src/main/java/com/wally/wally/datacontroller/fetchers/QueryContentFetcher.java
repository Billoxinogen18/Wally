package com.wally.wally.datacontroller.fetchers;

import com.wally.wally.datacontroller.DBController.Fetcher;
import com.wally.wally.datacontroller.DBController.ResultCallback;
import com.wally.wally.datacontroller.queries.ContentQuery;
import com.wally.wally.objects.content.Content;

import java.util.Collection;

public class QueryContentFetcher implements Fetcher {
    private final ContentQuery query;
    private ListPager fetcher;

    public QueryContentFetcher(ContentQuery query) {
        this.query = query;
    }

    @Override
    public void fetchNext(final int i, final ResultCallback callback) {
        if (fetcher == null) {
            query.fetch(new ResultCallback() {
                @Override
                public void onResult(Collection<Content> result) {
                    fetcher = new ListPager(result);
                    fetcher.fetchNext(i, callback);
                }
            });
        } else {
            fetcher.fetchNext(i, callback);
        }
    }
}
