package com.wally.wally.datacontroller;

import android.content.Context;

import com.firebase.client.Firebase;

import java.util.Collection;


public class DataController {
    private static final String FIREBASE_URL = "https://burning-inferno-2566.firebaseio.com/";
    private static final String CONTENT_DB_NAME = "Test";
    private static DataController instance;

    private DataAccessLayer<Content> contentManager;

    private DataController(DataAccessLayer<Content> contentManager) {
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
}
