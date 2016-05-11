package com.wally.wally.datacontroller;
import android.content.Context;
import android.util.Log;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.android.gms.maps.model.LatLngBounds;
import com.wally.wally.datacontroller.content.Content;
import com.wally.wally.datacontroller.firebase.FirebaseContent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class DataController {
    private static final String FIREBASE_URL = "https://burning-inferno-2566.firebaseio.com/";
    private static final String CONTENT_DB_NAME = "Develop-Contents";
    private static DataController instance;
    private static Firebase firebaseRoot;

    private DataController(Firebase firebase) {
        firebaseRoot = firebase;
    }

    public static DataController create(Context context) {
        if (instance == null) {
            Firebase.setAndroidContext(context);
            Firebase firebase = new Firebase(FIREBASE_URL).child(CONTENT_DB_NAME);
            instance = new DataController(firebase);
        }
        return instance;
    }

    private void firebaseAuth(String accessToken, final Callback<Boolean> resultCallback) {
        firebaseRoot.authWithOAuthToken("google", accessToken, new Firebase.AuthResultHandler() {
            @Override
            public void onAuthenticated(AuthData authData) {
                Log.d("auth", authData.getProviderData().get("displayName").toString());
                resultCallback.call(true, null);
            }

            @Override
            public void onAuthenticationError(FirebaseError firebaseError) {
                resultCallback.call(false, firebaseError.toException());
            }
        });
    }

    public void googleAuth(String accessToken, Callback<Boolean> resultCallback) {
        firebaseAuth(accessToken, resultCallback);
    }

    public void save(Content c) {
        new FirebaseContent(c).save(firebaseRoot);
    }

    public void delete(Content c) {
        new FirebaseContent(c).delete(firebaseRoot);
    }

    public void fetch(LatLngBounds bounds, final Callback<Collection<Content>> resultCallback) {
        new LatLngQuery(bounds).fetch(firebaseRoot, createFetchCallback(resultCallback));
    }

    public void fetch(String uuid, final Callback<Collection<Content>> resultCallback) {
        new UUIDQuery(uuid).fetch(firebaseRoot, createFetchCallback(resultCallback));
    }

    private Callback<Collection<FirebaseContent>> createFetchCallback(
            final Callback<Collection<Content>> resultCallback) {
        return new Callback<Collection<FirebaseContent>>() {
            @Override
            public void call(Collection<FirebaseContent> result, Exception e) {
                List<Content> contents = new ArrayList<>();
                for (FirebaseContent c : result) {
                    contents.add(convertToContent(c));
                }
                resultCallback.call(contents, e);
            }
        };
    }

    private Content convertToContent(FirebaseContent firebaseContent) {
        // TODO
        return firebaseContent.toContent();
    }

}