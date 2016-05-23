package com.wally.wally.datacontroller.user;

import java.io.Serializable;

public class User implements Serializable {
    private String id;
    private String ggId;
    private String fbId;

    public User() {}

    public User(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getGgId() {
        return ggId;
    }

    public String getFbId() {
        return fbId;
    }

    public User withGgId(String ggId) {
        this.ggId = ggId;
        return this;
    }

    // will or will not be used in future
    @SuppressWarnings("unused")
    public User withFbId(String fbId) {
        this.fbId = fbId;
        return this;
    }
}