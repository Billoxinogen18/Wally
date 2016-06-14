package com.wally.wally.userManager;

import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.wally.wally.datacontroller.DataController;
import com.wally.wally.datacontroller.user.User;

/**
 * Class that manages login flow
 * <p/>
 * Created by ioane5 on 5/10/16.
 * ReCreated by meravici on 5/21/16.
 */
public class UserManager {
    private static final String TAG = "UserManager";
    SocialUserFactory mSocialUserFactory;
    DataController mDataController;
    SocialUser mUser;


    public UserManager(SocialUserFactory socialUserFactory, DataController dataController){
        mSocialUserFactory = socialUserFactory;
        mDataController = dataController;
    }

    public void setUser(SocialUser user){
        mUser = user;
    }

    public SocialUser getUser(){
        Log.d(TAG, "getUser: " + mUser);
        return mUser;
    }

    public boolean isLoggedIn(){
        return mUser != null;
    }

    /*
     * GoogleApiClient googleApiClient = new GoogleApiClient.Builder(this)
     *      .enableAutoManage(this, this)
     *       .addApi(Plus.API)
     *      .addScope(Plus.SCOPE_PLUS_LOGIN)
     *      .addScope(Plus.SCOPE_PLUS_PROFILE)
     *      .build();
     */
    public void loadLoggedInUser(GoogleApiClient googleApiClient, final UserLoadListener userLoadListener) {
        User user = mDataController.getCurrentUser();

        loadLoggedInUser(user, googleApiClient, userLoadListener);
    }


    public void loadLoggedInUser(User user, GoogleApiClient googleApiClient, final UserLoadListener userLoadListener) {
        loadUser(user, googleApiClient, new UserLoadListener() {
            @Override
            public void onUserLoad(SocialUser user) {
                UserManager.this.setUser(user);
                userLoadListener.onUserLoad(user);
            }

            @Override
            public void onUserLoadFailed() {
                userLoadListener.onUserLoadFailed();
            }
        });
    }


    public void loadUser(User user, GoogleApiClient googleApiClient, UserLoadListener userLoadListener){
        mSocialUserFactory.getSocialUser(user, googleApiClient, userLoadListener);
    }

    public interface UserLoadListener {
        void onUserLoad(SocialUser user);
        void onUserLoadFailed();
    }
}
