package com.wally.wally.datacontroller.content;

import com.google.android.gms.maps.model.LatLng;
import com.wally.wally.datacontroller.user.User;

import java.io.Serializable;

public class Content implements Serializable {
    private static final int MAX_TITLE_LENGTH = 150;
    private String id;
    private String title;
    private String note;
    private String imageUri;
    private String uuid;
    private TangoData tangoData;
    private SerializableLatLng location;
    private Visibility visibility;
    private User author;

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
        if (title.length() > MAX_TITLE_LENGTH)
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
        return location.toLatLng();
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


    public User getAuthor() {
        return author;
    }

    public Content withAuthor(User author) {
        this.author = author;
        return this;
    }

    @Override
    public String toString() {
        return "Content{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", note='" + note + '\'' +
                ", imageUri='" + imageUri + '\'' +
                ", uuid='" + uuid + '\'' +
                ", tangoData=" + tangoData +
                ", location=" + location +
                ", visibility=" + visibility +
                '}';
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
            return "SerializableLatLng{" +
                    "lat=" + lat +
                    ", lng=" + lng +
                    '}';
        }
    }
}
