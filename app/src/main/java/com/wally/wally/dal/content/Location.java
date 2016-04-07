package com.wally.wally.dal.content;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Xato on 4/7/2016.
 */
public class Location {
    private double latitude;
    private double longitude;
    public Location(){

    }

    public Location(double latitude, double longitude){
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    @JsonIgnore
    public LatLng getLatLng(){
        return new LatLng(latitude, longitude);
    }
}
