package com.wally.wally.datacontroller.queries;

import com.google.firebase.database.DatabaseReference;
import com.wally.wally.datacontroller.utils.Predicate;
import com.wally.wally.datacontroller.callbacks.Callback;
import com.wally.wally.datacontroller.callbacks.FetchResultCallback;
import com.wally.wally.datacontroller.content.Content;
import com.wally.wally.datacontroller.content.FirebaseContent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
        this(query, ref, null);
    }


    public void fetch(final FetchResultCallback callback) {
        query.fetch(ref, new Callback<Collection<FirebaseContent>>() {
            @Override
            public void onResult(Collection<FirebaseContent> fetched) {
                List<Content> result = new ArrayList<>();
                for (FirebaseContent c : fetched) {
                    Content content = c.toContent();
                    if (predicate.test(content)) {
                        result.add(content);
                    }
                }
                callback.onResult(result);
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }
}