package com.wally.wally.datacontroller.queries;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.wally.wally.datacontroller.callbacks.Callback;
import com.wally.wally.datacontroller.content.FirebaseContent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public abstract class FirebaseQuery {

    public abstract Query getTarget(DatabaseReference ref);

    public final void fetch(DatabaseReference ref, final Callback<Collection<FirebaseContent>> callback) {
        getTarget(ref).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<FirebaseContent> result = new ArrayList<>();
                for (DataSnapshot contentSnapshot : snapshot.getChildren()) {
                    FirebaseContent content = new FirebaseContent();
                    GenericTypeIndicator<Map<String, Object>> indicator =
                            new GenericTypeIndicator<Map<String, Object>>(){};
                    content.putAll(contentSnapshot.getValue(indicator));
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