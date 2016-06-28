package com.wally.wally.datacontroller.adf;

import com.google.android.gms.maps.model.LatLng;
import com.wally.wally.datacontroller.utils.SerializableLatLng;

import java.io.Serializable;

/**
 * ADF data class that stores info about adf
 * Created by ioane5 on 6/25/16.
 */
public class AdfMetaData implements Serializable {

    private String name;
    private String uuid;
    private SerializableLatLng latLng;

    public AdfMetaData(String name, String uuid, SerializableLatLng latLng) {
        this.name = name;
        this.uuid = uuid;
        this.latLng = latLng;
    }

    public AdfMetaData(String name, String uuid, LatLng latLng) {
        this(name, uuid, SerializableLatLng.fromLatLng(latLng));
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public LatLng getLatLng() {
        return SerializableLatLng.toLatLng(latLng);
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = SerializableLatLng.fromLatLng(latLng);
    }

    public void setLatLng(SerializableLatLng latLng) {
        this.latLng = latLng;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AdfMetaData that = (AdfMetaData) o;

        return uuid.equals(that.uuid);

    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }

    @Override
    public String toString() {
        return "AdfMetaData{" +
                "name='" + name + '\'' +
                ", uuid='" + uuid + '\'' +
                ", latLng=" + latLng +
                '}';
    }
}
