package com.wally.wally.datacontroller;

import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.StorageReference;
import com.wally.wally.adf.AdfInfo;
import com.wally.wally.adf.AdfService;
import com.wally.wally.datacontroller.callbacks.Callback;
import com.wally.wally.datacontroller.firebase.FirebaseDAL;
import com.wally.wally.datacontroller.firebase.FirebaseObject;
import com.wally.wally.datacontroller.firebase.geofire.GeoHash;
import com.wally.wally.datacontroller.firebase.geofire.GeoHashQuery;
import com.wally.wally.datacontroller.utils.SerializableLatLng;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FirebaseAdfService implements AdfService {
    public static final double SEARCH_RADIUS_M = 50;
    private final DatabaseReference db;
    private final StorageReference storage;

    public FirebaseAdfService(DatabaseReference db, StorageReference storage) {
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
    public void searchNearLocation(SerializableLatLng location, final Callback<List<AdfInfo>> callback) {
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
            final int n, final Callback<List<AdfInfo>> callback) {
        return new ValueEventListener() {
            private int nUpdates = n;
            private List<AdfInfo> aggregated = new ArrayList<>();


            private synchronized boolean update(Collection<AdfInfo> result) {
                aggregated.addAll(result);
                return --nUpdates == 0;
            }

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Collection<AdfInfo> result = new HashSet<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    result.add(toAdfInfo(snapshot));
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
    @Deprecated
    public void upload(String path, final AdfInfo info, final Callback<Void> callback) {
        upload(path, info);
        callback.onResult(null);
    }

    @Override
    public void upload(String path, final AdfInfo info) {
        FirebaseDAL.uploadFile(storage, path, info.getUuid(), new Callback<String>() {
            @Override
            public void onResult(String result) {
                saveMetaData(info);
            }

            @Override
            public void onError(Exception e) {
                // should retry?!
            }
        });
    }

    private void saveMetaData(AdfInfo obj) {
        SerializableLatLng l = obj.getCreationLocation();
        String hash = new GeoHash(l.getLatitude(), l.getLongitude()).getGeoHashString();
        db.child(obj.getUuid()).updateChildren(toFirebaseObject(obj).put("hash", hash));
    }

    private FirebaseObject toFirebaseObject(AdfInfo obj) {
        SerializableLatLng l = obj.getCreationLocation();
        return new FirebaseObject()
                .put("name", obj.getName())
                .put("uuid", obj.getUuid())
                .put("location/latitude", l.getLatitude())
                .put("location/longitude", l.getLongitude());
    }

    private AdfInfo toAdfInfo(DataSnapshot snapshot) {
        return new AdfInfo()
                .withName(snapshot.child("name").getValue(String.class))
                .withUuid(snapshot.child("uuid").getValue(String.class))
                .withLocation(
                        new SerializableLatLng(
                            snapshot.child("location/latitude").getValue(Double.class),
                            snapshot.child("location/longitude").getValue(Double.class))
                );
    }
}