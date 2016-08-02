package com.wally.wally.datacontroller;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.wally.wally.adf.AdfService;
import com.wally.wally.datacontroller.callbacks.AggregatorCallback;
import com.wally.wally.datacontroller.callbacks.Callback;
import com.wally.wally.datacontroller.callbacks.FetchResultCallback;
import com.wally.wally.datacontroller.content.Content;
import com.wally.wally.datacontroller.content.ContentManager;
import com.wally.wally.datacontroller.fetchers.ContentFetcher;
import com.wally.wally.datacontroller.fetchers.PagerChain;
import com.wally.wally.datacontroller.user.User;
import com.wally.wally.datacontroller.user.UserManager;
import com.wally.wally.datacontroller.utils.SerializableLatLng;

import java.util.Collections;
import java.util.Map;

public class DataController {
    public static final String TAG = DataController.class.getSimpleName();
    private static DataController instance;

    private StorageReference storage;
    private FirebaseAdfService adfService;
    private DatabaseReference rooms;
    private UserManager userManager;
    private ContentManager contentManager;

    private ContentFetcherFactory fetcherFactory;

    public DataController withContentManager(ContentManager manager) {
        this.contentManager = manager;
        return this;
    }

    public DataController withUserManager(UserManager manager) {
        this.userManager = manager;
        return this;
    }

    public DataController withFetcherFactory(ContentFetcherFactory factory) {
        this.fetcherFactory = factory;
        return this;
    }

    public DataController(DatabaseReference database, StorageReference storage) {
        this.storage = storage;
        rooms = database.child(Config.ROOMS_NODE);
    }

    public static DataController create() {
        if (instance == null) {
            DatabaseReference root = FirebaseDatabase.getInstance()
                    .getReference().child(Config.DATABASE_ROOT);
            StorageReference storage = FirebaseStorage.getInstance()
                    .getReference().child(Config.STORAGE_ROOT);

            ContentManager cManager = new ContentManager(
                    root.child(Config.ROOMS_NODE),
                    root.child(Config.CONTENTS_NODE),
                    storage
            );

            ContentFetcherFactory fFactory = new ContentFetcherFactory(
                    root.child(Config.CONTENTS_NODE)
            );

            instance = new DataController(
                    FirebaseDatabase.getInstance().getReference().child(Config.DATABASE_ROOT),
                    FirebaseStorage.getInstance().getReference().child(Config.STORAGE_ROOT))
                    .withUserManager(createUserManager())
                    .withContentManager(cManager).withFetcherFactory(fFactory);
        }
        return instance;
    }

    public void save(final Content c) {
        contentManager.save(c);
    }

    public void delete(Content c) {
        contentManager.delete(c);
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
                    contentManager.fetchAt(key.replace(':', '/'), aggregator);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onError(databaseError.toException());
            }
        });
    }

    public ContentFetcher createFetcherForMyContent() {
        User current = getCurrentUser();
        PagerChain chain = new PagerChain();
        chain.addPager(fetcherFactory.createForPrivate(current));
        chain.addPager(fetcherFactory.createForSharedByMe(current));
        chain.addPager(fetcherFactory.createForPublic(current));
        return chain;
    }

    public ContentFetcher createFetcherForVisibleContent(SerializableLatLng center, double radiusKm) {
        PagerChain chain = new PagerChain();
        chain.addPager(fetcherFactory.createForSharedWithMe(getCurrentUser(), center, radiusKm));
        chain.addPager(fetcherFactory.createForPublic(center, radiusKm));
        return chain;
    }

    public ContentFetcher createFetcherForUserContent(User user) {
        PagerChain chain = new PagerChain();
        chain.addPager(fetcherFactory.createForSharedWithMe(getCurrentUser(), user));
        chain.addPager(fetcherFactory.createForPublic(user));
        return chain;
    }

    public User getCurrentUser() {
        return userManager.getCurrentUser();
    }

    public void fetchUser(String id, Callback<User> callback) {
        userManager.fetchUser(id, callback);
    }

    public AdfService getADFService() {
        if (adfService == null) {
            adfService = new FirebaseAdfService(rooms, storage);
        }
        return adfService;
    }

    public static UserManager createUserManager() {
        return new UserManager(
                FirebaseAuth.getInstance(),
                FirebaseDatabase.getInstance()
                        .getReference().child(Config.USERS_NODE)
        );
    }
}