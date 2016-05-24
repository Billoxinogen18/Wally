package com.wally.wally.userManager;

import com.wally.wally.datacontroller.user.Id;
import com.wally.wally.datacontroller.user.User;

import java.util.List;

/**
 * Created by Meravici on 5/12/2016.
 */
public class GoogleUser extends AbstractSocialUser {
    private String mDisplayName;
    private String mAvatarUrl;
    private String mCoverUrl;
    private String mFirstName;
    private List<Id> mFriends;

    protected GoogleUser(User baseUser) {
        super(baseUser);
    }

    public String getGoogleId() {
        return mBaseUser.getGgId().getId();
    }

    @Override
    public String getDisplayName() {
        return mDisplayName;
    }

    @Override
    public String getFirstName() {
        return mDisplayName;
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
    public List<Id> getFriends() {
        return mFriends;
    }

    @Override
    public SocialUser withDisplayName(String displayName) {
        mDisplayName = displayName;
        return this;
    }

    @Override
    public SocialUser withFirstName(String firstName) {
        mFirstName = firstName;
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

    @Override
    public SocialUser withFriends(List<Id> friends) {
        mFriends = friends;
        return this;
    }
}