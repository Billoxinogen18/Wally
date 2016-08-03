package com.wally.wally.datacontroller.firebase;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.UUID;

public class FirebaseDAL {

    public interface FileUploadListener {
        void onUploadSuccess(String downloadUri);
    }

    public static void uploadFile(StorageReference storage, String localPath, String name,
                                  final FileUploadListener listener) {
        storage.child(name)
                .putFile(Uri.fromFile(new File(localPath)))
                .addOnSuccessListener(
                        new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                Uri downloadUri = taskSnapshot.getDownloadUrl();
                                if (downloadUri == null) {
                                    Log.d("FirebaseDAL", "File upload error");
                                    // not clear what should we do yet! TODO
                                } else {
                                    listener.onUploadSuccess(downloadUri.toString());
                                }
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d("FirebaseDAL", "File upload error");
                                // not clear what should we do yet! TODO
                            }
                        }
                );
    }

    public static void uploadFile(StorageReference storage,
                                  String localPath,
                                  FileUploadListener callback) {
        uploadFile(storage, localPath, UUID.randomUUID().toString(), callback);
    }

    public static String save(DatabaseReference ref, FirebaseObject target) {
        ref.setValue(target);
        return ref.getKey();
    }

    public static String delete(DatabaseReference ref, FirebaseObject target) {
        ref.child(target.id).removeValue();
        return null;
    }
}
