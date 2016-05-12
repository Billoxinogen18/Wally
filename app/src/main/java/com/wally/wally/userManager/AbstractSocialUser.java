package com.wally.wally.userManager;

import com.wally.wally.datacontroller.user.User;

import java.net.URL;
import java.util.List;

/**
 * Created by Meravici on 5/12/2016.
 */
public abstract class AbstractSocialUser implements SocialUser {
    protected User mBaseUser;

    protected AbstractSocialUser(User baseUser){
        mBaseUser = baseUser;
    }

    public String getId(){
        return mBaseUser.getId();
    }
}
