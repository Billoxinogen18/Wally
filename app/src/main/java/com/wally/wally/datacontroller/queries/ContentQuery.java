package com.wally.wally.datacontroller.queries;

import com.google.firebase.database.DatabaseReference;
import com.wally.wally.datacontroller.DBController.ResultCallback;
import com.wally.wally.datacontroller.callbacks.FirebaseFetchResultCallback;
import com.wally.wally.datacontroller.utils.Predicate;
import com.wally.wally.objects.content.Content;

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


    public void fetch(final ResultCallback callback) {
        query.fetch(ref, new FirebaseFetchResultCallback(callback, predicate));
    }
}