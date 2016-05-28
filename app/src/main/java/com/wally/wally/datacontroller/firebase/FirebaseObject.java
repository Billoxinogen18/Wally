package com.wally.wally.datacontroller.firebase;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.wally.wally.datacontroller.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseObject extends HashMap<String, Object> {
    private Map<String, FirebaseObject> children;

    @Exclude
    public String id;

    public FirebaseObject() {
        children = new HashMap<>();
    }

    public String getString(String key) {
        return containsKey(key) ? (String) get(key) : null;
    }

    public Integer getInteger(String key) {
        return containsKey(key) ? (int) (long) get(key) : null;
    }

    public Double getDouble(String key) {
        return containsKey(key) ? Utils.toDouble(get(key)) : null;
    }

    @SuppressWarnings("unchecked")
    protected List<Double> getList(String key) {
        return containsKey(key) ? (List<Double>) get(key) : null;
    }

    public Double[] getArray(String key) {
        return containsKey(key) ? Utils.listToArray(getList(key)) : null;
    }

    @Override
    public FirebaseObject put(String key, Object value) {
        if (value != null) {
            super.put(key, value);
        }
        return this;
    }

    public FirebaseObject putArray(String key, Double[] array) {
        return put(key, Utils.arrayToList(array));
    }


    @SuppressWarnings("unchecked")
    protected FirebaseObject getChild(String key) {
        if (children.containsKey(key))
            return children.get(key);

        FirebaseObject child = new FirebaseObject();
        if (!containsKey(key)) {
            put(key, child);
            children.put(key, child);
        } else {
            child.putAll((Map) get(key));
        }
        return child;
    }

    @Deprecated
    public boolean hasChild(String key) {
        return containsKey(key);
    }

    public void save(DatabaseReference ref) {
        id = FirebaseUtils.save(ref, this);
    }

    public void delete(DatabaseReference ref) {
        id = FirebaseUtils.delete(ref, this);
    }

}