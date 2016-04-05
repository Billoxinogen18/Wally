package com.wally.wally.dal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.android.gms.maps.model.LatLng;

public class Content {
    private double latitude;
    private double longitude;

    public Content(LatLng latlng) {
        this.latitude = latlng.latitude;
        this.longitude = latlng.longitude;
    }

    public Content() {
        this(new LatLng(0, 0));
    }

    @JsonIgnore
    public LatLng getLatlng() {
        return new LatLng(latitude, longitude);
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Content content = (Content) o;

        return latitude == content.latitude && longitude == content.longitude;

    }

    @Override
    public int hashCode() {
        return (int)Math.round(latitude + longitude);
    }

    @Override
    public String toString() {
        return "I am what I am";
    }
}
