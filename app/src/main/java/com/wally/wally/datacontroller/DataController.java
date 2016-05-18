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

    public void googleAuth(String accessToken, final Callback<User> callback) {
        firebaseAuth("google", accessToken, new Callback<AuthData>() {
            @Override
            public void onResult(AuthData authData) {
                String id = authData.getUid();
                String ggId = (String) authData.getProviderData().get("id");
                User user = new User(id).withGgId(ggId);
                users.child(id).child("ggId").setValue(ggId);
                callback.onResult(user);
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }

    private void firebaseAuth(String provider, String token, final Callback<AuthData> callback) {
        firebaseRoot.authWithOAuthToken(provider, token, new Firebase.AuthResultHandler() {
            @Override
            public void onAuthenticated(AuthData authData) {
                callback.onResult(authData);
            }

            @Override
            public void onAuthenticationError(FirebaseError firebaseError) {
                callback.onError(firebaseError.toException());
            }
        });
    }

}