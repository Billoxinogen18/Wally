package com.wally.wally.datacontroller.adf;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.StorageReference;
import com.wally.wally.datacontroller.DebugUtils;
import com.wally.wally.datacontroller.callbacks.Callback;
import com.wally.wally.datacontroller.firebase.FirebaseDAL;
import com.wally.wally.datacontroller.firebase.FirebaseObject;
import com.wally.wally.datacontroller.firebase.geofire.GeoHash;

import java.io.File;
import java.util.List;

public class FirebaseADFService implements ADFService {

    private static final String TAG = FirebaseADFService.class.getSimpleName();
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
                            public void onFailure(Exception e) {
                                callback.onError(e);
                            }
                        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void searchADfMetaDataNearLocation(@NonNull LatLng location, final Callback<List<AdfMetaData>> callback) {
        Log.w(TAG, "searchADfMetaDataNearLocation: the method is stub");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (DebugUtils.randomBool()) {
                    callback.onResult(DebugUtils.generateRandomAdfMetaData(5));
                } else {
                    callback.onError(new Exception("Here is random description"));
                }
            }
        });
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
}