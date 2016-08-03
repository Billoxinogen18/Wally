package com.wally.wally.datacontroller.callbacks;

import com.wally.wally.datacontroller.DataController.FetchResultCallback;
import com.wally.wally.datacontroller.content.Content;
import com.wally.wally.datacontroller.content.FirebaseContent;
import com.wally.wally.datacontroller.utils.Predicate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FirebaseFetchResultCallback implements Callback<Collection<FirebaseContent>> {
    private FetchResultCallback callback;
    private Predicate<Content> predicate;

    public FirebaseFetchResultCallback(FetchResultCallback callback) {
        this(callback, new Predicate<Content>() {
            @Override
            public boolean test(Content target) {
                return true;
            }
        });
    }

    public FirebaseFetchResultCallback(FetchResultCallback callback, Predicate<Content> predicate) {
        this.callback = callback;
        this.predicate = predicate;
    }

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

}

