package com.wally.wally.userManager;

import android.text.TextUtils;

import com.wally.wally.datacontroller.user.User;

import java.util.List;

/**
 * Created by Meravici on 5/12/2016. yea
 */
public class GoogleUser extends AbstractSocialUser {
    private static final int DEFAULT_AVATAR_SIZE = 256;

    private String mDisplayName;
    private String mAvatarUrl;
    private String mCoverUrl;
    private String mFirstName;
    private List<SocialUser> mFriends;

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
        return mAvatarUrl + "&sz=" + DEFAULT_AVATAR_SIZE;
    }

    @Override
    public String getAvatarUrl(int size) {
        return mAvatarUrl + "&sz=" + size;
    }

    @Override
    public String getCoverUrl() {
        return mCoverUrl;
    }

    @Override
    public List<SocialUser> getFriends() {
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
    public SocialUser withFriends(List<SocialUser> friends) {
        mFriends = friends;
        return this;
    }

    @Override
    public String toString() {
        return "GoogleUser{" +
                "mDisplayName='" + mDisplayName + '\'' +
                ", mAvatarUrl='" + mAvatarUrl + '\'' +
                ", mCoverUrl='" + mCoverUrl + '\'' +
                ", mFirstName='" + mFirstName + '\'' +
                ", mFriends=" + mFriends +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GoogleUser that = (GoogleUser) o;

        return (getBaseUser().getGgId() != null && that.getBaseUser().getGgId() != null) && TextUtils.equals(getBaseUser().getGgId().getId(), that.getBaseUser().getGgId().getId());
    }

    @Override
    public int hashCode() {
        return getBaseUser().getGgId() != null ? getBaseUser().getGgId().hashCode() : 0;
    }
}