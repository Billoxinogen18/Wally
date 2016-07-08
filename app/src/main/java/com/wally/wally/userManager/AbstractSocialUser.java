package com.wally.wally.userManager;

import com.wally.wally.datacontroller.user.User;

/**
 * Created by Meravici on 5/12/2016. yea
 */
public abstract class AbstractSocialUser implements SocialUser {
    protected User mBaseUser;

    protected AbstractSocialUser(User baseUser){
        mBaseUser = baseUser;
    }

    public User getBaseUser(){
        return mBaseUser;
    }
}
