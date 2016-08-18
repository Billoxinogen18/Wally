package com.wally.wally.datacontroller;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.wally.wally.datacontroller.DataController.FetchResultCallback;
import com.wally.wally.datacontroller.callbacks.AggregatorCallback;
import com.wally.wally.datacontroller.content.Content;
import com.wally.wally.datacontroller.content.FirebaseContent;
import com.wally.wally.datacontroller.firebase.FirebaseDAL;
import com.wally.wally.datacontroller.queries.FirebaseQuery;

import java.util.Collections;
import java.util.Map;

public class ContentManager {
    private StorageReference storage;
    private DatabaseReference contents, rooms;

    public ContentManager(DatabaseReference rooms,
                          DatabaseReference contents,
                          StorageReference storage) {

        this.rooms = rooms;
        this.storage = storage;
        this.contents = contents;
    }

    public void save(final Content c) {
        if (c.getId() != null) { delete(c); }
        final DatabaseReference ref = getContentReference(c);
        c.withId(ref.getKey());
        uploadImage(
                c.getImageUri(),
                ref.getKey(),
                new FirebaseDAL.FileUploadListener() {
                    @Override
                    public void onUploadSuccess(String result) {
                        c.withImageUri(result);
                        new FirebaseContent(c).save(ref);
                        if (c.getUuid() != null) {
                            String extendedId = ref.getParent().getKey() + ":" + ref.getKey();
                            addInRoom(c.getUuid(), extendedId);
                        }
                    }
                }
        );
    }

    public void delete(Content c) {
        if (c.getId() == null) {
            throw new IllegalArgumentException("Id of the content is null");
        }
        String extendedPublicId = "Public:" + c.getId();
        String extendedSharedId = "Shared:" + c.getId();
        String extendedPrivateId = c.getAuthorId() + ":" + c.getId();
        rooms.child(c.getUuid()).child("Contents").child(extendedPublicId).removeValue();
        rooms.child(c.getUuid()).child("Contents").child(extendedSharedId).removeValue();
        rooms.child(c.getUuid()).child("Contents").child(extendedPrivateId).removeValue();
        contents.child("Public").child(c.getId()).removeValue();
        contents.child("Shared").child(c.getId()).removeValue();
        contents.child(c.getAuthorId()).child(c.getId()).removeValue();
    }

    private void fetchAt(String path, final FetchResultCallback callback) {
        contents.child(path).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Content content = FirebaseQuery.firebaseContentFromSnapshot(dataSnapshot).toContent();
                callback.onResult(Collections.singleton(content));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onError(databaseError.toException());
            }
        });
    }

    public void fetchForUuid(String uuid, final FetchResultCallback callback) {
        rooms.child(uuid).child("Contents")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        GenericTypeIndicator<Map<String, Boolean>> indicator =
                                new GenericTypeIndicator<Map<String, Boolean>>(){};
                        Map<String, Boolean> extendedIds = dataSnapshot.getValue(indicator);
                        if (extendedIds == null) {
                            callback.onResult(Collections.<Content>emptySet());
                            return;
                        }
                        AggregatorCallback aggregator = new AggregatorCallback(callback)
                                .withExpectedCallbacks(extendedIds.size());
                        for (String key : extendedIds.keySet()) {
                            fetchAt(key.replace(':', '/'), aggregator);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        callback.onError(databaseError.toException());
                    }
                });
    }

    private void uploadImage(String imagePath, String folder, FirebaseDAL.FileUploadListener listener) {
        if (imagePath != null && imagePath.startsWith(Content.UPLOAD_URI_PREFIX)) {
            String imgUriString = imagePath.substring(Content.UPLOAD_URI_PREFIX.length());
            FirebaseDAL.uploadFile(storage.child(folder), imgUriString, listener);
        }
    }

    private DatabaseReference getContentReference(Content c) {
        DatabaseReference target;
        if (c.isPublic()) {
            target = contents.child("Public");
        } else if (c.isPrivate()) {
            target = contents.child(c.getAuthorId());
        } else {
            target = contents.child("Shared");
        }
        return c.getId() != null ? target.child(c.getId()) : target.push();
    }

    private void addInRoom(String uuid, String id) {
        rooms.child(uuid).child("Contents").child(id).setValue(true);
    }
}