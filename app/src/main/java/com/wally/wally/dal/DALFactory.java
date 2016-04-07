package com.wally.wally.dal;

import android.content.Context;

public class DALFactory {
    private static final boolean DEBUG = false;
    public static DataAccessLayer create(Context context) {
        if (!DEBUG) {
            return new DummyDAL();
        }
        return new FirebaseDAL(context);
    }
}
