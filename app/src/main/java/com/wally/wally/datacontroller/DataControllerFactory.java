package com.wally.wally.datacontroller;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.wally.wally.adf.AdfService;
import com.wally.wally.datacontroller.user.User;
import com.wally.wally.datacontroller.user.UserManager;
import com.wally.wally.objects.content.Content;
import com.wally.wally.objects.content.Puzzle;
import com.wally.wally.objects.content.SerializableLatLng;

import java.util.Collection;

public class DataControllerFactory {
    private static final String DATABASE_ROOT = "Develop";
    private static final String STORAGE_ROOT = DATABASE_ROOT;
    private static final String ROOMS_NODE = "Rooms";
    private static final String USERS_NODE = DATABASE_ROOT + "/Users";
    private static final String CONTENTS_NODE = "Contents";

    private static AdfService adfServiceInstance;
    private static UserManager userManagerInstance;
    private static DataController dataControllerInstance;

    public static DataController getDataControllerInstance() {
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

    private static DBController dbController;
    public static DBController getDbController() {
        if (dbController == null) {
            getDataControllerInstance();
            dbController = new DBController() {
                @Override
                public void save(Content content) {
                    dataControllerInstance.save(content);
                }

                @Override
                public void delete(Content content) {
                    dataControllerInstance.delete(content);
                }

                @Override
                public void fetchForUuid(String uuid, final ResultCallback resultCallback) {
                    dataControllerInstance.fetchForUuid(uuid, callbackWrapper(resultCallback));
                }

                @Override
                public boolean checkAnswer(Puzzle puzzle, String answer) {
                    return dataControllerInstance.checkAnswer(puzzle, answer);
                }

                @Override
                public Fetcher createFetcherForPuzzleSuccessors(Puzzle puzzle) {
                    return fetcherWrapper(dataControllerInstance.createFetcherForPuzzleSuccessors(puzzle));
                }

                @Override
                public Fetcher createFetcherForMyContent() {
                    return fetcherWrapper(dataControllerInstance.createFetcherForMyContent());
                }

                @Override
                public Fetcher createFetcherForUserContent(User baseUser) {
                    return fetcherWrapper(dataControllerInstance.createFetcherForUserContent(baseUser));
                }

                @Override
                public Fetcher createFetcherForVisibleContent(SerializableLatLng center, double radius) {
                    return fetcherWrapper(dataControllerInstance.createFetcherForVisibleContent(center, radius));
                }
            };
        }
        return dbController;
    }

    private static DataController.FetchResultCallback callbackWrapper(final DBController.ResultCallback resultCallback) {
        return new DataController.FetchResultCallback() {
            @Override
            public void onResult(Collection<Content> result) {
                resultCallback.onResult(result);
            }
        };
    }

    private static DBController.Fetcher fetcherWrapper(final DataController.Fetcher fetcher) {
        return new DBController.Fetcher() {
            @Override
            public void fetchNext(int i, DBController.ResultCallback callback) {
                fetcher.fetchNext(i, callbackWrapper(callback));
            }
        };
    }
}