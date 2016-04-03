package com.wally.wally.dal;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class FirebaseDAL implements DataAccessLayer {
    private List<Content> db;

    public FirebaseDAL() {
        this.db = new ArrayList<Content>();
    }

    @Override
    public void save(@NonNull Content c, @NonNull Callback<Boolean> statusCallback) {
        db.add(c);
        statusCallback.call(true, null);
    }

    @Override
    public void saveEventually(@NonNull Content c) {
        db.add(c);
    }

    @Override
    public void delete(@NonNull Content c, @NonNull Callback<Boolean> statusCallback) {
        statusCallback.call(false, new Exception("Stub impl"));
    }

    @Override
    public void deleteEventually(@NonNull Content c) {

    }
}
