package com.wally.wally.datacontroller.content;

import com.google.android.gms.maps.model.LatLng;
import com.wally.wally.datacontroller.firebase.FirebaseObject;
import com.wally.wally.datacontroller.user.Id;

import java.util.ArrayList;
import java.util.List;

public class FirebaseContent extends FirebaseObject {
    public static final int PUBLIC = Visibility.SocialVisibility.PUBLIC;

    // NoteData
    public static final String K_NOTE           = "note";
    public static final String K_TITLE          = "title";
    public static final String K_COLOR          = "color";
    public static final String K_IMGURI         = "image";
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
    public static final String K_SHARED         = "Shared";

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
        return get(K_ROOM).toString();
    }

    public String getNote() {
        return getChild(K_NOTE_DATA).get(K_NOTE).toString();
    }

    public String getTitle() {
        return getChild(K_NOTE_DATA).get(K_TITLE).toString();
    }

    public Integer getColor() {
        return getChild(K_NOTE_DATA).get(K_COLOR).toInteger();
    }

    public String getImageUri() {
        return getChild(K_NOTE_DATA).get(K_IMGURI).toString();
    }

    public String getAuthorId() {
        return get(K_AUTHOR).toString();
    }

    public Double getLatitude() {
        return getChild(K_LOCATION).get(K_LAT).toDouble();
    }

    public Double getLongitude() {
        return getChild(K_LOCATION).get(K_LNG).toDouble();
    }

    public LatLng getLocation() {
        return containsKey(K_LOCATION) ? new LatLng(getLatitude(), getLongitude()) : null;
    }

    public TangoData getTangoData() {
        if (!containsKey(K_TANGO_DATA)) return null;
        FirebaseObject tangoData = getChild(K_TANGO_DATA);
        return new TangoData()
                .withScale(tangoData.get(K_SCALE).toDouble())
                .withRotation(tangoData.get(K_ROTATION).toDoubleArray())
                .withTranslation(tangoData.get(K_TRANSLATION).toDoubleArray());
    }

    public Visibility getVisibility() {
        return new Visibility()
                .withRangeVisibility(getRange())
                .withSocialVisibility(getPublicity())
                .withTimeVisibility(get(K_DURATION).toData())
                .withVisiblePreview(getChild(K_NOTE_DATA).get(K_PREVIEW).toBoolean());
    }

    @SuppressWarnings("WrongConstant")
    private Visibility.RangeVisibility getRange() {
        return new Visibility.RangeVisibility(get(K_RANGE).toInteger());
    }

    @SuppressWarnings("WrongConstant")
    private Visibility.SocialVisibility getPublicity() {
        List<Id> sharedWith = new ArrayList<>();
        FirebaseObject shared = getChild(K_SHARED);
        for (String provider : shared.keySet()) {
            for (String id : shared.get(provider).toStringMap().keySet()) {
                sharedWith.add(new Id(provider, id));
            }
        }
        return new Visibility.SocialVisibility(get(K_PUBLICITY).toInteger())
                .withSharedWith(sharedWith);
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
                .put(K_ROTATION, td.getRotation())
                .put(K_TRANSLATION, td.getTranslation());
    }

    private void setVisibility(Visibility v) {
        if (v == null) return;
        put(K_DURATION, v.getVisibleUntil());
        put(K_RANGE, v.getRangeVisibility().getRange());
        put(K_PUBLICITY, v.getSocialVisibility().getMode());
        getChild(K_NOTE_DATA).put(K_PREVIEW, v.isPreviewVisible());
        // TODO generalize creation of "shared" object
        FirebaseObject shared = getChild(K_SHARED)
                .put(Id.PROVIDER_FIREBASE, new FirebaseObject())
                .put(Id.PROVIDER_FACEBOOK, new FirebaseObject())
                .put(Id.PROVIDER_GOOGLE, new FirebaseObject());
        List<Id> sharedWith = v.getSocialVisibility().getSharedWith();
        if (sharedWith == null) return;
        for (Id id : sharedWith ) {
            shared.get(id.getProvider()).toFirebaseObject().put(id.getId(), true);
        }
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

    @Override
    public FirebaseObject getChild(String key) {
        FirebaseObject child = super.getChild(key);
        if (!containsKey(key)) {
            put(key, child);
        } else {
            child.putAll(get(key).toStringMap());
        }
        return child;
    }
}