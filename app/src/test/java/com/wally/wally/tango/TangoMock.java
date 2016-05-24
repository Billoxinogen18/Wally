package com.wally.wally.tango;

import android.content.Context;

import com.google.atap.tangoservice.Tango;
import com.google.atap.tangoservice.TangoCoordinateFramePair;
import com.google.atap.tangoservice.TangoPoseData;

import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

/**
 * Created by shota on 5/24/16.
 */

public class TangoMock extends Tango{
    public TangoMock(Context context) {
        super(context);
    }


    public static Tango getTango(){
        Objenesis objenesis = new ObjenesisStd(); // or ObjenesisSerializer
        TangoMock res = (TangoMock) objenesis.newInstance(TangoMock.class);

        return res;
    }

    @Override
    public TangoPoseData getPoseAtTime(double timestamp, TangoCoordinateFramePair framePair) {
        return null;
    }
}
