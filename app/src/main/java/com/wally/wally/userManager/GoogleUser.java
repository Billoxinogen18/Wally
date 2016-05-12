package com.wally.wally.userManager;

import com.wally.wally.datacontroller.user.User;

import java.net.URL;
import java.util.List;

/**
 * Created by Meravici on 5/12/2016.
 */
public class GoogleUser extends AbstractSocialUser {

    protected GoogleUser(User baseUser) {
        super(baseUser);

    }

    public String getGoogleId(){
        return mBaseUser.getGgId();
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public URL getAvatarUrl() {
        return null;
    }

    @Override
    public URL getCoverUrl() {
        return null;
    }

    @Override
    public List<AbstractSocialUser> getFriends() {
        return null;
    }
}
