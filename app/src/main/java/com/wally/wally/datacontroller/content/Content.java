package com.wally.wally.datacontroller.content;

import com.google.android.gms.maps.model.LatLng;
import com.wally.wally.datacontroller.utils.SerializableLatLng;

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
        return SerializableLatLng.toLatLng(location);
    }

    public Content withLocation(LatLng location) {
        this.location = SerializableLatLng.fromLatLng(location);
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
        return "Content{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", note='" + note + '\'' +
                ", imageUri='" + imageUri + '\'' +
                ", uuid='" + uuid + '\'' +
                ", color=" + color +
                ", tangoData=" + tangoData +
                ", location=" + location +
                ", visibility=" + visibility +
                ", authorId='" + authorId + '\'' +
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

    public boolean isPublic() {
        return visibility.getSocialVisibility().getMode() == Visibility.SocialVisibility.PUBLIC;
    }

    public boolean isPrivate() {
        return visibility.getSocialVisibility().getMode() == Visibility.SocialVisibility.PRIVATE;
    }
}
