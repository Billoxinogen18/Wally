package com.wally.wally.userManager;

import com.wally.wally.datacontroller.user.User;

import java.net.URL;
import java.util.List;

/**
 * Created by Xato on 5/12/2016.
 */
public interface SocialUser {
    User getBaseUser();
    String getName();
    String getAvatarUrl();
    String getCoverUrl();
    void getFriends(FriendsLoadListener friendsLoadListener);

    SocialUser withName(String name);
    SocialUser withAvatar(String avatarUrl);
    SocialUser withCover(String coverUrl);
}
