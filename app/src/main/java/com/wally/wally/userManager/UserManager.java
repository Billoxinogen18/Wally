package com.wally.wally.userManager;

import com.google.android.gms.common.api.GoogleApiClient;
import com.wally.wally.App;
import com.wally.wally.datacontroller.user.User;

/**
 * Class that manages login flow
 * <p/>
 * Created by ioane5 on 5/10/16.
 * ReCreated by meravici on 5/21/16.
 */
public class UserManager {
    SocialUserFactory mSocialUserFactory;
    SocialUser mUser;


    public UserManager(){
        mSocialUserFactory = new SocialUserFactory();
    }

    public void setUser(SocialUser user){
        mUser = user;
    }

    public SocialUser getUser(){
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
    public void loadUser(GoogleApiClient googleApiClient, final UserLoadListener userLoadListener) {
        User user = App.getInstance().getDataController().getCurrentUser();

        loadUser(user, googleApiClient, userLoadListener);
    }


    public void loadUser(User user, GoogleApiClient googleApiClient, final UserLoadListener userLoadListener) {
        mSocialUserFactory.getSocialUser(user, googleApiClient, new UserLoadListener() {
            @Override
            public void onUserLoad(SocialUser user) {
                App.getInstance().getUserManager().setUser(user);
                userLoadListener.onUserLoad(user);
            }
        });
    }

    public interface UserLoadListener {
        void onUserLoad(SocialUser user);

    }
}
