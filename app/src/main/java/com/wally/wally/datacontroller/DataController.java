package com.wally.wally.datacontroller;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.wally.wally.adf.AdfService;
import com.wally.wally.datacontroller.callbacks.Callback;
import com.wally.wally.datacontroller.callbacks.FetchResultCallback;
import com.wally.wally.datacontroller.content.Content;
import com.wally.wally.datacontroller.content.ContentManager;
import com.wally.wally.datacontroller.fetchers.ContentFetcher;
import com.wally.wally.datacontroller.fetchers.PagerChain;
import com.wally.wally.datacontroller.user.User;
import com.wally.wally.datacontroller.user.UserManager;
import com.wally.wally.datacontroller.utils.SerializableLatLng;

public class DataController {
    public static final String TAG = DataController.class.getSimpleName();
    private static DataController instance;
    private static AdfService adfServiceInstance;
    private static UserManager userManagerInstance;

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

    public static DataController getInstance() {
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

            instance = new DataController()
                    .withUserManager(getUserManagerInstance())
                    .withContentManager(cManager)
                    .withFetcherFactory(fFactory);
        }
        return instance;
    }

    public void save(Content c) {
        contentManager.save(c);
    }

    public void delete(Content c) {
        contentManager.delete(c);
    }

    public void fetchForUuid(String uuid, FetchResultCallback callback) {
        contentManager.fetchForUuid(uuid, callback);
    }

    public ContentFetcher createFetcherForMyContent() {
        User current = userManager.getCurrentUser();
        PagerChain chain = new PagerChain();
        chain.addPager(fetcherFactory.createForPrivate(current));
        chain.addPager(fetcherFactory.createForSharedByMe(current));
        chain.addPager(fetcherFactory.createForPublic(current));
        return chain;
    }

    public ContentFetcher createFetcherForVisibleContent(SerializableLatLng center, double radiusKm) {
        PagerChain chain = new PagerChain();
        chain.addPager(fetcherFactory.createForSharedWithMe(userManager.getCurrentUser(), center, radiusKm));
        chain.addPager(fetcherFactory.createForPublic(center, radiusKm));
        return chain;
    }

    public ContentFetcher createFetcherForUserContent(User user) {
        PagerChain chain = new PagerChain();
        chain.addPager(fetcherFactory.createForSharedWithMe(userManager.getCurrentUser(), user));
        chain.addPager(fetcherFactory.createForPublic(user));
        return chain;
    }

    public void fetchUser(String id, Callback<User> callback) {
        userManager.fetchUser(id, callback);
    }

    public static AdfService getAdfServiceInstance() {
        if (adfServiceInstance == null) {
            adfServiceInstance = new FirebaseAdfService(
                    FirebaseDatabase.getInstance().getReference()
                            .child(Config.DATABASE_ROOT).child(Config.ROOMS_NODE),
                    FirebaseStorage.getInstance().getReference().child(Config.STORAGE_ROOT));
        }
        return adfServiceInstance;
    }

    public static UserManager getUserManagerInstance() {
        if (userManagerInstance == null) {
            userManagerInstance = new UserManager(
                    FirebaseAuth.getInstance(),
                    FirebaseDatabase.getInstance()
                            .getReference().child(Config.USERS_NODE)
            );
        }
        return userManagerInstance;
    }
}