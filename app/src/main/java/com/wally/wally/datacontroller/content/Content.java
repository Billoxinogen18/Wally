package com.wally.wally.datacontroller.content;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;

public class Content implements Serializable {
    public static final String UPLOAD_URI_PREFIX = "file://";

    private static final int MAX_TITLE_LENGTH = 150;
    private String id;
    private String title;
    private String note;
    private String imageUri;
    private String uuid;
    private Integer color;
    private TangoData tangoData;
    private SerializableLatLng location;
    private Visibility visibility;
    private String authorId;

    public Content() {
    }

    public Content(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    public Content withId(String id) {
        this.id = id;
        return this;
    }

    public String getUuid() {
        return uuid;
    }

    public Content withUuid(String uuid) {
        this.uuid = uuid;
        return this;
    }

    public Integer getColor() {
        return color;
    }

    public Content withColor(Integer color) {
        this.color = color;
        return this;
    }

    public TangoData getTangoData() {
        return tangoData;
    }

    public Content withTangoData(TangoData tangoData) {
        this.tangoData = tangoData;
        return this;
    }


    public String getTitle() {
        return title;
    }

    public Content withTitle(String title) {
        if (title != null && title.length() > MAX_TITLE_LENGTH)
            throw new IllegalArgumentException("Title exceeded length limit");
        this.title = title;
        return this;
    }

    public String getNote() {
        return note;
    }

    public Content withNote(String note) {
        this.note = note;
        return this;
    }

    public String getImageUri() {
        return imageUri;
    }

    public Content withImageUri(String image) {
        this.imageUri = image;
        return this;
    }

    public LatLng getLocation() {
        return location == null ? null : location.toLatLng();
    }

    public Content withLocation(LatLng location) {
        this.location = new SerializableLatLng(location);
        return this;
    }

    public Visibility getVisibility() {
        return visibility;
    }

    public Content withVisibility(Visibility visibility) {
        this.visibility = visibility;
        return this;
    }


    public String getAuthorId() {
        return authorId;
    }

    public Content withAuthorId(String authorId) {
        this.authorId = authorId;
        return this;
    }

    @Override
    public String toString() {
        return id + ": {uuid=" + uuid +
                ", note=" + note +
                ", title=" + title +
                ", imageUri=" + imageUri +
                ", visibility=" + visibility +
                ", location=" + location +
                ", tangoData=" + tangoData +
                "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Content content = (Content) o;

        return id != null ? id.equals(content.id) : content.id == null;

    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    private static class SerializableLatLng implements Serializable {
        private double lat;
        private double lng;

        public SerializableLatLng(LatLng loc) {
            lat = loc.latitude;
            lng = loc.longitude;
        }

        public LatLng toLatLng() {
            return new LatLng(lat, lng);
        }

        @Override
        public String toString() {
            return "{" + "lat=" + lat + ", lng=" + lng + "}";
        }
    }
}
