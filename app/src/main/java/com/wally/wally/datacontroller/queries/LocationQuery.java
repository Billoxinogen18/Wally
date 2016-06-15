package com.wally.wally.datacontroller.queries;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.wally.wally.datacontroller.content.FirebaseContent;
import com.wally.wally.datacontroller.firebase.geofire.GeoHashQuery;

public class LocationQuery extends FirebaseQuery {
    private GeoHashQuery query;

    public LocationQuery(GeoHashQuery query) {
        this.query = query;
    }


    @Override
    public Query getTarget(DatabaseReference ref) {
        return ref.orderByChild(FirebaseContent.K_LOCATION + "/" + FirebaseContent.K_HASH)
                .startAt(query.getStartValue())
                .endAt(query.getEndValue());
    }


}
