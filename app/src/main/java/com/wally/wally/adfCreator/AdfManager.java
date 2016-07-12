package com.wally.wally.adfCreator;

/**
 * Lazy retrieval of ADF files from the cloud.
 * Created by shota on 7/11/16.
 */
public class AdfManager {
    public boolean hasAdf(){
        return true;
    }

    public boolean isAdfReady(){
        return true;
    }

    public AdfInfo getAdf(){
        return new AdfInfo();
    }


}
