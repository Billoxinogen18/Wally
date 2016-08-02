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

public class UserManagerTest {
    private UserManager mUserManager;

    @Before
    public void init(){
        mUserManager = new UserManager(null);
    }


    @After
    public void finish(){
        mUserManager = null;
    }

    @Test
    public void isLoggedInTest1(){
        assertThat(mUserManager.isLoggedIn(), is(false));
    }

    @Test
    public void isLoggedInTest2(){
        mUserManager.setUser(new GoogleUser(new User()));
        assertThat(mUserManager.isLoggedIn(), is(true));
    }

    @Test
    public void isLoggedInTest3(){
        mUserManager.setUser(null);
        assertThat(mUserManager.isLoggedIn(), is(false));
    }

    @Test
    public void setUserTest(){
        SocialUser gu = new GoogleUser(new User());
        mUserManager.setUser(gu);
        assertThat(mUserManager.getUser(), is(gu));
    }


}
