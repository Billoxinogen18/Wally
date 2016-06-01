package com.wally.wally.datacontroller.firebase;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;

import java.util.HashMap;

public class FirebaseObject extends HashMap<String, Object> {
    private HashMap<String, FirebaseObject> children;

    @Exclude
    public String id;

    public FirebaseObject() {
        children = new HashMap<>();
    }

    @Override
    public FirebaseObject put(String key, Object value) {
        super.put(key, new FirebaseField(value).getValue());
        return this;
    }

    public FirebaseField get(String key) {
        return new FirebaseField(super.get(key));
    }

    public FirebaseObject getChild(String key) {
        if (!children.containsKey(key))
            children.put(key, new FirebaseObject());
        return children.get(key);
    }

    public void save(DatabaseReference ref) {
        id = FirebaseDAL.save(ref, this);
    }

    public void delete(DatabaseReference ref) {
        id = FirebaseDAL.delete(ref, this);
    }

}