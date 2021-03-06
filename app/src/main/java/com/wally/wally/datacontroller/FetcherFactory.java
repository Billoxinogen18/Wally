package com.wally.wally.datacontroller;

import com.google.firebase.database.DatabaseReference;
import com.wally.wally.objects.content.Content;
import com.wally.wally.datacontroller.fetchers.FilteredFetcher;
import com.wally.wally.datacontroller.fetchers.KeyPager;
import com.wally.wally.datacontroller.fetchers.PagerChain;
import com.wally.wally.datacontroller.fetchers.QueryContentFetcher;
import com.wally.wally.datacontroller.fetchers.ValuePager;
import com.wally.wally.datacontroller.firebase.geofire.GeoHashQuery;
import com.wally.wally.datacontroller.firebase.geofire.GeoUtils;
import com.wally.wally.datacontroller.queries.AuthorQuery;
import com.wally.wally.datacontroller.queries.ContentQuery;
import com.wally.wally.datacontroller.queries.FirebaseQuery;
import com.wally.wally.datacontroller.queries.SharedWithQuery;
import com.wally.wally.datacontroller.user.User;
import com.wally.wally.datacontroller.utils.Predicate;
import com.wally.wally.datacontroller.DBController.*;

import java.util.Collections;
import java.util.Set;

class FetcherFactory {
    private static final double RADIUS_MAX_KM = 1000.0;
    private DatabaseReference contents;
    private DatabaseReference publicContents;
    private DatabaseReference sharedContents;


    FetcherFactory(DatabaseReference contents) {
        this.contents = contents;
        this.publicContents = contents.child("Public");
        this.sharedContents = contents.child("Shared");
    }

    Fetcher createForPrivate(User current) {
        return new KeyPager(contents.child(current.getId().getId()));
    }

    Fetcher createForPublic(User user) {
        FirebaseQuery authorQuery = new AuthorQuery(user.getId());
        ContentQuery query = new ContentQuery(authorQuery, publicContents);
        return new QueryContentFetcher(query);
    }

    Fetcher createForPublic(double latitude, double longitude,double radiusKm) {
        Fetcher fetcher = createForLocation(latitude, longitude, radiusKm, publicContents);
        if (fetcher == null) { fetcher = new KeyPager(publicContents); }
        return fetcher;
    }

    Fetcher createForSharedByMe(User current) {
        FirebaseQuery authorQuery = new AuthorQuery(current.getId());
        ContentQuery query = new ContentQuery(authorQuery, sharedContents);
        return new QueryContentFetcher(query);
    }

    Fetcher createForSharedWithMe(User current, double latitude, double longitude, double radiusKm) {
        FirebaseQuery sharedWithQuery = new SharedWithQuery(current.getGgId());
        Predicate<Content> predicate = isLocationInRangePredicate(latitude, longitude, radiusKm);
        ContentQuery query = new ContentQuery(sharedWithQuery, sharedContents, predicate);
        return new QueryContentFetcher(query);
    }

    Fetcher createForSharedWithMe(User current, User other) {
        Fetcher sharedContentFetcher = createForSharedWithMe(current);
        Predicate<Content> hasAuthorPredicate = hasAuthorPredicate(other.getId().getId());
        return new FilteredFetcher(sharedContentFetcher, hasAuthorPredicate);
    }

    private Fetcher createForSharedWithMe(User current) {
        FirebaseQuery sharedWithQuery = new SharedWithQuery(current.getGgId());
        ContentQuery query = new ContentQuery(sharedWithQuery, sharedContents);
        return new QueryContentFetcher(query);
    }

    private Fetcher createForLocation(double latitude, double longitude,
                                      double radiusKm, DatabaseReference target) {
        // We decided that too big radius (>2500 km)
        // means we don't need to filter by location
        if (radiusKm > RADIUS_MAX_KM) { return null; }
        if (radiusKm <= 0) { return createTrivial(); }

        final double radius = radiusKm * 1000; // Convert to meters
        Set<GeoHashQuery> queries = GeoHashQuery.queriesAtLocation(latitude, longitude, radius);
        PagerChain chain = new PagerChain();
        for (GeoHashQuery query : queries) {
            String startKey = query.getStartValue();
            String endKey = query.getEndValue();
            chain.addPager(new ValuePager(target, "hash", startKey, endKey));
        }
        return new FilteredFetcher(chain, isLocationInRangePredicate(latitude, longitude, radiusKm));
    }

    private Fetcher createTrivial() {
        return new Fetcher() {
            @Override
            public void fetchNext(int i, ResultCallback callback) {
                callback.onResult(Collections.<Content>emptySet());
            }
        };
    }

    private Predicate<Content> isLocationInRangePredicate(
            final double latitude, final double longitude, final double radiusKm) {
        return new Predicate<Content>() {
            private double radius = radiusKm * 1000;

            @Override
            public boolean test(Content target) {
                double distance = GeoUtils.distance(
                        target.getLocation().getLatitude(),
                        target.getLocation().getLongitude(),
                        latitude, longitude);
                return distance < radius;
            }
        };
    }

    private Predicate<Content> hasAuthorPredicate(final String userId) {
        return new Predicate<Content>() {
            @Override
            public boolean test(Content target) {
                return userId.equals(target.getAuthorId());
            }
        };
    }
}
