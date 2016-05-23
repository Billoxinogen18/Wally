package com.wally.wally.userManager;

import com.wally.wally.datacontroller.user.User;

import java.io.Serializable;
import java.net.URL;
import java.util.List;

/**
 * Created by Xato on 5/12/2016.
 */
public interface SocialUser extends Serializable {
    User getBaseUser();
    String getDisplayName();
    String getFirstName();
    String getAvatarUrl();
    String getCoverUrl();
    List<String> getFriends();

    SocialUser withDisplayName(String displayName);
    SocialUser withFirstName(String firstName);
    SocialUser withAvatar(String avatarUrl);
    SocialUser withCover(String coverUrl);
    SocialUser withFriends(List<String> friends);
}
