package com.wally.wally.datacontroller.queries;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.wally.wally.datacontroller.content.FirebaseContent;
import com.wally.wally.datacontroller.user.Id;

public class AuthorQuery extends FirebaseQuery {
    private Id authorId;

    public AuthorQuery(Id authorId) {
        if (authorId == null)
            throw new IllegalArgumentException("Provided authorId is null");
        this.authorId = authorId;
    }

    @Override
    public Query getTarget(DatabaseReference ref) {
        return ref.orderByChild(FirebaseContent.K_AUTHOR).equalTo(authorId.getId());
    }
}