package com.wally.wally.datacontroller.callbacks;

import android.util.Log;

import com.wally.wally.datacontroller.DBController.ResultCallback;
import com.wally.wally.objects.content.Content;
import com.wally.wally.datacontroller.content.FirebaseContent;
import com.wally.wally.datacontroller.utils.Predicate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FirebaseFetchResultCallback {
    private static final String TAG = "FbFetchResultCallback";
    private ResultCallback callback;
    private Predicate<Content> predicate;

    public FirebaseFetchResultCallback(ResultCallback callback) {
        this(callback, new Predicate<Content>() {
            @Override
            public boolean test(Content target) {
                return true;
            }
        });
    }

    public FirebaseFetchResultCallback(ResultCallback callback, Predicate<Content> predicate) {
        this.callback = callback;
        this.predicate = predicate;
    }

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


    public void onError(Exception e) {
        Log.d(TAG, e.toString());
    }

}

