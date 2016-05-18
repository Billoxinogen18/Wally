package com.wally.wally.datacontroller.callbacks;

import com.wally.wally.datacontroller.content.Content;
import com.wally.wally.datacontroller.content.FirebaseContent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FirebaseFetchResultCallback implements Callback<Collection<FirebaseContent>> {
    public Callback<Collection<Content>> callback;

    public FirebaseFetchResultCallback(Callback<Collection<Content>> callback) {
        this.callback = callback;
    }

    @Override
    public void onResult(Collection<FirebaseContent> result) {
        List<Content> contents = new ArrayList<>();
        for (FirebaseContent c : result) {
            contents.add(c.toContent());
        }
        callback.onResult(contents);
    }

    @Override
    public void onError(Exception e) {
        callback.onError(e);
    }

}