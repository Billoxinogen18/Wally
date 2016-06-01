package com.wally.wally.datacontroller.user;

import java.io.Serializable;

public class Id implements Serializable{
    public static final String PROVIDER_GOOGLE = "google";
    public static final String PROVIDER_FIREBASE = "firebase";
    public static final String PROVIDER_FACEBOOK = "facebook";

    private String provider;
    private String id;

    public Id(){

    }

    public Id(String provider, String id){
        // TODO validate
        this.provider = provider;
        this.id = id;
    }

    public String getProvider() {
        return provider;
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return provider + ":" + id;
    }
}
