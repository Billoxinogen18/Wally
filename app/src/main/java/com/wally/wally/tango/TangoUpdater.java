package com.wally.wally.tango;

import android.util.Log;

import com.google.atap.tangoservice.Tango;
import com.google.atap.tangoservice.TangoCameraIntrinsics;
import com.google.atap.tangoservice.TangoEvent;
import com.google.atap.tangoservice.TangoPoseData;
import com.google.atap.tangoservice.TangoXyzIjData;
import com.projecttango.tangosupport.TangoPointCloudManager;
import com.wally.wally.components.WallyTangoUx;

import org.rajawali3d.surface.RajawaliSurfaceView;

import java.util.ArrayList;
import java.util.List;

public class TangoUpdater implements Tango.OnTangoUpdateListener {
    private static final String TAG = TangoUpdater.class.getSimpleName();

    private WallyTangoUx mTangoUx;
    private boolean isLocalized;
    private boolean mIsFrameAvailableTangoThread;
    private RajawaliSurfaceView mSurfaceView;
    private TangoPointCloudManager mPointCloudManager;
    private List<LocalizationListener> mLocalizationListeners;
    private List<ValidPoseListener> mValidPoseListeners;


    public TangoUpdater(WallyTangoUx tangoUx, RajawaliSurfaceView surfaceView, TangoPointCloudManager pointCloudManager) {
        mTangoUx = tangoUx;
        mSurfaceView = surfaceView;
        mPointCloudManager = pointCloudManager;
        mLocalizationListeners = new ArrayList<>();
        mValidPoseListeners = new ArrayList<>();
    }

    @Override
    public void onPoseAvailable(TangoPoseData pose) {
        if (mTangoUx != null) {
            mTangoUx.updatePoseStatus(pose.statusCode);
        }
        if (pose.statusCode != TangoPoseData.POSE_VALID) {
            setTangoLocalization(false);
            if (mTangoUx != null) {
                mTangoUx.showCustomMessage("Hold Still");
                //setTangoLocalization(false);
            }
        } else if (pose.baseFrame == TangoPoseData.COORDINATE_FRAME_AREA_DESCRIPTION && pose.targetFrame == TangoPoseData.COORDINATE_FRAME_START_OF_SERVICE) {
            setTangoLocalization(true);
            if (mTangoUx != null) {
               // mTangoUx.hideCustomMessage();
            }
        } else if (!isTangoLocalized()) {
            if (mTangoUx != null) {
                mTangoUx.showCustomMessage("Walk around!");
            }
        }

        if (pose.statusCode == TangoPoseData.POSE_VALID) {
            fireOnValidPose(pose);
        }
    }

    @Override
    public void onFrameAvailable(int cameraId) {
        // Check if the frame available is for the camera we want and update its frame
        // on the view.
        if (cameraId == TangoCameraIntrinsics.TANGO_CAMERA_COLOR) {
            // Mark a camera frame is available for rendering in the OpenGL thread
            setFrameAvailableTangoThread(true);
            mSurfaceView.requestRenderUpdate();
        }
    }

    @Override
    public void onXyzIjAvailable(TangoXyzIjData xyzIj) {
        if (mTangoUx != null) {
            mTangoUx.updateXyzCount(xyzIj.xyzCount);
        }
        // Save the cloud and point data for later use.
        mPointCloudManager.updateXyzIj(xyzIj);
    }

    @Override
    public void onTangoEvent(TangoEvent event) {
        Log.d(TAG, "onTangoEvent() called with: " + "event = [" + event.eventKey + "]");
        if (mTangoUx != null) {
            mTangoUx.updateTangoEvent(event);
        }
    }

    public synchronized boolean isFrameAvailableTangoThread() {
        return mIsFrameAvailableTangoThread;
    }

    public synchronized void setFrameAvailableTangoThread(boolean available) {
        mIsFrameAvailableTangoThread = available;
    }

    private synchronized boolean isTangoLocalized() {
        return isLocalized;
    }

    public synchronized void setTangoLocalization(boolean localization) {
        if (isLocalized != localization) {
            isLocalized = localization;
            if (isLocalized) {
                for(LocalizationListener listener : mLocalizationListeners) {
                    listener.localized();
                }
            } else {
                for(LocalizationListener listener : mLocalizationListeners) {
                    listener.notLocalized();
                }
            }
        }
    }

    public synchronized void addLocalizationListener(LocalizationListener listener){
        mLocalizationListeners.add(listener);
    }

    public void addValidPoseListener(ValidPoseListener listener) {
        mValidPoseListeners.add(listener);
    }

    public void removeValidPoserListener(ValidPoseListener listener) {
        mValidPoseListeners.remove(listener);
    }

    private void fireOnValidPose(TangoPoseData poseData) {
        for (ValidPoseListener listener : mValidPoseListeners) {
            listener.onValidPose(poseData);
        }
    }

    public interface ValidPoseListener {
        void onValidPose(TangoPoseData data);
    }
}
