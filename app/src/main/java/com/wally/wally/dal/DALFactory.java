package com.wally.wally.dal;

import android.content.Context;

public class DALFactory {
    private static final String FIREBASE_URL = "https://burning-inferno-2566.firebaseio.com/";

    public static DataAccessLayer create(Context context) {
        return new FirebaseDAL(context, FIREBASE_URL);
    }
}
