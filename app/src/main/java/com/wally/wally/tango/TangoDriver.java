package com.wally.wally.tango;

import android.util.Log;

import com.google.atap.tangoservice.TangoPoseData;
import com.projecttango.rajawali.Pose;
import com.wally.wally.adf.AdfInfo;
import com.wally.wally.tango.states.TangoBase;


/**
 * Created by shota on 8/9/16.
 *
 */
public class TangoDriver implements TangoBase.StateChangeListener {
    private static final String TAG = TangoDriver.class.getSimpleName();

    private TangoBase tangoState;

    public TangoDriver(TangoBase startTangoState) {
        tangoState = startTangoState;
        tangoState.setStateChangeListener(this);
    }
    
    public void pause() {
        tangoState.pause();
    }

    public void resume() {
        tangoState.resume();
    }

    @Override
    public void onStateChange(TangoBase nextTangoState) {
        Log.d(TAG, "onStateChange from =" + tangoState + " -- To [" + nextTangoState + "]");
        tangoState = nextTangoState;
        tangoState.setStateChangeListener(this);
    }

    public boolean isLearningState() {
        return tangoState.isLearningState();
    }

    public boolean isTangoLocalized() {
        return tangoState.isLocalized();
    }

    public boolean isTangoConnected() {
        return tangoState.isConnected();
    }

    /**
     * Finds plane pose in the middle of the screen.
     */
    public TangoPoseData findPlaneInMiddle() {
        return tangoState.findPlaneInMiddle();
    }

    public AdfInfo getAdf() {
        Log.d(TAG, "getAdf");
        return tangoState.getAdf();
    }

    public Pose getDevicePoseInFront() {
        return tangoState.getDevicePoseInFront();
    }

}
