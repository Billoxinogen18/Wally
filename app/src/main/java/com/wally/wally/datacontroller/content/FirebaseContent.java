package com.wally.wally.datacontroller.content;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.wally.wally.datacontroller.Utils;
import com.wally.wally.datacontroller.callbacks.Callback;
import com.wally.wally.datacontroller.user.User;

import java.util.Date;
import java.util.List;

public class FirebaseContent {
    public static final int PUBLIC = Visibility.SocialVisibility.PUBLIC;

    @Exclude
    public String id;

    // Content
    public String note;
    public String uuid;
    public String title;
    public String imageUri;
    public User author;

    // Location
    public double latitude;
    public double longitude;

    // TangoData
    public double scale;
    public List<Double> rotation;
    public List<Double> translation;

    // Visibility
    public int socialVisibility;
    public int rangeVisibility;
    public Date visibleUntil;
    public boolean isPreviewVisible;

    // Constructor is indirectly used by Firebase!
    @SuppressWarnings("unused")
    public FirebaseContent() {}

    public FirebaseContent(Content content) {
        id = content.getId();

        note = content.getNote();
        uuid = content.getUuid();
        title = content.getTitle();
        imageUri = content.getImageUri();
        author = content.getAuthor();

        LatLng location = content.getLocation();
        if (location != null) {
            latitude = location.latitude;
            longitude = location.longitude;
        }

        TangoData tangoData = content.getTangoData();
        if (tangoData != null) {
            scale = tangoData.getScale();
            rotation = Utils.arrayToList(tangoData.getRotation());
            translation = Utils.arrayToList(tangoData.getTranslation());
        }

        Visibility visibility = content.getVisibility();
        if (visibility != null) {
            rangeVisibility = visibility.getRangeVisibility().getRange();
            socialVisibility = visibility.getSocialVisibility().getMode();
            visibleUntil = visibility.getVisibleUntil();
            isPreviewVisible = visibility.isPreviewVisible();
        }
    }

    public void save(DatabaseReference ref) {
        if (id == null) {
            ref = ref.push();
            ref.setValue(this);
            id = ref.getKey();
        } else {
            ref.child(id).setValue(this);
        }
    }

    @SuppressWarnings("unused")
    public void save(DatabaseReference ref, final Callback<Boolean> statusCallback) {
        ref.push().setValue(this, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError firebaseError, DatabaseReference firebase) {
                if (firebaseError == null) {
                    id = firebase.getKey();
                }
                statusCallback.onResult(firebaseError == null);
            }
        });
    }

    public void delete(DatabaseReference ref) {
        ref.child(id).removeValue();
    }

    @SuppressWarnings("unused")
    public void delete(DatabaseReference ref, final Callback<Boolean> statusCallback) {
        ref.child(id).removeValue(new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError firebaseError, DatabaseReference firebase) {
                statusCallback.onResult(firebaseError == null);
            }
        });
    }

    public Content toContent() {
        return new Content()
                .withId(id)
                .withNote(note)
                .withUuid(uuid)
                .withImageUri(imageUri)
                .withTitle(title)
                .withAuthor(author)
                .withLocation(new LatLng(latitude, longitude))
                .withTangoData(new TangoData()
                        .withScale(scale)
                        .withRotation(Utils.listToArray(rotation))
                        .withTranslation(Utils.listToArray(translation)))
                .withVisibility(new Visibility()
                        .withRangeVisibility(new Visibility.RangeVisibility(rangeVisibility))
                        .withSocialVisibility(new Visibility.SocialVisibility(socialVisibility))
                        .withTimeVisibility(visibleUntil)
                        .withVisiblePreview(isPreviewVisible));
    }
}