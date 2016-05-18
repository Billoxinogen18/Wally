package com.wally.wally.datacontroller.queries;

import com.firebase.client.Firebase;
import com.firebase.client.Query;


public class AuthorQuery extends FirebaseQuery {
    private String authorId;

    public AuthorQuery(String authorId) {
        if (authorId == null)
            throw new IllegalArgumentException("Provided authorId is null");
        this.authorId = authorId;
    }

    @Override
    public Query getTarget(Firebase ref) {
        return ref.orderByChild("author/id").equalTo(authorId);
    }
}