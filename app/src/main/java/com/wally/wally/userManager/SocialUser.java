package com.wally.wally.userManager;

import java.net.URL;
import java.util.List;

/**
 * Created by Xato on 5/12/2016.
 */
public interface SocialUser {
    String getId();
    String getName();
    URL getAvatarUrl();
    URL getCoverUrl();
    List<AbstractSocialUser> getFriends();
}
