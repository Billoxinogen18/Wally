package com.wally.wally.datacontroller.user;

public class User {
    private String id;
    private String ggId;

    public User(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getGgId() {
        return ggId;
    }

    public User withGgId(String ggId) {
        this.ggId = ggId;
        return this;
    }
}