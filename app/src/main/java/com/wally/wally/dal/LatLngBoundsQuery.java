package com.wally.wally.dal;

import com.google.android.gms.maps.model.LatLngBounds;

public class LatLngBoundsQuery implements Query {
    LatLngBounds bounds;
    public LatLngBoundsQuery(LatLngBounds bounds){
        this.bounds = bounds;
    }
}
