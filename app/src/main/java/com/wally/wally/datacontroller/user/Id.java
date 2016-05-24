package com.wally.wally.datacontroller.user;

import java.io.Serializable;

/**
 * Created by Meravici on 5/23/2016.
 */
public class Id implements Serializable{
    public static final int PROVIDER_FIREBASE = 0;
    public static final int PROVIDER_GOOGLE = 1;
    public static final int PROVIDER_FACEBOOK = 2;

    private int provider;
    private String id;

    public Id(){

    }

    public Id(int provider, String id){
        // TODO validate
        this.provider = provider;
        this.id = id;
    }

    public int getProvider() {
        return provider;
    }

    public String getId() {
        return id;
    }
}
