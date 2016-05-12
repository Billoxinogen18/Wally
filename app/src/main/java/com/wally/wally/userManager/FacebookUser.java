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
    public URL getAvatarUrl() {
        throw new UnsupportedOperationException();
    }

    @Override
    public URL getCoverUrl() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<AbstractSocialUser> getFriends() {
        throw new UnsupportedOperationException();
    }
}
