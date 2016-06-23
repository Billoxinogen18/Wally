package com.wally.wally.datacontroller;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.StorageReference;
import com.wally.wally.datacontroller.callbacks.Callback;
import com.wally.wally.datacontroller.firebase.FirebaseDAL;
import com.wally.wally.datacontroller.firebase.geofire.GeoHash;

import java.io.File;

public class FirebaseADFService implements ADFService {

    private final DatabaseReference db;
    private final StorageReference storage;

    public FirebaseADFService(DatabaseReference db, StorageReference storage) {
        this.db = db;
        this.storage = storage.child("ADFs");
    }

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
                    public void onFailure(Exception e) {
                        callback.onError(e);
                    }
                });
    }

    @Override
    public void searchUuidNearLocation(LatLng location, Callback<String> callback) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void upload(String path, final String uuid, final LatLng location, final Callback<Void> callback) {
        FirebaseDAL.uploadFile(storage.child(uuid), path, new Callback<String>() {
            @Override
            public void onResult(String result) {
                GeoHash hash = new GeoHash(location.latitude, location.longitude);
                db.child(hash.getGeoHashString()).setValue(uuid);
                callback.onResult(null);
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });

    }

}