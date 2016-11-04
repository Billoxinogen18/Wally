package com.wally.wally.objects.content;

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

    public double getLatitude(){
        return lat;
    }

    public double getLongitude(){
        return lng;
    }

    @Override
    public String toString() {
        return "{" + "lat=" + lat + ", lng=" + lng + "}";
    }
}
