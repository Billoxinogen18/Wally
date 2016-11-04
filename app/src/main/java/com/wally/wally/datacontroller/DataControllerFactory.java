package com.wally.wally.datacontroller;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.wally.wally.adf.AdfService;
import com.wally.wally.datacontroller.user.UserManager;

public class DataControllerFactory {
    private static final String DATABASE_ROOT = "Develop";
    private static final String STORAGE_ROOT = DATABASE_ROOT;
    private static final String ROOMS_NODE = "Rooms";
    private static final String USERS_NODE = DATABASE_ROOT + "/Users";
    private static final String CONTENTS_NODE = "Contents";

    private static AdfService adfServiceInstance;
    private static UserManager userManagerInstance;
    private static DataController dataControllerInstance;

    public static DBController getDbController() {
        if (dataControllerInstance == null) {
            DatabaseReference root = FirebaseDatabase.getInstance()
                    .getReference().child(DATABASE_ROOT);
            StorageReference storage = FirebaseStorage.getInstance()
                    .getReference().child(STORAGE_ROOT);

            ContentManager cManager = new ContentManager(
                    root.child(ROOMS_NODE),
                    root.child(CONTENTS_NODE),
                    storage
            );

            FetcherFactory fFactory = new FetcherFactory(
                    root.child(CONTENTS_NODE)
            );

            dataControllerInstance = new DataController()
                    .withContentManager(cManager)
                    .withFetcherFactory(fFactory)
                    .withCurrentUser(getUserManagerInstance().getCurrentUser());
        }
        return dataControllerInstance;
    }

    public static AdfService getAdfServiceInstance() {
        if (adfServiceInstance == null) {
            adfServiceInstance = new FirebaseAdfService(
                    FirebaseDatabase.getInstance().getReference()
                            .child(DATABASE_ROOT).child(ROOMS_NODE),
                    FirebaseStorage.getInstance().getReference().child(STORAGE_ROOT));
        }
        return adfServiceInstance;
    }

    public static UserManager getUserManagerInstance() {
        if (userManagerInstance == null) {
            userManagerInstance = new UserManager(
                    FirebaseAuth.getInstance(),
                    FirebaseDatabase.getInstance()
                            .getReference().child(USERS_NODE)
            );
        }
        return userManagerInstance;
    }
}