package com.wally.wally.datacontroller.firebase;

import com.firebase.client.Firebase;
import com.firebase.client.Query;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

public class FirebaseQuery {
    private String uuid;
    private LatLngBounds bounds;

    public FirebaseQuery withUuid(String uuid) {
        if (bounds != null) {
            throw new UnsupportedOperationException("Can't query uuid and bounds together");
        }
        this.uuid = uuid;
        return this;
    }

    public FirebaseQuery withBounds(LatLngBounds bounds) {
        if (uuid != null) {
            throw new UnsupportedOperationException("Can't query bounds and uuid together");
        }
        this.bounds = bounds;
        return this;
    }

    public Query getTarget(Firebase fb) {
        Query query = fb;

        if (uuid != null) {
            query =  query.orderByChild("uuid").equalTo(uuid);
        }

        if (bounds != null) {
            LatLng bottomLeft = bounds.southwest;
            LatLng topRight = bounds.northeast;

            query = query
                    .orderByChild("location/latitude")
                    .startAt(bottomLeft.latitude)
                    .endAt(topRight.latitude)
                    .getRef()
                    .orderByChild("location/longitude")
                    .startAt(bottomLeft.longitude)
                    .endAt(topRight.longitude);
            // TODO needs revision
        }

        return query;
    }

}
