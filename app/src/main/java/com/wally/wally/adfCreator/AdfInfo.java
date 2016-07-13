package com.wally.wally.adfCreator;


import com.wally.wally.datacontroller.adf.AdfMetaData;

import java.io.Serializable;

public class AdfInfo implements Serializable {
    private String path;
    private String uuid;
    private AdfMetaData adfMetaData;

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

    public AdfInfo withMetaData(AdfMetaData adfMetaData) {
        this.adfMetaData = adfMetaData;
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

    public AdfMetaData getMetaData() {
        return adfMetaData;
    }
}
