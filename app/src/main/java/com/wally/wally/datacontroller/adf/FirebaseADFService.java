package com.wally.wally.datacontroller.adf;

import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.StorageReference;
import com.wally.wally.datacontroller.callbacks.Callback;
import com.wally.wally.datacontroller.firebase.FirebaseDAL;
import com.wally.wally.datacontroller.firebase.FirebaseObject;
import com.wally.wally.datacontroller.firebase.geofire.GeoHash;
import com.wally.wally.datacontroller.firebase.geofire.GeoHashQuery;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FirebaseADFService implements ADFService {
    public static final double SEARCH_RADIUS_M = 50;
    private final DatabaseReference db;
    private final StorageReference storage;

    public FirebaseADFService(DatabaseReference db, StorageReference storage) {
        this.db = db;
        this.storage = storage.child("ADFs");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void download(String path, String uuid, final Callback<Void> callback) {
        File localFile = new File(path);

        storage.child(uuid).getFile(localFile)
                .addOnSuccessListener(
                        new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                callback.onResult(null);
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                callback.onError(e);
                            }
                        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void searchADfMetaDataNearLocation(LatLng location, final Callback<List<AdfMetaData>> callback) {
        Set<GeoHashQuery> queries = GeoHashQuery.queriesAtLocation(location, SEARCH_RADIUS_M);
        ValueEventListener aggregator = getMetaDataAggregatorCallback(queries.size(), callback);
        for (GeoHashQuery q : queries) {
            Query query = db.orderByChild("hash")
                    .startAt(q.getStartValue())
                    .endAt(q.getEndValue());
            query.addListenerForSingleValueEvent(aggregator);
        }
    }

    public ValueEventListener getMetaDataAggregatorCallback(
            final int n, final Callback<List<AdfMetaData>> callback) {
        return new ValueEventListener() {
            private int nUpdates = n;
            private List<AdfMetaData> aggregated = new ArrayList<>();


            private synchronized boolean update(Collection<AdfMetaData> result) {
                aggregated.addAll(result);
                return --nUpdates == 0;
            }

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Collection<AdfMetaData> result = new HashSet<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    result.add(toAdfMetaData(snapshot));
                }
                if (update(result)) {
                    callback.onResult(aggregated);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void upload(String path, final AdfMetaData adfMetaData, final Callback<Void> callback) {
        FirebaseDAL.uploadFile(storage, path, adfMetaData.getUuid(), new Callback<String>() {
            @Override
            public void onResult(String result) {
                saveMetaData(adfMetaData);
                callback.onResult(null);
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }

    private void saveMetaData(AdfMetaData obj) {
        LatLng l = obj.getLatLng();
        String hash = new GeoHash(l.latitude, l.longitude).getGeoHashString();
        db.child(hash).setValue(toFirebaseObject(obj).put("hash", hash));
    }

    private FirebaseObject toFirebaseObject(AdfMetaData obj) {
        return new FirebaseObject()
                .put("name", obj.getName())
                .put("uuid", obj.getUuid())
                .put("location", obj.getLatLng());
    }

    private AdfMetaData toAdfMetaData(DataSnapshot snapshot) {
        return new AdfMetaData(
                snapshot.child("name").getValue(String.class),
                snapshot.child("uuid").getValue(String.class),
                new LatLng(
                        snapshot.child("location/latitude").getValue(Double.class),
                        snapshot.child("location/longitude").getValue(Double.class)
                )
        );
    }
}