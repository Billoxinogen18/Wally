package com.wally.wally.adf;

import com.wally.wally.datacontroller.utils.SerializableLatLng;

import java.io.Serializable;

public class AdfInfo implements Serializable {
    private String name;
    private String path;
    private String uuid;
    private boolean isImported;
    private boolean isUploaded;
    private SerializableLatLng creationLocation;

    public AdfInfo withName(String name) {
        this.name = name;
        return this;
    }

    public AdfInfo withUuid(String uuid) {
        this.uuid = uuid;
        return this;
    }

    public AdfInfo withPath(String path) {
        this.path = path;
        return this;
    }

    public AdfInfo withUploadedStatus(boolean status) {
        this.isUploaded = status;
        return this;
    }

    @SuppressWarnings("unused")
    public AdfInfo withImportedStatus(boolean status) {
        this.isImported = status;
        return this;
    }

    public AdfInfo withCreationLocation(SerializableLatLng location) {
        this.creationLocation = location;
        return this;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public String getUuid() {
        return uuid;
    }

    @SuppressWarnings("unused")
    public boolean isImported() {
        return isImported;
    }

    public boolean isUploaded() {
        return isUploaded;
    }

    public SerializableLatLng getCreationLocation() {
        return creationLocation;
    }

    @Override
    public String toString() {
        return "path: " + path + "; " +
                "uuid: " + uuid + "; ";
    }
}