package com.wally.wally;

import android.app.Application;
import android.content.Context;

import com.wally.wally.datacontroller.DataController;
import com.wally.wally.userManager.UserManager;
import com.wally.wally.userManager.SocialUserFactory;

/**
 * Application class for Application wide feature initializations.
 * <p/>
 * Created by ioane5 on 3/31/16.
 */
public class App extends Application {
    private static App sInstance = null;

    private DataController dataController;
    private UserManager userManager;

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
        userManager = new UserManager(new SocialUserFactory(), dataController);
        sInstance = this;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }

    public DataController getDataController() {
        return dataController;
    }

    public UserManager getUserManager() {
        return userManager;
    }
}
