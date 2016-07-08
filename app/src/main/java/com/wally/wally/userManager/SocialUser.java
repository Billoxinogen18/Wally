package com.wally.wally.userManager;

import com.wally.wally.datacontroller.user.User;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Meravici on 5/12/2016. yea
 */
public interface SocialUser extends Serializable {
    User getBaseUser();
    String getDisplayName();
    String getFirstName();
    String getAvatarUrl();
    String getAvatarUrl(int size);
    String getCoverUrl();
    List<SocialUser> getFriends();

    SocialUser withDisplayName(String displayName);
    SocialUser withFirstName(String firstName);
    SocialUser withAvatar(String avatarUrl);
    SocialUser withCover(String coverUrl);
    SocialUser withFriends(List<SocialUser> friends);
}
