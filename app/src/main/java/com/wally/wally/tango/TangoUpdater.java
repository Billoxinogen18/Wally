package com.wally.wally.tango;

import android.opengl.GLSurfaceView;

import com.google.atap.tango.ux.TangoUx;
import com.google.atap.tangoservice.Tango;
import com.google.atap.tangoservice.TangoCameraIntrinsics;
import com.google.atap.tangoservice.TangoCoordinateFramePair;
import com.google.atap.tangoservice.TangoEvent;
import com.google.atap.tangoservice.TangoPoseData;
import com.google.atap.tangoservice.TangoXyzIjData;
import com.projecttango.tangosupport.TangoPointCloudManager;

import org.rajawali3d.surface.RajawaliSurfaceView;
import org.rajawali3d.surface.RajawaliTextureView;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class TangoUpdater implements Tango.OnTangoUpdateListener {
    private TangoUx mTangoUx;
    private boolean isLocalized;
    private RajawaliTextureView mSurfaceView;
    private TangoPointCloudManager mPointCloudManager;
    private List<ValidPoseListener> mValidPoseListeners;
    private ArrayList<TangoCoordinateFramePair> mFramePairs;
    private PriorityQueue<TangoUpdaterListener> mTangoUpdaterListeners;



    public TangoUpdater(TangoUx tangoUx, RajawaliTextureView surfaceView, TangoPointCloudManager pointCloudManager) {
        mTangoUx = tangoUx;
        mSurfaceView = surfaceView;
        mPointCloudManager = pointCloudManager;
        mTangoUpdaterListeners = new PriorityQueue<>(5, new Comparator<TangoUpdaterListener>() {
            @Override
            public int compare(TangoUpdaterListener a, TangoUpdaterListener b) {
                return a.priority() - b.priority();
            }
        });
        mValidPoseListeners = new ArrayList<>();
        mFramePairs = new ArrayList<>();
        mFramePairs.add(
                new TangoCoordinateFramePair(
                        TangoPoseData.COORDINATE_FRAME_AREA_DESCRIPTION,
                        TangoPoseData.COORDINATE_FRAME_START_OF_SERVICE));
        mFramePairs.add(
                new TangoCoordinateFramePair(
                        TangoPoseData.COORDINATE_FRAME_START_OF_SERVICE,
                        TangoPoseData.COORDINATE_FRAME_DEVICE
                ));
    }

    public ArrayList<TangoCoordinateFramePair> getFramePairs() {
        return mFramePairs;
    }

    @Override
    public void onPoseAvailable(TangoPoseData pose) {
        mTangoUx.updatePoseStatus(pose.statusCode);
        updateLocalization(pose);
        checkAndFireOnValidPose(pose);
    }

    private void updateLocalization(TangoPoseData pose) {
        if (pose.statusCode != TangoPoseData.POSE_VALID) {
            setTangoLocalization(false);
        } else if (pose.baseFrame == TangoPoseData.COORDINATE_FRAME_AREA_DESCRIPTION &&
                pose.targetFrame == TangoPoseData.COORDINATE_FRAME_START_OF_SERVICE) {
            setTangoLocalization(true);
        }
    }

    @Override
    public void onFrameAvailable(int cameraId) {
        // Check if the frame available is for the camera we want and
        // update its frame on the view.
        if (cameraId == TangoCameraIntrinsics.TANGO_CAMERA_COLOR) {
            // Now that we are receiving onFrameAvailable callbacks, we can switch
            // to RENDERMODE_WHEN_DIRTY to drive the render loop from this callback.
            // This will result on a frame rate of  approximately 30FPS, in synchrony with
            // the RGB camera driver.
            // If you need to render at a higher rate (i.e.: if you want to render complex
            // animations smoothly) you  can use RENDERMODE_CONTINUOUSLY throughout the
            // application lifecycle.
            if (mSurfaceView.getRenderMode() != GLSurfaceView.RENDERMODE_WHEN_DIRTY) {
                mSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
            }
            fireFameAvailable();
            // Mark a camera frame is available for rendering in the OpenGL thread
            mSurfaceView.requestRenderUpdate();
        }
    }

    private void fireFameAvailable() {
        for (TangoUpdaterListener listener : mTangoUpdaterListeners){
            listener.onFrameAvailable();
        }
    }

    @Override
    public void onXyzIjAvailable(TangoXyzIjData xyzIj) {
        mTangoUx.updateXyzCount(xyzIj.xyzCount);
        // Save the cloud and point data for later use.
        mPointCloudManager.updateXyzIj(xyzIj);
    }

    @Override
    public void onTangoEvent(TangoEvent event) {
        mTangoUx.updateTangoEvent(event);
    }

    public synchronized void setTangoLocalization(boolean localization) {
        if (isLocalized == localization) return;
        isLocalized = localization;
        for (TangoUpdaterListener updaterListener : mTangoUpdaterListeners){
            updaterListener.onLocalization(isLocalized);
        }
    }

    public synchronized void addTangoUpdaterListener(TangoUpdaterListener listener){
        mTangoUpdaterListeners.add(listener);
    }

    public synchronized void removeTangoUpdaterListener(TangoUpdaterListener listener){
        mTangoUpdaterListeners.remove(listener);
    }

    public synchronized void addValidPoseListener(ValidPoseListener listener) {
        mValidPoseListeners.add(listener);
    }

    public synchronized void removeValidPoseListener(ValidPoseListener listener) {
        mValidPoseListeners.remove(listener);
    }

    private void checkAndFireOnValidPose(final TangoPoseData poseData) {
        if (poseData.statusCode != TangoPoseData.POSE_VALID) return;
        for (ValidPoseListener listener : mValidPoseListeners) {
            listener.onValidPose(poseData);
        }
    }

    public interface ValidPoseListener {
        void onValidPose(TangoPoseData data);
    }

    public interface TangoUpdaterListener{
        void onFrameAvailable();
        void onLocalization(boolean localization);
        int priority();
    }
}