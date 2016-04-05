package com.wally.wally;

import android.app.Application;

import com.wally.wally.dal.DALFactory;
import com.wally.wally.dal.DataAccessLayer;

/**
 * Created by ioane5 on 3/31/16.
 */
public class App extends Application {
    private DataAccessLayer dal;
    @Override
    public void onCreate() {
        super.onCreate();
        dal  = DALFactory.create(this);
    }

    public DataAccessLayer getDal() {
        return dal;
    }
}
