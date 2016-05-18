package com.wally.wally.datacontroller.oldqueries;

import com.firebase.client.Firebase;
import com.firebase.client.Query;
import com.google.android.gms.maps.model.LatLngBounds;

@Deprecated
public class LatLngQuery extends FirebaseQuery {
    private LatLngBounds bounds;

    public LatLngQuery(LatLngBounds bounds) {
        if (bounds == null)
            throw new IllegalArgumentException("Provided bounds is null");
        this.bounds = bounds;
    }

    @Override
    public Query getTarget(Firebase root) {
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
