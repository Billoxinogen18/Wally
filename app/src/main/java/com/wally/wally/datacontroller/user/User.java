package com.wally.wally.datacontroller.user;

import java.io.Serializable;

public class User implements Serializable {
    private Id id;
    private Id ggId;
    private Id fbId;

    public User() {}

    public User(Id id) {
        this.id = id;
    }

    public Id getId() {
        return id;
    }

    public Id getGgId() {
        return ggId;
    }

    public Id getFbId() {
        return fbId;
    }

    public User withGgId(Id ggId) {
        this.ggId = ggId;
        return this;
    }

    // will or will not be used in future
    @SuppressWarnings("unused")
    public User withFbId(Id fbId) {
        this.fbId = fbId;
        return this;
    }
}