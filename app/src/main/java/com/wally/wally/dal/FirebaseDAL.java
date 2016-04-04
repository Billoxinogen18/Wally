package com.wally.wally.dal;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FirebaseDAL implements DataAccessLayer {
    private List<Content> db;

    public FirebaseDAL(Context context) {
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

    @Override
    public void fetch(@NonNull Query query, @NonNull Callback<Collection<Content>> resultCallback) {
        resultCallback.call(new ArrayList<Content>(db), null);
    }
}
