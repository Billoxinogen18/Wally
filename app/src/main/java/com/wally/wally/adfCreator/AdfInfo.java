package com.wally.wally.adfCreator;


public class AdfInfo {
    private String path;
    private String uuid;

    public AdfInfo() {

    }

    public AdfInfo(AdfInfo other) {
        this.path = other.path;
        this.uuid = other.uuid;
    }

    public AdfInfo withUuid(String uuid) {
        this.uuid = uuid;
        return this;
    }

    public AdfInfo withPath(String path) {
        this.path = path;
        return this;
    }

    public String getPath(){
        return path;
    }

    public String getUuid(){
        return uuid;
    }

    public boolean isImported(){
        return true;
    }

}
