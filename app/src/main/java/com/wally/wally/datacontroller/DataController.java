package com.wally.wally.datacontroller;

import android.content.Context;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.android.gms.maps.model.LatLngBounds;
import com.wally.wally.datacontroller.queries.*;
import com.wally.wally.datacontroller.callbacks.*;
import com.wally.wally.datacontroller.content.Content;
import com.wally.wally.datacontroller.content.FirebaseContent;
import com.wally.wally.datacontroller.user.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class DataController {
    private static final String FIREBASE_URL = "https://burning-inferno-2566.firebaseio.com/";
    private static final String DB_PATH = "Develop";
    private static DataController instance;
    private Firebase firebaseRoot;
    private Firebase users;
    private Firebase contents;

    private DataController(Firebase firebase) {
        firebaseRoot = firebase;
        users = firebaseRoot.child("Users");
        contents = firebaseRoot.child("Contents");
    }

    public static DataController create(Context context) {
        if (instance == null) {
            Firebase.setAndroidContext(context);
            Firebase firebase = new Firebase(FIREBASE_URL).child(DB_PATH);
            instance = new DataController(firebase);
        }
        return instance;
    }

    private void firebaseAuth(String accessToken,
                              final com.wally.wally.datacontroller.callbacks.Callback<User> callback) {
        firebaseRoot.authWithOAuthToken("google", accessToken, new Firebase.AuthResultHandler() {
            @Override
            public void onAuthenticated(AuthData authData) {
                // Map<String, Object> data = authData.getProviderData();
                String ggId = (String) authData.getProviderData().get("id");
                User user = new User(authData.getUid()).withGgId(ggId);
                users.child(user.getId()).setValue(user);
                callback.onResult(user);
            }

            @Override
            public void onAuthenticationError(FirebaseError firebaseError) {
                callback.onError(firebaseError.toException());
            }
        });
    }

    @Deprecated
    public void googleAuth(String accessToken, final Callback<User> resultCallback) {
        firebaseAuth(accessToken, new com.wally.wally.datacontroller.callbacks.Callback<User>() {
            @Override
            public void onResult(User result) {
                resultCallback.call(result, null);
            }

            @Override
            public void onError(Exception e) {
                resultCallback.call(null, e);
            }
        });
    }

    public void googleAuth(String accessToken,
                           com.wally.wally.datacontroller.callbacks.Callback<User> callback) {
        firebaseAuth(accessToken, callback);
    }

    public void save(Content c) {
        new FirebaseContent(c).save(contents);
    }

    public void delete(Content c) {
        new FirebaseContent(c).delete(contents);
    }

    /**
     * @deprecated use {@link #fetchByBounds(LatLngBounds, FetchResultCallback)} ()} instead.
     */
    @Deprecated
    public void fetchByBounds(LatLngBounds bounds, final Callback<Collection<Content>> resultCallback) {
        new com.wally.wally.datacontroller.oldqueries.LatLngQuery(bounds)
                .fetch(contents, createFetchCallback(resultCallback));
    }

    public void fetchByBounds(LatLngBounds bounds, final FetchResultCallback callback) {
        new LatLngQuery(bounds).fetch(contents, createFetchCallback(callback));
    }

    /**
     * @deprecated use {@link #fetchByUUID(String, FetchResultCallback)} ()} instead.
     */
    @Deprecated
    public void fetchByUUID(String uuid, final Callback<Collection<Content>> resultCallback) {
        new com.wally.wally.datacontroller.oldqueries.UUIDQuery(uuid)
                .fetch(contents, createFetchCallback(resultCallback));
    }

    public void fetchByUUID(String uuid, final FetchResultCallback callback) {
        new UUIDQuery(uuid).fetch(contents, createFetchCallback(callback));
    }

    /**
     * @deprecated use {@link #fetchByAuthor(String, FetchResultCallback)} instead.
     */
    @Deprecated
    public void fetchByAuthor(String authorId, final Callback<Collection<Content>> resultCallback) {
        new com.wally.wally.datacontroller.oldqueries.AuthorQuery(authorId)
                .fetch(contents, createFetchCallback(resultCallback));
    }

    public void fetchByAuthor(String authorId, final FetchResultCallback callback) {
        new AuthorQuery(authorId).fetch(contents, createFetchCallback(callback));
    }

    public void fetchByAuthor(User author, FetchResultCallback resultCallback) {
        fetchByAuthor(author.getId(), resultCallback);
    }


    private FirebaseFetchResultCallback createFetchCallback(final FetchResultCallback callback) {
        return new FirebaseFetchResultCallback() {
            @Override
            public void onResult(Collection<FirebaseContent> result) {
                List<Content> contents = new ArrayList<>();
                for (FirebaseContent c : result) {
                    contents.add(convertToContent(c));
                }
                callback.onResult(contents);
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        };
    }

    @Deprecated
    private Callback<Collection<FirebaseContent>> createFetchCallback(
            final Callback<Collection<Content>> resultCallback) {
        return new Callback<Collection<FirebaseContent>>() {
            @Override
            public void call(Collection<FirebaseContent> result, Exception e) {
                List<Content> contents = new ArrayList<>();
                for (FirebaseContent c : result) {
                    contents.add(convertToContent(c));
                }
                resultCallback.call(contents, e);
            }
        };
    }

    private Content convertToContent(FirebaseContent firebaseContent) {
        // TODO
        return firebaseContent.toContent();
    }

}