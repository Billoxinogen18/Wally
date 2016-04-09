package com.wally.wally.datacontroller;

import android.content.Context;

import com.wally.wally.dal.Callback;
import com.wally.wally.dal.Content;
import com.wally.wally.dal.DataAccessLayer;
import com.wally.wally.dal.FirebaseDAL;

import java.util.Collection;


public class DataController {
    private static final String FIREBASE_URL = "https://burning-inferno-2566.firebaseio.com/";
    private static DataController instance;

    private DataAccessLayer<Content> dal;

    private DataController(DataAccessLayer<Content> dal) {
        this.dal = dal;
    }

    public static DataController create(Context context) {
        if (instance == null) {
            instance = new DataController(new FirebaseDAL(context, FIREBASE_URL));
        }
        return instance;
    }

    public void fetch(Callback<Collection<Content>> resultCallback) {
        dal.fetch(null, resultCallback);
    }

}
