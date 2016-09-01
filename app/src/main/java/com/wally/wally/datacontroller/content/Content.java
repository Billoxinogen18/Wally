package com.wally.wally.datacontroller.content;

import android.graphics.Color;

import com.wally.wally.datacontroller.utils.SerializableLatLng;

import java.io.Serializable;
import java.util.Date;

public class Content implements Serializable {
    public static final String UPLOAD_URI_PREFIX = "file://";

    private static final int MAX_TITLE_LENGTH = 150;
    private String id;
    private String title;
    private String note;
    private String imageUri;
    private String uuid;
    private Integer color;
    private Integer textColor;
    private TangoData tangoData;
    private SerializableLatLng location;
    private Visibility visibility;
    private String authorId;
    private Date creationDate;
    private Puzzle puzzle;

    public Content(Content content) {
        this.id = content.id;
        this.title = content.title;
        this.note = content.note;
        this.imageUri = content.imageUri;
        this.uuid = content.uuid;
        this.color = content.color;
        this.textColor = content.textColor;
        this.tangoData = content.tangoData;
        this.location = content.location;
        this.visibility = content.visibility;
        this.authorId = content.authorId;
        this.creationDate = content.creationDate;
    }

    public Content() {
        withTextColor(Color.BLACK);
        withColor(Color.WHITE);
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

    public Integer getTextColor() {
        return textColor;
    }

    public Content withColor(Integer color) {
        this.color = color;
        return this;
    }

    public Content withTextColor(Integer textColor) {
        this.textColor = textColor;
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

    public SerializableLatLng getLocation() {
        return location;
    }

    public Content withLocation(SerializableLatLng location) {
        this.location = location;
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

    public Date getCreationDate() {
        return creationDate;
    }

    public Content withCreationDate(Date date) {
        creationDate = date;
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
                ", location=" + location +
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

    public Puzzle getPuzzle() {
        return puzzle;
    }

    public Content withPuzzle(Puzzle puzzle) {
        this.puzzle = puzzle;
        return this;
    }

    public boolean isPuzzle() {
        return puzzle != null;
    }
}
