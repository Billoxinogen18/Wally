package com.wally.wally.dal;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class FirebaseDAL implements DataAccessLayer {
    private Set<Content> db;
    private Firebase fb;

    public FirebaseDAL(Context context) {
        Firebase.setAndroidContext(context);
        fb = new Firebase("https://burning-inferno-2566.firebaseio.com/").child("Contents");
        this.db = new HashSet<>();
        generateDummyData();
    }

    private void generateDummyData(){
        Random rand = new Random();
        double lat = 41.7151 + rand.nextDouble()/100;
        double lng = 44.8271 + rand.nextDouble()/100;
        saveEventually(new Content(new LatLng(lat, lng)));
    }

    @Override
    public void save(@NonNull Content c, @NonNull Callback<Boolean> statusCallback) {
        db.add(c);
        statusCallback.call(true, null);
    }

    @Override
    public void saveEventually(@NonNull Content c) {
        fb.push().setValue(c);
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
    public void fetch(@NonNull Query query, @NonNull final Callback<Collection<Content>> resultCallback) {
        Log.d("Viper", "Add Listener");
        fb.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot contentSnapshot: snapshot.getChildren()) {
                    db.add(contentSnapshot.getValue(Content.class));
                }
                resultCallback.call(new ArrayList<>(db), null);
                Log.d("Viper", "" + db.size());
            }


            @Override
            public void onCancelled(FirebaseError firebaseError) {
                resultCallback.call(null, new Exception(firebaseError.getMessage()));
            }

        });
    }

}
