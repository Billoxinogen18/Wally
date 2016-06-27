package com.wally.wally.datacontroller.utils;

import android.support.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;

/**
 * Same LatLng class but Serializable
 * Created by ioane5 on 6/25/16.
 */
public class SerializableLatLng implements Serializable {
    private double lat;
    private double lng;

    public SerializableLatLng(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
    }

    public static
    @Nullable
    LatLng toLatLng(@Nullable SerializableLatLng ll) {
        if (ll == null) {
            return null;
        }
        return new LatLng(ll.lat, ll.lng);
    }

    public static
    @Nullable
    SerializableLatLng fromLatLng(@Nullable LatLng ll) {
        if (ll == null) {
            return null;
        }
        return new SerializableLatLng(ll.latitude, ll.longitude);
    }

    @Override
    public String toString() {
        return "{" + "lat=" + lat + ", lng=" + lng + "}";
    }
}
