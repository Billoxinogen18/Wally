package com.wally.wally.datacontroller.user;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.wally.wally.datacontroller.callbacks.Callback;

public class UserManager {
    private User currentUser;
    private DatabaseReference users;
    private FirebaseAuth authManager;

    public UserManager(FirebaseAuth authManager, DatabaseReference users) {
        this.users = users;
        this.authManager = authManager;
    }

    public User getCurrentUser() {
        if (currentUser != null) return currentUser;
        FirebaseUser user = authManager.getCurrentUser();
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

}
