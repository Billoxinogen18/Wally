package com.wally.wally.datacontroller.content;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;

public class Content implements Serializable {
    private static final int MAX_TITLE_LENGTH = 150;
    private String id;
    private String title;
    private String note;
    private String imageUri;
    private String uuid;
    private TangoData tangoData;
    private LatLng location;
    private Visibility visibility;

    public Content() {}

    public Content(String id) {
        this.id = id;
    }

    @JsonIgnore
    public String getId() {
        return this.id;
    }

    void setId(String id) {
        this.id = id;
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
        return location;
    }

    public Content withLocation(LatLng location) {
        this.location = location;
        return this;
    }

    public Visibility getVisibility(){
        return visibility;
    }

    public Content withVisibility(Visibility visibility){
        this.visibility = visibility;
        return this;
    }

    @Override
    public String toString() {
        return "Content{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", note='" + note + '\'' +
                ", imageUri='" + imageUri + '\'' +
                ", location=" + location +
                '}';
    }
}
