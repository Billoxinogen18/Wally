package com.wally.wally.tango;

import com.google.atap.tangoservice.Tango;
import com.google.atap.tangoservice.TangoCameraIntrinsics;
import com.google.atap.tangoservice.TangoCoordinateFramePair;
import com.google.atap.tangoservice.TangoEvent;
import com.google.atap.tangoservice.TangoPoseData;
import com.google.atap.tangoservice.TangoXyzIjData;
import com.projecttango.tangosupport.TangoPointCloudManager;

import org.rajawali3d.surface.RajawaliSurfaceView;

import java.util.ArrayList;
import java.util.List;

public class TangoUpdater implements Tango.OnTangoUpdateListener {
    private WallyTangoUx mTangoUx;
    private boolean isLocalized;
    private boolean mIsFrameAvailableTangoThread;
    private RajawaliSurfaceView mSurfaceView;
    private TangoPointCloudManager mPointCloudManager;
    private List<LocalizationListener> mLocalizationListeners;
    private List<ValidPoseListener> mValidPoseListeners;
    private ArrayList<TangoCoordinateFramePair> mFramePairs;


    public TangoUpdater(WallyTangoUx tangoUx, RajawaliSurfaceView surfaceView, TangoPointCloudManager pointCloudManager) {
        mTangoUx = tangoUx;
        mSurfaceView = surfaceView;
        mPointCloudManager = pointCloudManager;
        mLocalizationListeners = new ArrayList<>();
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
            // Mark a camera frame is available for rendering in the OpenGL thread
            setFrameAvailableTangoThread(true);
            mSurfaceView.requestRenderUpdate();
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

    public synchronized boolean isFrameAvailableTangoThread() {
        return mIsFrameAvailableTangoThread;
    }

    public synchronized void setFrameAvailableTangoThread(boolean available) {
        mIsFrameAvailableTangoThread = available;
    }

    public synchronized void setTangoLocalization(boolean localization) {
        if (isLocalized == localization) return;
        isLocalized = localization;
        if (isLocalized) {
            for (LocalizationListener listener : mLocalizationListeners) {
                listener.onLocalize();
            }
        } else {
            for (LocalizationListener listener : mLocalizationListeners) {
                listener.onNotLocalize();
            }
        }
    }

    public void addLocalizationListener(LocalizationListener listener) {
        mLocalizationListeners.add(listener);
    }

    public void addValidPoseListener(ValidPoseListener listener) {
        mValidPoseListeners.add(listener);
    }

    public void removeValidPoseListener(ValidPoseListener listener) {
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

    public interface LocalizationListener {
        void onLocalize();
        void onNotLocalize();
    }
}