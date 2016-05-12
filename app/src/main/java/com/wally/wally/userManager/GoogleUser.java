package com.wally.wally.userManager;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.plus.People;
import com.google.android.gms.plus.Plus;
import com.wally.wally.datacontroller.user.User;

import java.net.URL;
import java.util.List;

/**
 * Created by Meravici on 5/12/2016.
 */
public class GoogleUser extends AbstractSocialUser {
    private GoogleApiClient mGoogleApiClient;

    private String mName;
    private String mAvatarUrl;
    private String mCoverUrl;

    protected GoogleUser(User baseUser) {
        super(baseUser);
    }

    public String getGoogleId(){
        return mBaseUser.getGgId();
    }

    @Override
    public String getName() {
        return mName;
    }

    @Override
    public String getAvatarUrl() {
        return mAvatarUrl;
    }

    @Override
    public String getCoverUrl() {
        return mCoverUrl;
    }

    @Override
    public void getFriends(FriendsLoadListener friendsLoadListener) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SocialUser withName(String name) {
        mName = name;
        return this;
    }

    @Override
    public SocialUser withAvatar(String avatarUrl) {
        mAvatarUrl = avatarUrl;
        return this;
    }

    @Override
    public SocialUser withCover(String coverUrl) {
        mCoverUrl = coverUrl;
        return this;
    }
}