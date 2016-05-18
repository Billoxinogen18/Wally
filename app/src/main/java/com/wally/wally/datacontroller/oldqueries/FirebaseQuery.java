package com.wally.wally.datacontroller.oldqueries;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.wally.wally.datacontroller.Callback;
import com.wally.wally.datacontroller.content.FirebaseContent;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Deprecated
public abstract class FirebaseQuery {

    public abstract Query getTarget(Firebase ref);

    public final void fetch(Firebase ref, final Callback<Collection<FirebaseContent>> resultCallback) {
        getTarget(ref).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Set<FirebaseContent> result = new HashSet<>();
                for (DataSnapshot contentSnapshot : snapshot.getChildren()) {
                    FirebaseContent content = contentSnapshot.getValue(FirebaseContent.class);
                    content.setId(contentSnapshot.getKey());
                    result.add(content);
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
