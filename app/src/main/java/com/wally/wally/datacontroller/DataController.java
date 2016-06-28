package com.wally.wally.datacontroller;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.wally.wally.datacontroller.adf.ADFService;
import com.wally.wally.datacontroller.adf.FirebaseADFService;
import com.wally.wally.datacontroller.callbacks.AggregatorCallback;
import com.wally.wally.datacontroller.callbacks.Callback;
import com.wally.wally.datacontroller.callbacks.FetchResultCallback;
import com.wally.wally.datacontroller.content.Content;
import com.wally.wally.datacontroller.content.FirebaseContent;
import com.wally.wally.datacontroller.fetchers.ContentFetcher;
import com.wally.wally.datacontroller.fetchers.FilteredFetcher;
import com.wally.wally.datacontroller.fetchers.KeyPager;
import com.wally.wally.datacontroller.fetchers.PagerChain;
import com.wally.wally.datacontroller.fetchers.QueryContentFetcher;
import com.wally.wally.datacontroller.fetchers.ValuePager;
import com.wally.wally.datacontroller.firebase.FirebaseDAL;
import com.wally.wally.datacontroller.firebase.geofire.GeoHashQuery;
import com.wally.wally.datacontroller.firebase.geofire.GeoUtils;
import com.wally.wally.datacontroller.queries.AuthorQuery;
import com.wally.wally.datacontroller.queries.ContentQuery;
import com.wally.wally.datacontroller.queries.FirebaseQuery;
import com.wally.wally.datacontroller.queries.SharedWithQuery;
import com.wally.wally.datacontroller.user.User;
import com.wally.wally.datacontroller.utils.Predicate;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class DataController {
    public static final String TAG = DataController.class.getSimpleName();

    private static DataController instance;

    private User currentUser;
    private StorageReference storage;
    private FirebaseADFService adfService;
    private DatabaseReference users, contents, rooms;

    private DataController(DatabaseReference database, StorageReference storage) {
        this.storage = storage;
        users = database.child(Config.USERS_NODE);
        contents = database.child(Config.CONTENTS_NODE);
        rooms = database.child(Config.ROOMS_NODE);

        // Debug calls will be deleted in the end
//        DebugUtils.refreshContents(contents.getParent(), this);
        DebugUtils.sanityCheck(this);
    }

    public static DataController create() {
        if (instance == null) {
            instance = new DataController(
                    FirebaseDatabase.getInstance().getReference().child(Config.DATABASE_ROOT),
                    FirebaseStorage.getInstance().getReference().child(Config.STORAGE_ROOT)
            );
        }
        return instance;
    }

    private void uploadImage(String imagePath, String folder, final Callback<String> callback) {
        if (imagePath != null && imagePath.startsWith(Content.UPLOAD_URI_PREFIX)) {
            String imgUriString = imagePath.substring(Content.UPLOAD_URI_PREFIX.length());
            FirebaseDAL.uploadFile(storage.child(folder), imgUriString, callback);
        } else {
            callback.onResult(imagePath);
        }
    }

    private DatabaseReference getContentReference(Content c) {
        DatabaseReference target;
        if (c.isPublic()) {
            target = contents.child("Public");
        } else if (c.isPrivate()) {
            target = contents.child(c.getAuthorId());
        } else {
            target = contents.child("Shared");
        }
        return c.getId() != null ? target.child(c.getId()) : target.push();
    }

    private void addInRoom(String uuid, String id) {
        rooms.child(uuid).child("Contents").child(id).setValue(true);
    }

    public void save(final Content c) {
        if (c.getId() != null) { delete(c); }
        final DatabaseReference ref = getContentReference(c);
        c.withId(ref.getKey());
        uploadImage(
                c.getImageUri(),
                ref.getKey(),
                new Callback<String>() {
                    @Override
                    public void onResult(String result) {
                        c.withImageUri(result);
                        new FirebaseContent(c).save(ref);
                        if (c.getUuid() != null) {
                            String extendedId = ref.getParent().getKey() + ":" + ref.getKey();
                            addInRoom(c.getUuid(), extendedId);
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        // Omitted Implementation:
                        // If we are here image upload failed somehow
                        // We decided to leave this case for now!
                    }
                }
        );
    }

    public void delete(Content c) {
        if (c.getId() == null) {
            throw new IllegalArgumentException("Id of the content is null");
        }
        String extendedPublicId = "Public:" + c.getId();
        String extendedSharedId = "Shared:" + c.getId();
        String extendedPrivateId = c.getAuthorId() + ":" + c.getId();
        rooms.child(c.getUuid()).child(extendedPublicId).removeValue();
        rooms.child(c.getUuid()).child(extendedSharedId).removeValue();
        rooms.child(c.getUuid()).child(extendedPrivateId).removeValue();
        // TODO rewrite using .replace(':', '/') calls
        contents.child("Public").child(c.getId()).removeValue();
        contents.child("Shared").child(c.getId()).removeValue();
        contents.child(c.getAuthorId()).child(c.getId()).removeValue();
    }

    public void fetchByUUID(String uuid, final FetchResultCallback callback) {
        rooms.child(uuid).child("Contents")
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                GenericTypeIndicator<Map<String, Boolean>> indicator =
                        new GenericTypeIndicator<Map<String, Boolean>>(){};
                Map<String, Boolean> extendedIds = dataSnapshot.getValue(indicator);
                if (extendedIds == null) {
                    callback.onResult(Collections.<Content>emptySet());
                    return;
                }
                AggregatorCallback aggregator = new AggregatorCallback(callback)
                        .withExpectedCallbacks(extendedIds.size());
                for (String key : extendedIds.keySet()) {
                    fetchContentAt(key.replace(':', '/'), contents, aggregator);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onError(databaseError.toException());
            }
        });
    }

    private ContentFetcher createFetcherForPublicContent(LatLng center, double radiusKm) {
        DatabaseReference target = contents.child("Public");
        ContentFetcher fetcher = createFetcherForLocation(center, radiusKm, target);
        if (fetcher == null) { fetcher = new KeyPager(target); }
        return fetcher;
    }

    private ContentFetcher createFetcherForPublicContent(User user, LatLng center, double radiusKm) {
        FirebaseQuery authorQuery = new AuthorQuery(user.getId());
        Predicate<Content> predicate = isLocationInRangePredicate(center, radiusKm);
        ContentQuery query = new ContentQuery(authorQuery, contents.child("Public"), predicate);
        return new QueryContentFetcher(query);
    }

    private ContentFetcher createFetcherForPublicContent(User user) {
        FirebaseQuery authorQuery = new AuthorQuery(user.getId());
        ContentQuery query = new ContentQuery(authorQuery, contents.child("Public"));
        return new QueryContentFetcher(query);
    }

    private ContentFetcher createFetcherForMySharedContent(LatLng center, double radiusKm) {
        User current = getCurrentUser();
        FirebaseQuery authorQuery = new AuthorQuery(current.getId());
        Predicate<Content> predicate = isLocationInRangePredicate(center, radiusKm);
        ContentQuery query = new ContentQuery(authorQuery, contents.child("Shared"), predicate);
        return new QueryContentFetcher(query);
    }

    private ContentFetcher createFetcherForMySharedContent() {
        User current = getCurrentUser();
        FirebaseQuery authorQuery = new AuthorQuery(current.getId());
        ContentQuery query = new ContentQuery(authorQuery, contents.child("Shared"));
        return new QueryContentFetcher(query);
    }

    private ContentFetcher createFetcherForPrivateContent(LatLng center, double radiusKm) {
        User current = getCurrentUser();
        DatabaseReference target = contents.child(current.getId().getId());
        ContentFetcher fetcher = createFetcherForLocation(center, radiusKm, target);
        if (fetcher == null) {
            fetcher = new KeyPager(target);
        }
        return fetcher;
    }

    private ContentFetcher createFetcherForPrivateContent() {
        User current = getCurrentUser();
        DatabaseReference target = contents.child(current.getId().getId());
        return new KeyPager(target);
    }


    private ContentFetcher createFetcherForContentSharedWithMe(LatLng center, double radiusKm) {
        User current = getCurrentUser();
        FirebaseQuery sharedWithQuery = new SharedWithQuery(current.getGgId());
        Predicate<Content> predicate = isLocationInRangePredicate(center, radiusKm);
        ContentQuery query = new ContentQuery(sharedWithQuery, contents.child("Shared"),predicate);
        return new QueryContentFetcher(query);
    }

    private ContentFetcher createFetcherForContentSharedWithMe() {
        User current = getCurrentUser();
        FirebaseQuery sharedWithQuery = new SharedWithQuery(current.getGgId());
        ContentQuery query = new ContentQuery(sharedWithQuery, contents.child("Shared"));
        return new QueryContentFetcher(query);
    }

    public ContentFetcher createFetcherForMyContent(LatLng center, double radiusKm) {
        User current = getCurrentUser();
        PagerChain chain = new PagerChain();
        chain.addPager(createFetcherForPrivateContent(center, radiusKm));
        chain.addPager(createFetcherForMySharedContent(center, radiusKm));
        chain.addPager(createFetcherForPublicContent(current, center, radiusKm));
        return chain;
    }

    public ContentFetcher createFetcherForMyContent() {
        User current = getCurrentUser();
        PagerChain chain = new PagerChain();
        chain.addPager(createFetcherForPrivateContent());
        chain.addPager(createFetcherForMySharedContent());
        chain.addPager(createFetcherForPublicContent(current));
        return chain;
    }

    public ContentFetcher createFetcherForVisibleContent(LatLng center, double radiusKm) {
        PagerChain chain = new PagerChain();
        chain.addPager(createFetcherForPublicContent(center, radiusKm));
        chain.addPager(createFetcherForContentSharedWithMe(center, radiusKm));
        return chain;
    }

    public ContentFetcher createFetcherForUserContent(User user, LatLng center, double radiusKm) {
        User current = getCurrentUser();
        PagerChain chain = new PagerChain();
        ContentFetcher sharedContentFetcher = createFetcherForContentSharedWithMe(center, radiusKm);
        Predicate<Content> hasAuthorPredicate = hasAuthorPredicate(user.getId().getId());
        sharedContentFetcher = new FilteredFetcher(sharedContentFetcher, hasAuthorPredicate);
        chain.addPager(sharedContentFetcher);
        chain.addPager(createFetcherForPublicContent(user, center, radiusKm));
        return chain;
    }

    public ContentFetcher createFetcherForUserContent(User user) {
        User current = getCurrentUser();
        PagerChain chain = new PagerChain();
        ContentFetcher sharedContentFetcher = createFetcherForContentSharedWithMe();
        Predicate<Content> hasAuthorPredicate = hasAuthorPredicate(user.getId().getId());
        sharedContentFetcher = new FilteredFetcher(sharedContentFetcher, hasAuthorPredicate);
        chain.addPager(sharedContentFetcher);
        chain.addPager(createFetcherForPublicContent(user));
        return chain;
    }

    public User getCurrentUser() {
        if (currentUser != null) return currentUser;
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return null;
        String id = user.getUid();
        // .get(1) assumes only one provider (Google)
        String ggId = user.getProviderData().get(1).getUid();
        currentUser = new User(id).withGgId(ggId);
        users.child(id).setValue(currentUser);
        return currentUser;
    }

    public void fetchUser(String id, final Callback<User> callback) {
        users.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                callback.onResult(dataSnapshot.getValue(User.class));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onError(databaseError.toException());
            }
        });
    }

    private void fetchContentAt(String path, DatabaseReference ref,
                                final FetchResultCallback callback) {
        ref.child(path).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Content content = FirebaseQuery.firebaseContentFromSnapshot(dataSnapshot).toContent();
                callback.onResult(Collections.singleton(content));

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onError(databaseError.toException());
            }
        });
    }

    private ContentFetcher createFetcherForLocation(LatLng center,
                                                    double radiusKm,
                                                    DatabaseReference target) {
        if (radiusKm > Config.RADIUS_MAX_KM) {
            // We decided that too big radius (>2500 km)
            // means we don't need to filter by location
            return null;
        }

        if (radiusKm <= 0) {
            return new ContentFetcher() {
                @Override
                public void fetchPrev(int i, FetchResultCallback callback) {
                    callback.onResult(Collections.<Content>emptySet());
                }

                @Override
                public void fetchNext(int i, FetchResultCallback callback) {
                    callback.onResult(Collections.<Content>emptySet());
                }
            };
        }

        final double radius = radiusKm * 1000; // Convert to meters
        Set<GeoHashQuery> queries = GeoHashQuery.queriesAtLocation(center, radius);
        PagerChain chain = new PagerChain();
        for (GeoHashQuery query : queries) {
            String startKey = query.getStartValue();
            String endKey = query.getEndValue();
            chain.addPager(new ValuePager(target, "hash", startKey, endKey));
        }
        return new FilteredFetcher(chain, isLocationInRangePredicate(center, radiusKm));
    }

    private Predicate<Content> isLocationInRangePredicate(final LatLng center,
                                                          final double radiusKm) {
        return new Predicate<Content>() {
            private double radius = radiusKm * 1000;

            @Override
            public boolean test(Content target) {
                return GeoUtils.distance(target.getLocation(), center) < radius;
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

    public ADFService getADFService() {
        if (adfService == null) {
            adfService = new FirebaseADFService(rooms, storage);
        }
        return adfService;
    }
}