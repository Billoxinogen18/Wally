package com.wally.wally.datacontroller;

import com.firebase.client.Firebase;
import com.firebase.client.Query;
import com.wally.wally.datacontroller.firebase.FirebaseQuery;

public class UUIDQuery extends FirebaseQuery {
    private String uuid;

    public UUIDQuery(String uuid) {
        if (uuid == null)
            throw new IllegalArgumentException("Provided uuid is null");
        this.uuid = uuid;
    }

    @Override
    public Query getTarget(Firebase ref) {
        return ref.orderByChild("uuid").equalTo(uuid);
    }
}
