package com.wally.wally.datacontroller.content;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.android.gms.maps.model.LatLng;

import java.util.Date;

public class FirebaseContent {

    // Content
    private String id;
    private String note;
    private String uuid;
    private String title;
    private String imageUri;

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

    public FirebaseContent() {}

    public FirebaseContent(Content content) {
        id = content.getId();

        note = content.getNote();
        uuid = content.getUuid();
        title = content.getTitle();
        imageUri = content.getImageUri();

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

    public void setId(String id) {
        this.id = id;
    }

    @JsonIgnore
    public String getId() {
        return id;
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

    public Content toContent() {
        return new Content()
                .withId(id)
                .withNote(note)
                .withUuid(uuid)
                .withImageUri(imageUri)
                .withTitle(title)
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