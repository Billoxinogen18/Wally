package com.wally.wally.datacontroller.firebase;

import android.net.Uri;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.wally.wally.datacontroller.callbacks.Callback;

import java.io.File;
import java.util.UUID;

public class FirebaseDAL {

    public static void uploadFile(StorageReference storage, String localPath,
                                  final Callback<String> resultCallback) {

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

    public static String save(DatabaseReference ref, FirebaseObject target) {
        ref.setValue(target);
        return ref.getKey();
    }

    public static void save(DatabaseReference ref, FirebaseObject target,
                            final Callback<String > callback) {
        ref.push().setValue(target, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError error, DatabaseReference destination) {
                if (error == null) {
                    callback.onResult(destination.getKey());
                } else {
                    callback.onError(error.toException());
                }
            }
        });
    }

    public static String delete(DatabaseReference ref, FirebaseObject target) {
        ref.child(target.id).removeValue();
        return null;
    }

    public static void delete(DatabaseReference ref, FirebaseObject target,
                              final Callback<Boolean> statusCallback) {
        ref.child(target.id).removeValue(new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError firebaseError, DatabaseReference firebase) {
                statusCallback.onResult(firebaseError == null);
            }
        });
    }

}
