package com.wally.wally.adfCreator;


import com.wally.wally.datacontroller.adf.AdfMetaData;

import java.io.Serializable;

public class AdfInfo implements Serializable {
    private String path;
    private String uuid;
    private AdfMetaData adfMetaData;
    private boolean isUploaded;

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

    public AdfInfo withUploaded(boolean uploaded) {
        this.isUploaded = uploaded;
        return this;
    }

    public String getPath() {
        return path;
    }

    public String getUuid() {
        return uuid;
    }

    public boolean isImported() {
        return true;
    }

    public AdfMetaData getMetaData() {
        return adfMetaData;
    }

    public boolean isUploaded() {
        return isUploaded;
    }

    @Override
    public String toString() {
        return "path: " + path + "; " +
                "uuid: " + uuid + "; " +
                "AdfMetaData: " + adfMetaData + ".";
    }
}
