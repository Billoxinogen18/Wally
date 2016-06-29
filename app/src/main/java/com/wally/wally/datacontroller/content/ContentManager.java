package com.wally.wally.datacontroller.content;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;
import com.wally.wally.datacontroller.callbacks.Callback;
import com.wally.wally.datacontroller.firebase.FirebaseDAL;

public class ContentManager {
    private StorageReference storage;
    private DatabaseReference contents, rooms;

    public ContentManager(DatabaseReference rooms,
                          DatabaseReference contents,
                          StorageReference storage) {

        this.rooms =rooms;
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
                new Callback<String>() {
                    @Override
                    public void onResult(String result) {
                        c.withImageUri(result);
                        new FirebaseContent(c).save(ref);
                        if (c.getUuid() != null) {
                            String extendedId = ref.getParent().getKey() + ":" + ref.getKey();
                            addInRoom(c.getUuid(), extendedId);
                        }
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

    private void uploadImage(String imagePath, String folder, final Callback<String> callback) {
        if (imagePath != null && imagePath.startsWith(Content.UPLOAD_URI_PREFIX)) {
            String imgUriString = imagePath.substring(Content.UPLOAD_URI_PREFIX.length());
            FirebaseDAL.uploadFile(storage.child(folder), imgUriString, callback);
        } else {
            callback.onResult(imagePath);
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