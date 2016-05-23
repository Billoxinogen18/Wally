package com.wally.wally.datacontroller.queries;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

public class PublicityQuery extends FirebaseQuery {
    private int status;

    public PublicityQuery(int status) {
        this.status = status;
    }

    @Override
    public Query getTarget(DatabaseReference ref) {
        return ref.orderByChild("socialVisibility").equalTo(status);
    }
}