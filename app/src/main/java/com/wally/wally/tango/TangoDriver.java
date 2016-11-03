package com.wally.wally.tango;

import android.hardware.Camera;
import android.hardware.display.DisplayManager;
import android.util.Log;
import android.view.Display;

import com.projecttango.rajawali.Pose;
import com.wally.wally.adf.AdfInfo;
import com.wally.wally.tango.states.TangoState;


/**
 * Created by shota on 8/9/16.
 *
 */
public class TangoDriver implements TangoState.StateChangeListener, DisplayManager.DisplayListener {
    private static final String TAG = TangoDriver.class.getSimpleName();
    private boolean mIsPaused = false;

    private TangoState tangoState;
    private Display mDisplay;

    public TangoDriver(TangoState startTangoState) {
        tangoState = startTangoState;
    }

    public synchronized void pause() {
        mIsPaused = true;
        tangoState.pause();
    }

    public synchronized void resume() {
        mIsPaused = false;
        tangoState.resume();
    }

    @Override
    public synchronized void onStateChange(TangoState nextTangoState) {
        Log.d(TAG, "onStateChange(" + tangoState + " ===> " + nextTangoState + ")");
        tangoState = nextTangoState;
    }

    @Override
    public synchronized boolean canChangeState() {
        return !mIsPaused;
    }

    public synchronized AdfInfo getAdf() {
        Log.d(TAG, "getAdf");
        return tangoState.getAdf();
    }

    public void setDefaultDisplay(Display display){
        mDisplay = display;
    }


    @Override
    public void onDisplayAdded(int displayId) {}

    @Override
    public void onDisplayChanged(int displayId) {
        synchronized (this) {
            Camera.CameraInfo colorCameraInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(0, colorCameraInfo);

            int colorCameraToDisplayAndroidRotation =
                    getColorCameraToDisplayAndroidRotation(mDisplay.getRotation(),
                            colorCameraInfo.orientation);
            tangoState.updateColorCameraTextureUv(colorCameraToDisplayAndroidRotation);
        }
    }

    @Override
    public void onDisplayRemoved(int displayId) {}


    public synchronized boolean isLearningState() {
        return tangoState.isLearningState();
    }

    public synchronized boolean isTangoLocalized() {
        return tangoState.isLocalized();
    }

    public synchronized boolean isTangoConnected() {
        return tangoState.isConnected();
    }

    synchronized float[] findPlaneInMiddle() {
        return tangoState.findPlaneInMiddle();
    }

    synchronized Pose getDevicePoseInFront() {
        return tangoState.getDevicePoseInFront();
    }


    private static int getColorCameraToDisplayAndroidRotation(int displayRotation,
                                                              int cameraRotation) {
        int cameraRotationNormalized = 0;
        switch (cameraRotation) {
            case 90:
                cameraRotationNormalized = 1;
                break;
            case 180:
                cameraRotationNormalized = 2;
                break;
            case 270:
                cameraRotationNormalized = 3;
                break;
            default:
                cameraRotationNormalized = 0;
                break;
        }
        int ret = displayRotation - cameraRotationNormalized;
        if (ret < 0) {
            ret += 4;
        }
        return ret;
    }
}
