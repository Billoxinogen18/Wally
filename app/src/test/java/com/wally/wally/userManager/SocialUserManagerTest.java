package com.wally.wally.userManager;

import com.wally.wally.datacontroller.user.User;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by shota on 5/24/16.
 */

public class SocialUserManagerTest {
    private SocialUserManager mSocialUserManager;

    @Before
    public void init(){
        mSocialUserManager = new SocialUserManager(null);
    }


    @After
    public void finish(){
        mSocialUserManager = null;
    }

    @Test
    public void isLoggedInTest1(){
        assertThat(mSocialUserManager.isLoggedIn(), is(false));
    }

    @Test
    public void isLoggedInTest2(){
        mSocialUserManager.setUser(new GoogleUser(new User()));
        assertThat(mSocialUserManager.isLoggedIn(), is(true));
    }

    @Test
    public void isLoggedInTest3(){
        mSocialUserManager.setUser(null);
        assertThat(mSocialUserManager.isLoggedIn(), is(false));
    }

    @Test
    public void setUserTest(){
        SocialUser gu = new GoogleUser(new User());
        mSocialUserManager.setUser(gu);
        assertThat(mSocialUserManager.getUser(), is(gu));
    }


}
