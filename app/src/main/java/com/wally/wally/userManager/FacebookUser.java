package com.wally.wally.userManager;

import com.wally.wally.datacontroller.user.User;

import java.net.URL;
import java.util.List;

/**
 * Created by Meravici on 5/12/2016.
 */
public class FacebookUser extends AbstractSocialUser {
    public FacebookUser(User baseUser) {
        super(baseUser);
    }

    @Override
    public String getName() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getAvatarUrl() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getCoverUrl() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void getFriends(FriendsLoadListener friendsLoadListener) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SocialUser withName(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SocialUser withAvatar(String avatarUrl) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SocialUser withCover(String coverUrl) {
        throw new UnsupportedOperationException();
    }
}