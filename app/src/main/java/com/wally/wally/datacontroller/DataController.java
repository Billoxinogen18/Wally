package com.wally.wally.datacontroller;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.wally.wally.datacontroller.callbacks.Callback;
import com.wally.wally.datacontroller.callbacks.FetchResultCallback;
import com.wally.wally.datacontroller.callbacks.FirebaseFetchResultCallback;
import com.wally.wally.datacontroller.content.Content;
import com.wally.wally.datacontroller.content.FirebaseContent;
import com.wally.wally.datacontroller.fetchers.ContentFetcher;
import com.wally.wally.datacontroller.fetchers.KeyPager;
import com.wally.wally.datacontroller.firebase.FirebaseDAL;
import com.wally.wally.datacontroller.queries.UUIDQuery;
import com.wally.wally.datacontroller.user.User;

public class DataController {
    public static final String TAG = DataController.class.getSimpleName();

    private static DataController instance;

    private User currentUser;
    private StorageReference storage;
    private DatabaseReference users, contents;

    private DataController(DatabaseReference database, StorageReference storage) {
        this.storage = storage;
        users = database.child(Config.USERS_NODE);
        contents = database.child(Config.CONTENTS_NODE);
        DebugUtils.sanityCheck();
    }

    public static DataController create() {
        if (instance == null) {
            instance = new DataController(
                    FirebaseDatabase.getInstance().getReference().child(Config.DATABASE_ROOT),
                    FirebaseStorage.getInstance().getReference().child(Config.STORAGE_ROOT)
            );
        }
        return instance;
    }

    private void uploadImage(String imagePath, String folder, final Callback<String> callback) {
        if (imagePath != null && imagePath.startsWith(Content.UPLOAD_URI_PREFIX)) {
            String imgUriString = imagePath.substring(Content.UPLOAD_URI_PREFIX.length());
            FirebaseDAL.uploadFile(storage.child(folder), imgUriString, callback);
        } else {
            callback.onResult(imagePath);
        }
    }

    public void save(final Content c) {
        DatabaseReference target;

        if (c.isPublic()) {
            target = contents.child("Public");
        } else if (c.isPrivate()) {
            target = contents.child(c.getAuthorId()).child("Private");
        } else {
            target = contents.child(c.getAuthorId()).child("Shared");
        }

        if (c.getId() == null) {
            target = target.push();
        } else {
            target = target.child(c.getId());
        }

        final DatabaseReference ref = target;
        uploadImage(
                c.getImageUri(),
                ref.getKey(),
                new Callback<String>() {
                    @Override
                    public void onResult(String result) {
                        c.withImageUri(result);
                        c.withId(new FirebaseContent(c).save(ref));
                    }

                    @Override
                    public void onError(Exception e) {
                        // Omitted Implementation:
                        // If we are here image upload failed somehow
                        // We decided to leave this case for now!
                    }
                }
        );
    }

    public void delete(Content c) {
        new FirebaseContent(c).delete(contents);
    }

    /**
     * No alternative sadly, this method may crash badly
     */
    @Deprecated
    public void fetchByUUID(String uuid, FetchResultCallback callback) {
        new UUIDQuery(uuid).fetch(contents, new FirebaseFetchResultCallback(callback));
    }

    public ContentFetcher createPublicContentFetcher() {
        return new KeyPager(contents.child("Public"));
    }

    public ContentFetcher createVisibleContentFetcher(User user, LatLng center, double r) {
        // TODO stub implementation
        return createPublicContentFetcher();
    }

    public ContentFetcher createMyContentFetcher(User user, LatLng center, double r) {
        // TODO stub implementation
        return createPublicContentFetcher();
    }

    public ContentFetcher createUserContentFetcher(User me, User other, LatLng center, double r) {
        // TODO stub implementation
        return createPublicContentFetcher();
    }

    public User getCurrentUser() {
        if (currentUser != null) return currentUser;
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return null;
        String id = user.getUid();
        // .get(1) assumes only one provider (Google)
        String ggId = user.getProviderData().get(1).getUid();
        currentUser = new User(id).withGgId(ggId);
        users.child(id).setValue(currentUser);
        return currentUser;
    }

    public void fetchUser(String id, final Callback<User> callback) {
        users.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                callback.onResult(dataSnapshot.getValue(User.class));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onError(databaseError.toException());
            }
        });
    }
}