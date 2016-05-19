package com.wally.wally.datacontroller.queries;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

public class UUIDQuery extends FirebaseQuery {
    private String uuid;

    public UUIDQuery(String uuid) {
        if (uuid == null)
            throw new IllegalArgumentException("Provided uuid is null");
        this.uuid = uuid;
    }

    @Override
    public Query getTarget(DatabaseReference ref) {
        return ref.orderByChild("uuid").equalTo(uuid);
    }
}
