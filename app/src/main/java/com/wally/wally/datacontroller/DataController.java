package com.wally.wally.datacontroller;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.wally.wally.datacontroller.queries.*;
import com.wally.wally.datacontroller.callbacks.*;
import com.wally.wally.datacontroller.content.Content;
import com.wally.wally.datacontroller.content.FirebaseContent;
import com.wally.wally.datacontroller.user.User;

public class DataController {
    private static final String FIREBASE_URL = "https://crypto-gradient-131308.firebaseio.com/";
    private static final String DB_PATH = "Firebase-Update";
    private static DataController instance;
    private FirebaseAuth authManager;
    private DatabaseReference firebaseRoot;
    private DatabaseReference users;
    private DatabaseReference contents;

    private DataController(DatabaseReference firebase) {
        firebaseRoot = firebase;
        users = firebaseRoot.child("Users");
        contents = firebaseRoot.child("Contents");
        // TODO must pass in constructor (or else)
        authManager = FirebaseAuth.getInstance();
    }

    public static DataController create(Context context) {
        if (instance == null) {
            DatabaseReference ref = FirebaseDatabase.getInstance()
                    .getReferenceFromUrl(FIREBASE_URL).child(DB_PATH);
            instance = new DataController(ref);
        }
        return instance;
    }

    public void save(Content c) {
        new FirebaseContent(c).save(contents);
    }

    public void delete(Content c) {
        new FirebaseContent(c).delete(contents);
    }

    public void fetchByBounds(LatLngBounds bounds, FetchResultCallback callback) {
        new LatLngQuery(bounds).fetch(contents, new FirebaseFetchResultCallback(callback));
    }

    public void fetchByUUID(String uuid, FetchResultCallback callback) {
        new UUIDQuery(uuid).fetch(contents, new FirebaseFetchResultCallback(callback));
    }

    public void fetchByAuthor(String authorId, FetchResultCallback callback) {
        new AuthorQuery(authorId).fetch(contents, new FirebaseFetchResultCallback(callback));
    }

    public void fetchByAuthor(User author, FetchResultCallback resultCallback) {
        fetchByAuthor(author.getId(), resultCallback);
    }

    public void googleAuth(String accessToken, final Callback<User> callback) {
        Log.d("Test", "Login");
        AuthCredential credentials = GoogleAuthProvider.getCredential(accessToken, null);
        authManager.signInWithCredential(credentials)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser user = authManager.getCurrentUser();
                    String id = user.getUid();
                    // .get(0) assumes only one provider (Google)
                    String ggId = user.getProviderData().get(0).getUid();
                    users.child(id).child("ggId").setValue(ggId);
                    callback.onResult(new User(id).withGgId(ggId));
                } else {
                    callback.onError(task.getException());
                }
            }
        });
    }

//    private void firebaseAuth(String provider, String token, final Callback<AuthData> callback) {
//        firebaseRoot.authWithOAuthToken(provider, token, new Firebase.AuthResultHandler() {
//            @Override
//            public void onAuthenticated(AuthData authData) {
//                callback.onResult(authData);
//            }
//
//            @Override
//            public void onAuthenticationError(FirebaseError firebaseError) {
//                callback.onError(firebaseError.toException());
//            }
//        });
//    }

}