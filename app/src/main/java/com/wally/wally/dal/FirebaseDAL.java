package com.wally.wally.dal;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.wally.wally.dal.content.Location;


import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class FirebaseDAL implements DataAccessLayer<Content> {
    private Firebase fb;

    public FirebaseDAL(Context context, String dbAddr) {
        Firebase.setAndroidContext(context);
        fb = new Firebase(dbAddr).child("Contents");
    }

    @Override
    public void save(@NonNull final Content c, @NonNull final Callback<Boolean> statusCallback) {
        fb.push().setValue(c, new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                if (firebaseError == null) {
                    c.setId(firebase.getKey());
                    statusCallback.call(true, null);
                } else {
                    statusCallback.call(false, firebaseError.toException());
                }
            }
        });
    }

    @Override
    public void save(@NonNull Content c) {
        fb.push().setValue(c);
    }

    @Override
    public void delete(@NonNull Content c, @NonNull final Callback<Boolean> statusCallback) {
        fb.child(c.getId()).removeValue(new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                if (firebaseError == null) {
                    statusCallback.call(true, null);
                } else {
                    statusCallback.call(false, firebaseError.toException());
                }
            }
        });

    }

    @Override
    public void delete(@NonNull Content c) {
        fb.child(c.getId()).removeValue();
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
