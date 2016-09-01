package com.wally.wally.datacontroller.firebase;

import android.net.Uri;
import android.util.Log;

import com.google.android.gms.tasks.OnSuccessListener;
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
                        });
    }

    public static void uploadFile(StorageReference storage,
                                  String localPath,
                                  FileUploadListener callback) {
        uploadFile(storage, localPath, UUID.randomUUID().toString(), callback);
    }
}
