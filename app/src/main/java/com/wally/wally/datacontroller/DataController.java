package com.wally.wally.datacontroller;

import android.content.Context;
import android.util.Log;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.android.gms.maps.model.LatLngBounds;
import com.wally.wally.datacontroller.content.Content;
import com.wally.wally.datacontroller.content.FirebaseDAL;
import com.wally.wally.datacontroller.content.FirebaseQuery;

import java.util.Collection;


public class DataController {
    private static final String FIREBASE_URL = "https://burning-inferno-2566.firebaseio.com/";
    private static final String CONTENT_DB_NAME = "Contents";
    private static DataController instance;
    private static Firebase fbInstance;

    private DataAccessLayer<Content, FirebaseQuery> contentManager;

    private DataController(DataAccessLayer<Content, FirebaseQuery> contentManager) {
        this.contentManager = contentManager;
    }

    public static DataController create(Context context) {
        if (instance == null) {
            Firebase.setAndroidContext(context);
            fbInstance = new Firebase(FIREBASE_URL);
            instance = new DataController(new FirebaseDAL(fbInstance.child(CONTENT_DB_NAME)));
        }
        return instance;
    }

    public void save(Content c) {
        contentManager.save(c);
    }
    public void delete(Content c) { contentManager.delete(c); }

    private static void firebaseAuth(String accessToken, final Callback<Boolean> resultCallback) {
        fbInstance.authWithOAuthToken("google", accessToken, new Firebase.AuthResultHandler() {
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

    public void fetch(LatLngBounds bounds, Callback<Collection<Content>> resultCallback) {
        contentManager.fetch(new FirebaseQuery().withBounds(bounds), resultCallback);
    }

    public void fetch(String uuid, Callback<Collection<Content>> resultCallback) {
        contentManager.fetch(new FirebaseQuery().withUuid(uuid), resultCallback);
    }

}
