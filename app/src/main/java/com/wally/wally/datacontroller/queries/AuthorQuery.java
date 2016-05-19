package com.wally.wally.datacontroller.queries;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

public class AuthorQuery extends FirebaseQuery {
    private String authorId;

    public AuthorQuery(String authorId) {
        if (authorId == null)
            throw new IllegalArgumentException("Provided authorId is null");
        this.authorId = authorId;
    }

    @Override
    public Query getTarget(DatabaseReference ref) {
        return ref.orderByChild("author/id").equalTo(authorId);
    }
}