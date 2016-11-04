package com.wally.wally.datacontroller.content;

import com.google.firebase.database.ServerValue;
import com.wally.wally.datacontroller.firebase.FirebaseObject;
import com.wally.wally.datacontroller.firebase.geofire.GeoHash;
import com.wally.wally.datacontroller.user.Id;
import com.wally.wally.objects.content.SerializableLatLng;
import com.wally.wally.objects.content.Content;
import com.wally.wally.objects.content.Puzzle;
import com.wally.wally.objects.content.TangoData;
import com.wally.wally.objects.content.Visibility;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FirebaseContent extends FirebaseObject {

    // NoteData
    public static final String K_NOTE           = "note";
    public static final String K_TITLE          = "title";
    public static final String K_COLOR          = "color";
    public static final String K_TEXT_COLOR     = "text_color";
    public static final String K_IMGURI         = "image";
    public static final String K_ROOM           = "roomId";
    public static final String K_AUTHOR         = "authorId";
    public static final String K_NOTE_DATA      = "NoteData";

    // Location
    public static final String K_HASH           = "hash";
    public static final String K_LAT            = "latitude";
    public static final String K_LNG            = "longitude";
    public static final String K_LOCATION       = "Location";

    // TangoData
    public static final String K_SCALE          = "scale";
    public static final String K_ROTATION       = "rotation";
    public static final String K_TRANSLATION    = "translation";
    public static final String K_TANGO_DATA     = "TangoData";

    // Visibility
    public static final String K_PREVIEW        = "preview";
    public static final String K_DURATION       = "duration";
    public static final String K_PUBLICITY      = "publicity";
    public static final String K_ANONYMOUS      = "anonymous";
    public static final String K_SHARED         = "Shared";

    public static final String K_TIMESTAMP      = "timestamp";

    private static final String K_PUZZLE_DATA   = "PuzzleData";
    private static final String K_ANSWERS       = "Answers";
    private static final String K_SUCCESSORS    = "Connections";

    public FirebaseContent() {}

    public FirebaseContent(Content c) {
        id = c.getId();
        put(K_ROOM, c.getUuid());
        put(K_AUTHOR, c.getAuthorId());
        put(K_TIMESTAMP, ServerValue.TIMESTAMP);
        setNoteData(c);
        setPuzzle(c.getPuzzle());
        setLocation(c.getLocation());
        setTangoData(c.getTangoData());
        setVisibility(c.getVisibility());
        c.withId(id);
    }


    private String getRoom() {
        return get(K_ROOM).toString();
    }

    public String getNote() {
        return getChild(K_NOTE_DATA).get(K_NOTE).toString();
    }

    public String getTitle() {
        return getChild(K_NOTE_DATA).get(K_TITLE).toString();
    }

    private Integer getColor() {
        return getChild(K_NOTE_DATA).get(K_COLOR).toInteger();
    }

    private Integer getTextColor() {
        return getChild(K_NOTE_DATA).get(K_TEXT_COLOR).toInteger();
    }

    private String getImageUri() {
        return getChild(K_NOTE_DATA).get(K_IMGURI).toString();
    }

    private String getAuthorId() {
        return get(K_AUTHOR).toString();
    }

    private Double getLatitude() {
        return getChild(K_LOCATION).get(K_LAT).toDouble();
    }

    private Double getLongitude() {
        return getChild(K_LOCATION).get(K_LNG).toDouble();
    }

    private SerializableLatLng getLocation() {
        return containsKey(K_LOCATION) ? new SerializableLatLng(getLatitude(), getLongitude()) : null;
    }

    private Date getCreationDate() {
        long timestamp = get(K_TIMESTAMP).toLong();
        return new Date(timestamp);
    }

    private void setLocation(SerializableLatLng loc) {
        if (loc == null) return;
        String hash = new GeoHash(loc.getLatitude(), loc.getLongitude()).getGeoHashString();
        put(K_HASH, hash);
        getChild(K_LOCATION)
                .put(K_LAT, loc.getLatitude())
                .put(K_LNG, loc.getLongitude());
    }

    private TangoData getTangoData() {
        if (!containsKey(K_TANGO_DATA)) return null;
        FirebaseObject tangoData = getChild(K_TANGO_DATA);
        return new TangoData()
                .withScale(tangoData.get(K_SCALE).toDouble())
                .withRotation(tangoData.get(K_ROTATION).toDoubleArray())
                .withTranslation(tangoData.get(K_TRANSLATION).toDoubleArray());
    }

    private void setTangoData(TangoData td) {
        if (td == null) return;
        getChild(K_TANGO_DATA)
                .put(K_SCALE, td.getScale())
                .put(K_ROTATION, td.getRotation())
                .put(K_TRANSLATION, td.getTranslation());
    }

    private Puzzle getPuzzle() {
        if (!containsKey(K_PUZZLE_DATA)) return null;
        FirebaseObject puzzleData = getChild(K_PUZZLE_DATA);
        return new Puzzle()
                .withMarkerURL(null) // TODO
                .withUnsolvedMarkerURL(null) // TODO
                .withAnswers(puzzleData.get(K_ANSWERS).toStringList())
                .withSuccessors(puzzleData.get(K_SUCCESSORS).toStringList());
    }

    private void setPuzzle(Puzzle puzzle) {
        if (puzzle == null) return;
        getChild(K_PUZZLE_DATA)
                .put(K_ANSWERS, puzzle.getAnswers())
                .put(K_SUCCESSORS, puzzle.getSuccessors());
    }

    private Visibility getVisibility() {
        return new Visibility()
                .withSocialVisibility(getPublicity())
                .withTimeVisibility(get(K_DURATION).toData())
                .withAnonymousAuthor(get(K_ANONYMOUS).toBoolean())
                .withVisiblePreview(getChild(K_NOTE_DATA).get(K_PREVIEW).toBoolean());
    }

    private void setVisibility(Visibility v) {
        if (v == null) return;
        put(K_DURATION, v.getVisibleUntil());
        put(K_PUBLICITY, v.getSocialVisibility().getMode());
        put(K_ANONYMOUS, v.isAuthorAnonymous());
        getChild(K_NOTE_DATA).put(K_PREVIEW, v.isPreviewVisible());
        // TODO generalize creation of "shared" object
        FirebaseObject shared = getChild(K_SHARED)
                .put(Id.PROVIDER_FIREBASE, new FirebaseObject())
                .put(Id.PROVIDER_FACEBOOK, new FirebaseObject())
                .put(Id.PROVIDER_GOOGLE, new FirebaseObject());
        List<Id> sharedWith = v.getSocialVisibility().getSharedWith();
        if (sharedWith == null) return;
        for (Id id : sharedWith) {
            shared.get(id.getProvider()).toFirebaseObject().put(id.getId(), true);
        }
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
                .put(K_COLOR, c.getColor())
                .put(K_TEXT_COLOR, c.getTextColor());
    }

    public Content toContent() {
        return new Content()
                .withId(id)
                .withUuid(getRoom())
                .withNote(getNote())
                .withTitle(getTitle())
                .withColor(getColor())
                .withPuzzle(getPuzzle())
                .withTextColor(getTextColor())
                .withImageUri(getImageUri())
                .withAuthorId(getAuthorId())
                .withLocation(getLocation())
                .withTangoData(getTangoData())
                .withVisibility(getVisibility())
                .withCreationDate(getCreationDate());
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