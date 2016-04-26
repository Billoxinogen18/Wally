package com.wally.wally.datacontroller;

import android.content.Context;

import com.firebase.client.Firebase;
import com.google.android.gms.maps.model.LatLngBounds;
import com.wally.wally.datacontroller.content.Content;
import com.wally.wally.datacontroller.content.FirebaseDAL;
import com.wally.wally.datacontroller.content.FirebaseQuery;

import java.util.Collection;


public class DataController {
    private static final String FIREBASE_URL = "https://burning-inferno-2566.firebaseio.com/";
    private static final String CONTENT_DB_NAME = "Test";
    private static DataController instance;

    private DataAccessLayer<Content, FirebaseQuery> contentManager;

    private DataController(DataAccessLayer<Content, FirebaseQuery> contentManager) {
        this.contentManager = contentManager;
    }

    public static DataController create(Context context) {
        if (instance == null) {
            Firebase.setAndroidContext(context);
            Firebase fb = new Firebase(FIREBASE_URL);
            instance = new DataController(new FirebaseDAL(fb.child(CONTENT_DB_NAME)));
        }
        return instance;
    }

    public void save(Content c) {
        contentManager.save(c);
    }

    public void fetch(Callback<Collection<Content>> resultCallback) {
        contentManager.fetch(null, resultCallback);
    }

    public void fetch(LatLngBounds bounds, Callback<Collection<Content>> resultCallback) {
        // TODO
    }

    public void fetch(String uuid, Callback<Collection<Content>> resultCallback) {
        // TODO
    }

}
