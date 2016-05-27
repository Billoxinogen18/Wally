package com.wally.wally.datacontroller;

import android.net.Uri;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.wally.wally.datacontroller.callbacks.Callback;

import java.io.File;
import java.util.UUID;

public class FirebaseUtils {

    public static void uploadFile(
            StorageReference storage, String localPath, final Callback<String> resultCallback) {

        storage.child(UUID.randomUUID().toString())
                .putFile(Uri.fromFile(new File(localPath)))
                .addOnSuccessListener(
                        new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                Uri downloadUri = taskSnapshot.getDownloadUrl();
                                if (downloadUri == null) {
                                    resultCallback.onError(new Exception("could not upload!"));
                                } else {
                                    resultCallback.onResult(downloadUri.toString());
                                }
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                resultCallback.onError(e);
                            }
                        }
                );
    }
}
