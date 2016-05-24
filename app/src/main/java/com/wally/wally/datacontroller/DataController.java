package com.wally.wally.datacontroller;

import com.google.android.gms.maps.model.LatLngBounds;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.wally.wally.datacontroller.queries.*;
import com.wally.wally.datacontroller.callbacks.*;
import com.wally.wally.datacontroller.content.Content;
import com.wally.wally.datacontroller.content.FirebaseContent;
import com.wally.wally.datacontroller.user.Id;
import com.wally.wally.datacontroller.user.User;

public class DataController {
    public static final String TAG = DataController.class.getSimpleName();

    private static final String USERS_NODE = "Users";
    private static final String CONTENTS_NODE = "Contents";
    private static final String ROOT_NODE = "Firebase-Update";

    private static DataController instance;

    private DatabaseReference users, contents;

    private DataController(DatabaseReference firebase) {
        users = firebase.child(USERS_NODE);
        contents = firebase.child(CONTENTS_NODE);
    }

    public static DataController create() {
        if (instance == null) {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
            instance = new DataController(ref.child(ROOT_NODE));
        }
        return instance;
    }

    public void save(Content c) {
        new FirebaseContent(c).save(contents);
    }

    public void delete(Content c) {
        new FirebaseContent(c).delete(contents);
    }

    public void fetchByBounds(LatLngBounds bounds, FetchResultCallback callback) {
        new LatLngQuery(bounds).fetch(contents, new FirebaseFetchResultCallback(callback));
    }

    public void fetchByUUID(String uuid, FetchResultCallback callback) {
        new UUIDQuery(uuid).fetch(contents, new FirebaseFetchResultCallback(callback));
    }

    public void fetchByAuthor(Id authorId, FetchResultCallback callback) {
        new AuthorQuery(authorId).fetch(contents, new FirebaseFetchResultCallback(callback));
    }

    // Hope front end will soon user this method :(
    @SuppressWarnings("unused")
    public void fetchByAuthor(User author, FetchResultCallback resultCallback) {
        fetchByAuthor(author.getId(), resultCallback);
    }

    public void fetchPublicContent(FetchResultCallback callback) {
        new PublicityQuery(FirebaseContent.PUBLIC)
                .fetch(contents, new FirebaseFetchResultCallback(callback));
    }

    public User getCurrentUser(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return null;
        String id = user.getUid();
        // .get(0) assumes only one provider (Google)
        String ggId = user.getProviderData().get(1).getUid();
        users.child(id).child("ggId").setValue(ggId);
        return new User(new Id(Id.PROVIDER_FIREBASE, id)).withGgId(new Id(Id.PROVIDER_GOOGLE, ggId));
    }
}