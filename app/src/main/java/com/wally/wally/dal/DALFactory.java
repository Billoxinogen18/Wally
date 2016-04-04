package com.wally.wally.dal;

import android.content.Context;

public class DALFactory {
    public static DataAccessLayer create(Context context) {
        return new FirebaseDAL(context);
    }
}
