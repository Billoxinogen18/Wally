package com.wally.wally;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.wally.wally.adf.AdfManager;
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
    private AdfManager adfManager;

    public static App getInstance() {
        return sInstance;
    }

    public static Context getContext() {
        return sInstance.getBaseContext();
    }

    @Override
    public void onCreate() {
        MultiDex.install(getApplicationContext());
        super.onCreate();
        // TODO: DataController.create()
        // Is implemented with singleton
        // Why save the instance here as well?
        dataController = DataController.create();
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

    public void setAdfManager(AdfManager adfManager) {
        this.adfManager = adfManager;
    }

    public AdfManager getAdfManager() {
        return adfManager;
    }
}
