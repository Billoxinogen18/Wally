package com.wally.wally.datacontroller;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.wally.wally.datacontroller.queries.*;
import com.wally.wally.datacontroller.callbacks.*;
import com.wally.wally.datacontroller.content.Content;
import com.wally.wally.datacontroller.content.FirebaseContent;
import com.wally.wally.datacontroller.user.Id;
import com.wally.wally.datacontroller.user.User;

import java.io.File;
import java.util.UUID;

public class DataController {
    public static final String TAG = DataController.class.getSimpleName();

    private static final String DATABASE_ROOT = "Develop";
    private static final String STORAGE_ROOT = "Develop";
    private static final String USERS_NODE = "Users";
    private static final String CONTENTS_NODE = "Contents";

    private static DataController instance;

    private StorageReference storage;
    private DatabaseReference users, contents;

    private DataController(DatabaseReference database, StorageReference storage) {
        this.storage = storage;
        users = database.child(USERS_NODE);
        contents = database.child(CONTENTS_NODE);
    }

    public static DataController create() {
        if (instance == null) {
            DatabaseReference database = FirebaseDatabase.getInstance().getReference();
            StorageReference storage = FirebaseStorage.getInstance()
                    .getReferenceFromUrl("gs://wally-virtual-notes.appspot.com/");
            instance = new DataController(
                    database.child(DATABASE_ROOT),
                    storage.child(STORAGE_ROOT));
        }
        return instance;
    }

    public void save(final Content c) {

        if (c.getImageUri() == null) {
            new FirebaseContent(c).save(contents);
            return;
        }

        String imgUriString = c.getImageUri().substring(7);
        Uri imgUri = Uri.fromFile(new File(imgUriString));
        final String imageId = UUID.randomUUID().toString();
        UploadTask imageUploadTask = storage.child(imageId).putFile(imgUri);
        Log.d(TAG, imgUriString);

        imageUploadTask.addOnSuccessListener(
                new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Uri downloadUri = taskSnapshot.getDownloadUrl();
                        if (downloadUri == null) return; // TODO retry upload?
                        Log.d(TAG, downloadUri.toString());
                        c.withImageUri(downloadUri.toString());
                        new FirebaseContent(c).put(FirebaseContent.K_IMG_ID, imageId).save(contents);

                    }
                }
        ).addOnFailureListener(
                new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // TODO retry upload?
                    }
                }
        );
    }

    public void delete(Content c) {
        new FirebaseContent(c).delete(contents);
    }

    public void fetchByBounds(LatLngBounds bounds, FetchResultCallback callback) {
//        new LatLngQuery(bounds).fetch(contents, new FirebaseFetchResultCallback(callback));
        // TODO stub implementation
        fetchPublicContent(callback);
    }

    public void fetchByUUID(String uuid, FetchResultCallback callback) {
        new UUIDQuery(uuid).fetch(contents, new FirebaseFetchResultCallback(callback));
    }

    public void fetchByAuthor(Id authorId, FetchResultCallback callback) {
        new AuthorQuery(authorId).fetch(contents, new FirebaseFetchResultCallback(callback));
    }

    public void fetchByAuthor(User author, FetchResultCallback resultCallback) {
        fetchByAuthor(author.getId(), resultCallback);
    }

    public void fetchPublicContent(FetchResultCallback callback) {
        new PublicityQuery(FirebaseContent.PUBLIC)
                .fetch(contents, new FirebaseFetchResultCallback(callback));
    }

    public void fetchAccessibleContent(User user, FetchResultCallback callback) {
        // TODO Stub implementation
        fetchPublicContent(callback);
    }

    public User getCurrentUser(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return null;
        String id = user.getUid();
        // .get(1) assumes only one provider (Google)
        String ggId = user.getProviderData().get(1).getUid();
        users.child(id).child("ggId").setValue(ggId);
        return new User(id).withGgId(ggId);
    }
}