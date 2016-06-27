package com.wally.wally.datacontroller.adf;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;
import com.wally.wally.datacontroller.DebugUtils;
import com.wally.wally.datacontroller.callbacks.Callback;

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
        Log.w(TAG, "download: the method is stub");

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                callback.onError(new Exception("Here is random description"));
            }
        }).start();
//        File localFile = new File(path);
//
//        storage.child(uuid).child(uuid).getFile(localFile)
//                .addOnSuccessListener(
//                        new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
//                            @Override
//                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
//                                callback.onResult(null);
//                            }
//                        })
//                .addOnFailureListener(
//                        new OnFailureListener() {
//                            @Override
//                            public void onFailure(Exception e) {
//                                callback.onError(e);
//                            }
//                        });
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
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                callback.onResult(DebugUtils.generateRandomAdfMetaData(5));
            }
        }).start();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void upload(String path, AdfMetaData adfMetaData, final Callback<Void> callback) {
        Log.w(TAG, "upload: the method is stub");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (DebugUtils.randomBool()) {
                    callback.onResult(null);
                } else {
                    callback.onError(new Exception("Here is random description"));
                }
            }
        }).start();

//        FirebaseDAL.uploadFile(storage.child(uuid), path, new Callback<String>() {
//            @Override
//            public void onResult(String result) {
//                GeoHash hash = new GeoHash(location.latitude, location.longitude);
//                db.child(hash.getGeoHashString()).setValue(uuid);
//                callback.onResult(null);
//            }
//
//            @Override
//            public void onError(Exception e) {
//                callback.onError(e);
//            }
//        });
    }
}