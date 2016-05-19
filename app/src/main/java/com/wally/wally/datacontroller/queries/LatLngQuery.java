package com.wally.wally.datacontroller.queries;

import com.google.android.gms.maps.model.LatLngBounds;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

public class LatLngQuery extends FirebaseQuery {
    private LatLngBounds bounds;

    public LatLngQuery(LatLngBounds bounds) {
        if (bounds == null)
            throw new IllegalArgumentException("Provided bounds is null");
        this.bounds = bounds;
    }

    @Override
    public Query getTarget(DatabaseReference root) {
        return root
                .orderByChild("location/latitude")
                .startAt(bounds.southwest.latitude)
                .endAt(bounds.northeast.latitude)
                .getRef() // Cant't call orderByChild twice on the same reference
                .orderByChild("location/longitude")
                .startAt(bounds.southwest.longitude)
                .endAt(bounds.northeast.longitude);
    }
}