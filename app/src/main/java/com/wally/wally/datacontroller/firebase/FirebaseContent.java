package com.wally.wally.datacontroller.firebase;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.android.gms.maps.model.LatLng;
import com.wally.wally.datacontroller.Callback;
import com.wally.wally.datacontroller.content.Content;
import com.wally.wally.datacontroller.content.TangoData;
import com.wally.wally.datacontroller.content.Visibility;
import com.wally.wally.datacontroller.user.User;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class FirebaseContent {

    // Content
    private String id;
    private String note;
    private String uuid;
    private String title;
    private String imageUri;
    private User author;

    // Location
    private double latitude;
    private double longitude;

    // TangoData
    private double scale;
    private double[] rotation;
    private double[] translation;

    // Visibility
    private int socialVisibility;
    private int rangeVisibility;
    private Date visibleUntil;
    private boolean isPreviewVisible;

    public FirebaseContent() {
    }

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
            rotation = tangoData.getRotation();
            translation = tangoData.getTranslation();
        }

        Visibility visibility = content.getVisibility();
        if (visibility != null) {
            rangeVisibility = visibility.getRangeVisibility().getRange();
            socialVisibility = visibility.getSocialVisibility().getMode();
            visibleUntil = visibility.getVisibleUntil();
            isPreviewVisible = visibility.isPreviewVisible();
        }
    }

    @JsonIgnore
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNote() {
        return note;
    }

    public String getUuid() {
        return uuid;
    }

    public String getTitle() {
        return title;
    }

    public String getImageUri() {
        return imageUri;
    }

    public User getAuthor() {
        return author;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getScale() {
        return scale;
    }

    public double[] getRotation() {
        return rotation;
    }

    public double[] getTranslation() {
        return translation;
    }

    public int getSocialVisibility() {
        return socialVisibility;
    }

    public int getRangeVisibility() {
        return rangeVisibility;
    }

    public Date getVisibleUntil() {
        return visibleUntil;
    }

    public boolean getIsPreviewVisible() {
        return isPreviewVisible;
    }

    public void save(Firebase ref) {
        if (id == null) {
            ref = ref.push();
            ref.setValue(this);
            setId(ref.getKey());
        } else {
            ref.child(id).setValue(this);
        }
    }

    public void save(Firebase ref, final Callback<Boolean> statusCallback) {
        ref.push().setValue(this, new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                if (firebaseError == null) {
                    setId(firebase.getKey());
                    statusCallback.call(true, null);
                } else {
                    statusCallback.call(false, firebaseError.toException());
                }
            }
        });
    }

    public void delete(Firebase ref) {
        ref.child(id).removeValue();
    }

    public void delete(Firebase ref, final Callback<Boolean> statusCallback) {
        ref.child(getId()).removeValue(new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                if (firebaseError == null) {
                    statusCallback.call(true, null);
                } else {
                    statusCallback.call(false, firebaseError.toException());
                }
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
                        .withRotation(rotation)
                        .withTranslation(translation))
                .withVisibility(new Visibility()
                        .withRangeVisibility(new Visibility.RangeVisibility(rangeVisibility))
                        .withSocialVisibility(new Visibility.SocialVisibility(socialVisibility))
                        .withTimeVisibility(visibleUntil)
                        .withVisiblePreview(isPreviewVisible));
    }
}