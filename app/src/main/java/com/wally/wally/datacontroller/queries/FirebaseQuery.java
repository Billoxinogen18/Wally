package com.wally.wally.datacontroller.queries;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.wally.wally.datacontroller.callbacks.Callback;
import com.wally.wally.datacontroller.content.FirebaseContent;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public abstract class FirebaseQuery {

    public abstract Query getTarget(DatabaseReference ref);

    public final void fetch(DatabaseReference ref, final Callback<Collection<FirebaseContent>> callback) {
        getTarget(ref).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Set<FirebaseContent> result = new HashSet<>();
                for (DataSnapshot contentSnapshot : snapshot.getChildren()) {
                    FirebaseContent content = contentSnapshot.getValue(FirebaseContent.class);
                    content.id = contentSnapshot.getKey();
                    result.add(content);
                }
                callback.onResult(result);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onError(databaseError.toException());
            }

        });
    }

}