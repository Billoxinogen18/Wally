package com.wally.wally;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.wally.wally.datacontroller.DataController;

/**
 * Application class for Application wide feature initializations.
 * <p/>
 * Created by ioane5 on 3/31/16.
 */
public class App extends Application {
    private DataController dataController;

    @Override
    public void onCreate() {
        super.onCreate();
        dataController = DataController.create(this);
    }

    public DataController getDataController() {
        return dataController;
    }

    @Override
    protected void attachBaseContext(Context base){
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

}
