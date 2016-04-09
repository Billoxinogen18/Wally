package com.wally.wally;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.wally.wally.dal.DALFactory;
import com.wally.wally.dal.DataAccessLayer;

/**
 * Application class for Application wide feature initializations.
 * <p/>
 * Created by ioane5 on 3/31/16.
 */
public class App extends Application {
    private DataAccessLayer dal;

    @Override
    public void onCreate() {
        super.onCreate();
        dal = DALFactory.create(this);
    }

    public DataAccessLayer getDal() {
        return dal;
    }

    // support multi-dex.
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
