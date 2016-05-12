package com.wally.wally;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.wally.wally.datacontroller.DataController;
import com.wally.wally.userManager.SocialUser;
import com.wally.wally.userManager.SocialUserFactory;

/**
 * Application class for Application wide feature initializations.
 * <p/>
 * Created by ioane5 on 3/31/16.
 */
public class App extends Application {
    private static App sInstance = null;

    private DataController dataController;
    private SocialUserFactory socialUserFactory;
    private SocialUser mUser;

    public static App getInstance() {
        return sInstance;
    }

    public static Context getContext() {
        return sInstance.getBaseContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        dataController = DataController.create(this);
        socialUserFactory = new SocialUserFactory();
        sInstance = this;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public DataController getDataController() {
        return dataController;
    }

    public SocialUserFactory getSocialUserFactory(){
        return socialUserFactory;
    }

    public SocialUser getUser() {
        return mUser;
    }

    public void setUser(SocialUser user){
        mUser = user;
    }
}
