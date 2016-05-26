package com.wally.wally.datacontroller.user;

import java.io.Serializable;

public class User implements Serializable {
    private Id id;
    private Id ggId;
    private Id fbId;

    public User() {}

    public User(String id) {
        this.id = new Id(Id.PROVIDER_FIREBASE, id);
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

    public User withGgId(String ggId) {
        this.ggId = new Id(Id.PROVIDER_GOOGLE, ggId);
        return this;
    }

    // will or will not be used in future
    @SuppressWarnings("unused")
    public User withFbId(String fbId) {
        this.fbId = new Id(Id.PROVIDER_FACEBOOK, fbId);
        return this;
    }
}