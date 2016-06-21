package com.wally.wally.datacontroller.queries;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.wally.wally.datacontroller.content.FirebaseContent;
import com.wally.wally.datacontroller.user.Id;

public class SharedWithQuery extends FirebaseQuery {
    private Id userId;

    public SharedWithQuery(Id userId) {
        if (userId == null)
            throw new IllegalArgumentException("Provided uuid is null");
        this.userId = userId;
    }


    @Override
    public Query getTarget(DatabaseReference ref) {
        String child = FirebaseContent.K_SHARED + "/" + userId.getProvider() + "/" + userId.getId();
        return ref.orderByChild(child).equalTo(true);
    }

}
