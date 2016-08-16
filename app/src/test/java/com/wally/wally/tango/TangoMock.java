package com.wally.wally.tango;

import android.content.Context;

import com.google.atap.tangoservice.Tango;
import com.google.atap.tangoservice.TangoConfig;
import com.google.atap.tangoservice.TangoCoordinateFramePair;
import com.google.atap.tangoservice.TangoPoseData;

import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

/**
 * Created by shota on 5/24/16.
 * Tango Mock for testing
 */

public class TangoMock extends Tango{
    private TangoConfig config;

    public TangoMock(Context context) {
        super(context);
    }


    public static Tango getTango(){
        Objenesis objenesis = new ObjenesisStd(); // or ObjenesisSerializer

        return objenesis.newInstance(TangoMock.class);
    }

    @Override
    public TangoPoseData getPoseAtTime(double timestamp, TangoCoordinateFramePair framePair) {
        return null;
    }

    @Override
    public void connect(TangoConfig config){
        this.config = config;
    }

    @Override
    public TangoConfig getConfig(int x){
        return config;
    }
}
