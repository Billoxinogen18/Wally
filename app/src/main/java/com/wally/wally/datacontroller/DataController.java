package com.wally.wally.datacontroller;

import android.content.Context;

import com.firebase.client.Firebase;
import com.google.android.gms.maps.model.LatLngBounds;
import com.wally.wally.datacontroller.content.Content;
import com.wally.wally.datacontroller.content.FirebaseContent;
import com.wally.wally.datacontroller.content.FirebaseDAL;
import com.wally.wally.datacontroller.content.FirebaseQuery;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class DataController {
    private static final String FIREBASE_URL = "https://burning-inferno-2566.firebaseio.com/";
    private static final String CONTENT_DB_NAME = "Contents";
    private static DataController instance;

    private DataAccessLayer<FirebaseContent, FirebaseQuery> contentManager;

    private DataController(DataAccessLayer<FirebaseContent, FirebaseQuery> contentManager) {
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
        contentManager.save(new FirebaseContent(c));
    }
    public void delete(Content c) { contentManager.delete(new FirebaseContent(c)); }

    public void fetch(LatLngBounds bounds, final Callback<Collection<Content>> resultCallback) {
        contentManager.fetch(new FirebaseQuery().withBounds(bounds), new Callback<Collection<FirebaseContent>>() {
            @Override
            public void call(Collection<FirebaseContent> result, Exception e) {
                List<Content> contents = new ArrayList<>();
                for (FirebaseContent c : result) {
                    contents.add(c.toContent());
                }
                resultCallback.call(contents, e);
            }
        });
    }

    public void fetch(String uuid, final Callback<Collection<Content>> resultCallback) {
        contentManager.fetch(new FirebaseQuery().withUuid(uuid), new Callback<Collection<FirebaseContent>>() {
            @Override
            public void call(Collection<FirebaseContent> result, Exception e) {
                List<Content> contents = new ArrayList<>();
                for (FirebaseContent c : result) {
                    contents.add(c.toContent());
                }
                resultCallback.call(contents, e);
            }
        });
    }

}
