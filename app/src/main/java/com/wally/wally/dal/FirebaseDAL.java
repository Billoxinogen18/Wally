package com.wally.wally.dal;

import android.content.Context;
import android.support.annotation.NonNull;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class FirebaseDAL implements DataAccessLayer {
    private Firebase fb;

    public FirebaseDAL(Context context) {
        Firebase.setAndroidContext(context);
        fb = new Firebase("https://burning-inferno-2566.firebaseio.com/").child("Contents");
    }

    @Override
    public void save(@NonNull Content c, @NonNull Callback<Boolean> statusCallback) {
        // TODO
        statusCallback.call(false, new UnsupportedOperationException("Not implemented"));
    }

    @Override
    public void saveEventually(@NonNull Content c) {
        fb.push().setValue(c);
    }

    @Override
    public void delete(@NonNull Content c, @NonNull Callback<Boolean> statusCallback) {
        // TODO
        statusCallback.call(false, new UnsupportedOperationException("Not implemented"));
    }

    @Override
    public void deleteEventually(@NonNull Content c) {
        // TODO
    }

    @Override
    public void fetch(@NonNull Query query, @NonNull final Callback<Collection<Content>> resultCallback) {
        fb.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Set<Content> result = new HashSet<>();
                for (DataSnapshot contentSnapshot: snapshot.getChildren()) {
                    result.add(contentSnapshot.getValue(Content.class));
                }
                resultCallback.call(result, null);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                resultCallback.call(null, firebaseError.toException());
            }

        });
    }

}
