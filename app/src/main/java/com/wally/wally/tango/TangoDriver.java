package com.wally.wally.tango;

import com.google.atap.tangoservice.TangoPoseData;
import com.projecttango.rajawali.Pose;
import com.wally.wally.adf.AdfInfo;
import com.wally.wally.tango.states.TangoBase;


/**
 * Created by shota on 8/9/16.
 *
 */
public class TangoDriver implements TangoBase.StateChangeListener {
    private TangoBase tangoState;

    public TangoDriver(TangoBase startTangoState) {
        tangoState = startTangoState;
        tangoState.setStateChangeListener(this);
    }

    @Override
    public void onStateChange(TangoBase nextTangoState) {
        tangoState = nextTangoState;
        tangoState.setStateChangeListener(this);
    }

    public void pause() {
        tangoState.pause();
    }

    public void resume() {
        tangoState.resume();
    }

    public AdfInfo getAdf() {
        return tangoState.getAdf();
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

    public Pose getDevicePoseInFront() {
        return tangoState.getDevicePoseInFront();
    }

}
