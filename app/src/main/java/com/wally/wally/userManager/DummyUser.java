package com.wally.wally.userManager;

import com.wally.wally.datacontroller.user.User;

import java.util.ArrayList;

/**
 * Created by Xato on 5/20/2016.
 */
public class DummyUser extends AbstractSocialUser{
    protected DummyUser(User baseUser) {
        super(baseUser);
    }

    @Override
    public String getDisplayName() {
        return "";
    }

    @Override
    public String getFirstName() {
        return "";
    }

    @Override
    public String getAvatarUrl() {
        return "";
    }

    @Override
    public String getCoverUrl() {
        return "";
    }

    @Override
    public void getFriends(FriendsLoadListener friendsLoadListener) {
        friendsLoadListener.onFriendsLoad(new ArrayList<SocialUser>());
    }

    @Override
    public SocialUser withDisplayName(String displayName) {
        return this;
    }

    @Override
    public SocialUser withFirstName(String firstName) {
        return this;
    }

    @Override
    public SocialUser withAvatar(String avatarUrl) {
        return this;
    }

    @Override
    public SocialUser withCover(String coverUrl) {
        return this;
    }
}
