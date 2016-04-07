package com.wally.wally.dal.content;

public class Content {
    private static final int MAX_TITLE_LENGTH = 150;

    private String title;
    private String note;
    private String image;
    private Location location;
    private SocialVisibility visibility;
    private Range range;
    private long timestamp;

    public Content() {

    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        if(title.length() > MAX_TITLE_LENGTH)
            throw new IllegalArgumentException("Title exceeded length limit");
        this.title = title;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public SocialVisibility getVisibility() {
        return visibility;
    }

    public void setVisibility(SocialVisibility visibility) {
        this.visibility = visibility;
    }

    public Range getRange() {
        return range;
    }

    public void setRange(Range range) {
        this.range = range;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
