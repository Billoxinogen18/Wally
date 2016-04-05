package com.wally.wally.dal;

import com.google.android.gms.maps.model.LatLngBounds;

/**
 * Created by Xato on 4/4/2016.
 */
public class LatLngBoundsQuery implements Query {
    LatLngBounds bounds;
    public LatLngBoundsQuery(LatLngBounds bounds){
        this.bounds = bounds;
    }
}
