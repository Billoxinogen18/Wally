package com.wally.wally.datacontroller.firebase;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.wally.wally.datacontroller.Callback;
import com.wally.wally.datacontroller.DataAccessLayer;
import com.wally.wally.datacontroller.content.Content;
import com.wally.wally.datacontroller.content.FirebaseContent;
import com.wally.wally.datacontroller.oldqueries.FirebaseQuery;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Deprecated
public class FirebaseDAL implements DataAccessLayer<Content, FirebaseQuery> {
    private Firebase fb;

    public FirebaseDAL(Firebase inner) {
        this.fb = inner;
    }

    @Override
    public void save(final Content c, final Callback<Boolean> statusCallback) {
        new FirebaseContent(c).save(fb, statusCallback);
    }

    @Override
    public void save(Content c) {
        new FirebaseContent(c).save(fb);
    }

    @Override
    public void delete(Content c, final Callback<Boolean> statusCallback) {
        new FirebaseContent(c).delete(fb, statusCallback);
    }

    @Override
    public void delete(Content c) { new FirebaseContent(c).delete(fb); }

    @Override
    public void fetch(final FirebaseQuery query,
                      final Callback<Collection<Content>> resultCallback) {

        query.getTarget(fb).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Set<Content> result = new HashSet<>();
                for (DataSnapshot contentSnapshot: snapshot.getChildren()) {
                    FirebaseContent fbcontent = contentSnapshot.getValue(FirebaseContent.class);
                    fbcontent.setId(contentSnapshot.getKey());
                    result.add(fbcontent.toContent());
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
