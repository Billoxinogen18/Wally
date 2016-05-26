package com.wally.wally.datacontroller.content;

import com.google.android.gms.maps.model.LatLng;
import com.wally.wally.datacontroller.FirebaseObject;

import java.util.Date;

public class FirebaseContent extends FirebaseObject {
    public static final int PUBLIC = Visibility.SocialVisibility.PUBLIC;

    // NoteData
    public static final String K_NOTE           = "note";
    public static final String K_TITLE          = "title";
    public static final String K_COLOR          = "color";
    public static final String K_IMGURI         = "image";
    public static final String K_IMG_ID         = "img_id";
    public static final String K_ROOM           = "roomId";
    public static final String K_AUTHOR         = "authorId";
    public static final String K_NOTE_DATA      = "NoteData";

    // Location
    public static final String K_LAT            = "latitude";
    public static final String K_LNG            = "longitude";
    public static final String K_LOCATION       = "Location";

    // TangoData
    public static final String K_SCALE          = "scale";
    public static final String K_ROTATION       = "rotation";
    public static final String K_TRANSLATION    = "translation";
    public static final String K_TANGO_DATA     = "TangoData";

    // Visibility
    public static final String K_RANGE          = "range";
    public static final String K_PREVIEW        = "preview";
    public static final String K_DURATION       = "duration";
    public static final String K_PUBLICITY      = "publicity";

    public FirebaseContent() {
    }

    public FirebaseContent(Content c) {
        id = c.getId();
        put(K_ROOM, c.getUuid());
        put(K_AUTHOR, c.getAuthorId());
        setNoteData(c);
        setLocation(c.getLocation());
        setTangoData(c.getTangoData());
        setVisibility(c.getVisibility());
    }


    public String getRoom() {
        return getString(K_ROOM);
    }

    public String getNote() {
        return getChild(K_NOTE_DATA).getString(K_NOTE);
    }

    public String getTitle() {
        return getChild(K_NOTE_DATA).getString(K_TITLE);
    }

    public Integer getColor() {
        return getChild(K_NOTE_DATA).getInteger(K_COLOR);
    }

    public String getImageUri() {
        return getChild(K_NOTE_DATA).getString(K_IMGURI);
    }

    public String getAuthorId() {
        return getString(K_AUTHOR);
    }

    public Double getLatitude() {
        return getChild(K_LOCATION).getDouble(K_LAT);
    }

    public Double getLongitude() {
        return getChild(K_LOCATION).getDouble(K_LNG);
    }

    public LatLng getLocation() {
        return hasChild(K_LOCATION) ? new LatLng(getLatitude(), getLongitude()) : null;
    }

    public TangoData getTangoData() {
        if (!containsKey(K_TANGO_DATA)) return null;
        FirebaseObject tangoData = getChild(K_TANGO_DATA);
        return new TangoData()
                .withScale(tangoData.getDouble(K_SCALE))
                .withRotation(tangoData.getArray(K_ROTATION))
                .withTranslation(tangoData.getArray(K_TRANSLATION));
    }

    public Visibility getVisibility() {
        return new Visibility()
                .withRangeVisibility(getRange())
                .withSocialVisibility(getPublicity())
                .withTimeVisibility((Date) get(K_DURATION))
                .withVisiblePreview((Boolean) getChild(K_NOTE_DATA).get(K_PREVIEW));
    }

    @SuppressWarnings("WrongConstant")
    private Visibility.RangeVisibility getRange() {
        return new Visibility.RangeVisibility(getInteger(K_RANGE));
    }

    @SuppressWarnings("WrongConstant")
    private Visibility.SocialVisibility getPublicity() {
        return new Visibility.SocialVisibility(getInteger(K_PUBLICITY));
    }

    private void setNoteData(Content c) {
        getChild(K_NOTE_DATA)
                .put(K_NOTE, c.getNote())
                .put(K_TITLE, c.getTitle())
                .put(K_IMGURI, c.getImageUri())
                .put(K_COLOR, c.getColor());
    }

    private void setLocation(LatLng loc) {
        if (loc == null) return;
        getChild(K_LOCATION)
                .put(K_LAT, loc.latitude)
                .put(K_LNG, loc.longitude);
    }

    private void setTangoData(TangoData td) {
        if (td == null) return;
        getChild(K_TANGO_DATA)
                .put(K_SCALE, td.getScale())
                .putArray(K_ROTATION, td.getRotation())
                .putArray(K_TRANSLATION, td.getTranslation());
    }

    private void setVisibility(Visibility v) {
        if (v == null) return;
        put(K_DURATION, v.getVisibleUntil());
        put(K_RANGE, v.getRangeVisibility().getRange());
        put(K_PUBLICITY, v.getSocialVisibility().getMode());
        getChild(K_NOTE_DATA).put(K_PREVIEW, v.isPreviewVisible());
    }

    public Content toContent() {
        return new Content()
                .withId(id)
                .withUuid(getRoom())
                .withNote(getNote())
                .withTitle(getTitle())
                .withColor(getColor())
                .withImageUri(getImageUri())
                .withAuthorId(getAuthorId())
                .withLocation(getLocation())
                .withTangoData(getTangoData())
                .withVisibility(getVisibility());
    }

    public static FirebaseContent fromContent(Content content) {
        return new FirebaseContent(content);
    }
}