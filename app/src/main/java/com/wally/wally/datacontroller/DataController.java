package com.wally.wally.datacontroller;

import android.content.Context;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.android.gms.maps.model.LatLngBounds;
import com.wally.wally.datacontroller.queries.*;
import com.wally.wally.datacontroller.callbacks.*;
import com.wally.wally.datacontroller.content.Content;
import com.wally.wally.datacontroller.content.FirebaseContent;
import com.wally.wally.datacontroller.user.User;

public class DataController {
    private static final String FIREBASE_URL = "https://burning-inferno-2566.firebaseio.com/";
    private static final String DB_PATH = "Develop";
    private static DataController instance;
    private Firebase firebaseRoot;
    private Firebase users;
    private Firebase contents;

    private DataController(Firebase firebase) {
        firebaseRoot = firebase;
        users = firebaseRoot.child("Users");
        contents = firebaseRoot.child("Contents");
    }

    public static DataController create(Context context) {
        if (instance == null) {
            Firebase.setAndroidContext(context);
            Firebase firebase = new Firebase(FIREBASE_URL).child(DB_PATH);
            instance = new DataController(firebase);
        }
        return instance;
    }

    public void googleAuth(String accessToken, Callback<User> callback) {
        firebaseAuth(accessToken, callback);
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

    public void fetchByAuthor(String authorId, FetchResultCallback callback) {
        new AuthorQuery(authorId).fetch(contents, new FirebaseFetchResultCallback(callback));
    }

    public void fetchByAuthor(User author, FetchResultCallback resultCallback) {
        fetchByAuthor(author.getId(), resultCallback);
    }

    private void firebaseAuth(String accessToken, final Callback<User> callback) {
        // TODO
        // make method more abstract
        // move provider specific data to caller method
        firebaseRoot.authWithOAuthToken("google", accessToken, new Firebase.AuthResultHandler() {
            @Override
            public void onAuthenticated(AuthData authData) {
                // Map<String, Object> data = authData.getProviderData();
                String ggId = (String) authData.getProviderData().get("id");
                User user = new User(authData.getUid()).withGgId(ggId);
                users.child(user.getId()).setValue(user);
                callback.onResult(user);
            }

            @Override
            public void onAuthenticationError(FirebaseError firebaseError) {
                callback.onError(firebaseError.toException());
            }
        });
    }

}