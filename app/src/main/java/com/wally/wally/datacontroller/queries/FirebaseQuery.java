package com.wally.wally.datacontroller.queries;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.wally.wally.datacontroller.callbacks.FirebaseFetchResultCallback;
import com.wally.wally.datacontroller.content.FirebaseContent;

import java.util.HashSet;
import java.util.Set;

public abstract class FirebaseQuery {

    public abstract Query getTarget(Firebase ref);

    public final void fetch(Firebase ref, final FirebaseFetchResultCallback resultCallback) {
        getTarget(ref).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Set<FirebaseContent> result = new HashSet<>();
                for (DataSnapshot contentSnapshot : snapshot.getChildren()) {
                    FirebaseContent content = contentSnapshot.getValue(FirebaseContent.class);
                    content.setId(contentSnapshot.getKey());
                    result.add(content);
                }
                resultCallback.onResult(result);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                resultCallback.onError(firebaseError.toException());
            }

        });
    }

}