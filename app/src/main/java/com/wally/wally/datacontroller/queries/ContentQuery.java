package com.wally.wally.datacontroller.queries;

import com.wally.wally.datacontroller.DataController.*;
import com.google.firebase.database.DatabaseReference;
import com.wally.wally.datacontroller.callbacks.FirebaseFetchResultCallback;
import com.wally.wally.datacontroller.utils.Predicate;
import com.wally.wally.datacontroller.content.Content;

@Deprecated
public class ContentQuery {
    private final DatabaseReference ref;
    private final FirebaseQuery query;
    private final Predicate<Content> predicate;


    public ContentQuery(FirebaseQuery query,
                        DatabaseReference ref,
                        Predicate<Content> predicate) {
        this.ref = ref;
        this.query = query;
        this.predicate = predicate;
    }


    public ContentQuery(FirebaseQuery query, DatabaseReference ref) {
        this(query, ref, new Predicate<Content>() {
            @Override
            public boolean test(Content target) {
                return true;
            }
        });
    }


    public void fetch(final FetchResultCallback callback) {
        query.fetch(ref, new FirebaseFetchResultCallback(callback, predicate));
    }
}