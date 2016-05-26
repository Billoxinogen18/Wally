package com.wally.wally.datacontroller.content;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.wally.wally.datacontroller.Utils;
import com.wally.wally.datacontroller.callbacks.Callback;
import com.wally.wally.datacontroller.user.User;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseContent extends HashMap<String, Object> {
    public static final int PUBLIC = Visibility.SocialVisibility.PUBLIC;

    public static final String K_ROOM = "room";
    public static final String K_NOTE = "note";
    public static final String K_TITLE = "title";
    public static final String K_AUTHOR = "author";
    public static final String K_IMGURI = "image_uri";

    public static final String K_LOCATION = "Location";
    public static final String K_LAT = "latitude";
    public static final String K_LNG = "longitude";

    public static final String K_SCALE = "scale";
    public static final String K_ROTATION = "rotation";
    public static final String K_TRANSLATION = "translation";
    public static final String K_TANGO_DATA = "TangoData";

    public static final String K_RANGE = "range";
    public static final String K_PUBLICITY = "publicity";
    public static final String K_HAS_PREVIEW = "has_preview";
    public static final String K_VISIBLE_UNTIL = "visible_until";

    @Exclude
    public String id;

    public FirebaseContent() {}

    public FirebaseContent(Content content) {
        id = content.getId();
        setInfo(content);
        setLocation(content);
        setTangoData(content);
        setVisibility(content);
    }

    private void setInfo(Content c) {
        put(K_ROOM, c.getUuid());
        put(K_NOTE, c.getNote());
        put(K_TITLE, c.getTitle());
        put(K_IMGURI, c.getImageUri());
        put(K_AUTHOR, c.getAuthor().getId().getId());
    }

    private void setLocation(Content c) {
        LatLng loc = c.getLocation();
        if (loc == null) return;
        Map<String, Double> location = new HashMap<>();
        location.put(K_LAT, loc.latitude);
        location.put(K_LNG, loc.longitude);
        put(K_LOCATION, location);
    }

    private void setTangoData(Content c) {
        TangoData td = c.getTangoData();
        if (td == null) return;
        Map<String, Object> tangoData = new HashMap<>();
        tangoData.put(K_SCALE, td.getScale());
        List<Double> rotation = Utils.arrayToList(td.getRotation());
        tangoData.put(K_ROTATION, rotation);
        List<Double> translation = Utils.arrayToList(td.getTranslation());
        tangoData.put(K_TRANSLATION, translation);
        put(K_TANGO_DATA, tangoData);
    }

    private void setVisibility(Content c) {
        Visibility visibility = c.getVisibility();
        if (visibility == null) return;
        put(K_HAS_PREVIEW, visibility.isPreviewVisible());
        put(K_VISIBLE_UNTIL,  visibility.getVisibleUntil());
        put(K_RANGE, visibility.getRangeVisibility().getRange());
        put(K_PUBLICITY, visibility.getSocialVisibility().getMode());
    }

    private LatLng getLocation() {
        if (!containsKey(K_LOCATION)) return null;
        Map location = (Map) get(K_LOCATION);
        return new LatLng(
                Utils.toDouble(location.get(K_LAT)),
                Utils.toDouble(location.get(K_LNG))
        );
    }

    @SuppressWarnings("unchecked")
    private TangoData getTangoData() {
        if (!containsKey(K_TANGO_DATA)) return null;
        Map tangoData = (Map) get(K_TANGO_DATA);
        double[] rotation = Utils.listToArray((List<Double>) tangoData.get(K_ROTATION));
        double[] translation = Utils.listToArray((List<Double>) tangoData.get(K_TRANSLATION));
        return new TangoData()
                .withRotation(rotation)
                .withTranslation(translation)
                .withScale(Utils.toDouble(tangoData.get(K_SCALE)));
    }

    private Visibility getVisibility() {
        //noinspection WrongConstant
        return new Visibility()
                .withTimeVisibility((Date) get(K_VISIBLE_UNTIL))
                .withVisiblePreview((Boolean) get(K_HAS_PREVIEW))
                .withRangeVisibility(
                        new Visibility.RangeVisibility((int) (long) get(K_RANGE)))
                .withSocialVisibility(
                        new Visibility.SocialVisibility((int) (long) get(K_PUBLICITY)));
    }

    public void save(DatabaseReference ref) {
        if (id == null) {
            ref = ref.push();
            ref.setValue(this);
            id = ref.getKey();
        } else {
            ref.child(id).setValue(this);
        }
    }

    @SuppressWarnings("unused")
    public void save(DatabaseReference ref, final Callback<Boolean> statusCallback) {
        ref.push().setValue(this, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError firebaseError, DatabaseReference firebase) {
                if (firebaseError == null) {
                    id = firebase.getKey();
                }
                statusCallback.onResult(firebaseError == null);
            }
        });
    }

    public void delete(DatabaseReference ref) {
        ref.child(id).removeValue();
    }

    @SuppressWarnings("unused")
    public void delete(DatabaseReference ref, final Callback<Boolean> statusCallback) {
        ref.child(id).removeValue(new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError firebaseError, DatabaseReference firebase) {
                statusCallback.onResult(firebaseError == null);
            }
        });
    }

    public Content toContent() {
        return new Content()
                .withId(id)
                .withUuid((String) get(K_ROOM))
                .withNote((String) get(K_NOTE))
                .withTitle((String) get(K_TITLE))
                .withImageUri((String) get(K_IMGURI))
                .withAuthor(new User((String) get(K_AUTHOR)))  // TODO should be string not user
                .withLocation(getLocation())
                .withTangoData(getTangoData())
                .withVisibility(getVisibility());
    }

}