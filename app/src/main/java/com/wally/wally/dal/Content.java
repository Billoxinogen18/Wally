package com.wally.wally.dal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.wally.wally.dal.content.Location;
import com.wally.wally.dal.content.Range;
import com.wally.wally.dal.content.SocialVisibility;

public class Content {
    private static final int MAX_TITLE_LENGTH = 150;
    private String id;
    private String title;
    private String note;
    private String image;
    private Location location;
    private SocialVisibility visibility;
    private Range range;
    private long timestamp;

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

    public String getTitle() {
        return title;
    }

    Content withTitle(String title) {
        if(title.length() > MAX_TITLE_LENGTH)
            throw new IllegalArgumentException("Title exceeded length limit");
        this.title = title;
        return this;
    }

    public String getNote() {
        return note;
    }

    Content withNote(String note) {
        this.note = note;
        return this;
    }

    public String getImage() {
        return image;
    }

    Content withImage(String image) {
        this.image = image;
        return this;
    }

    public Location getLocation() {
        return location;
    }

    Content withLocation(Location location) {
        this.location = location;
        return this;
    }

    public SocialVisibility getVisibility() {
        return visibility;
    }

    Content withVisibility(SocialVisibility visibility) {
        this.visibility = visibility;
        return this;
    }

    public Range getRange() {
        return range;
    }

    Content withRange(Range range) {
        this.range = range;
        return this;
    }

    public long getTimestamp() {
        return timestamp;
    }

    Content withTimestamp(long timestamp) {
        this.timestamp = timestamp;
        return this;
    }

}
