package com.wally.wally.tango;

import android.util.Log;

import com.google.atap.tango.ux.TangoUx;
import com.google.atap.tangoservice.Tango;
import com.google.atap.tangoservice.TangoCameraIntrinsics;
import com.google.atap.tangoservice.TangoCoordinateFramePair;
import com.google.atap.tangoservice.TangoException;
import com.google.atap.tangoservice.TangoPoseData;
import com.google.atap.tangoservice.TangoXyzIjData;
import com.projecttango.rajawali.DeviceExtrinsics;
import com.projecttango.rajawali.Pose;
import com.projecttango.rajawali.ScenePoseCalculator;
import com.projecttango.tangosupport.TangoPointCloudManager;
import com.projecttango.tangosupport.TangoSupport;
import com.wally.wally.adfCreator.AdfInfo;
import com.wally.wally.adfCreator.AdfManager;
import com.wally.wally.components.WallyTangoUx;
import com.wally.wally.datacontroller.adf.AdfMetaData;
import com.wally.wally.datacontroller.callbacks.Callback;

import org.rajawali3d.math.Quaternion;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.scene.ASceneFrameCallback;

import java.util.ArrayList;

public class TangoManager implements LocalizationListener {
    private static final String TAG = TangoManager.class.getSimpleName();

    public static final TangoCoordinateFramePair FRAME_PAIR =
            new TangoCoordinateFramePair(
                    TangoPoseData.COORDINATE_FRAME_AREA_DESCRIPTION,
                    TangoPoseData.COORDINATE_FRAME_DEVICE
            );
    private static final int INVALID_TEXTURE_ID = -1;
    private static final int ADF_LOCALIZAION_TIMEOUT_MS = 15000;


    private Tango mTango;
    private WallyTangoUx mTangoUx;
    private DeviceExtrinsics mExtrinsics;
    private TangoCameraIntrinsics mIntrinsics;
    private TangoPointCloudManager mPointCloudManager;

    private WallyRenderer mRenderer;

    private double mCameraPoseTimestamp = 0;

    private boolean mIsConnected;

    // Texture rendering related fields
    // NOTE: Naming indicates which thread is in charge of updating this variable
    private int mConnectedTextureIdGlThread = INVALID_TEXTURE_ID;
    private double mRgbTimestampGlThread;

    private TangoUpdater mTangoUpdater;
    private TangoFactory mTangoFactory;

    private AdfInfo savedAdf;
    private AdfInfo currentAdf;
    private AdfManager mAdfManager;

    private boolean mIsLocalized;
    private boolean mIsReadyToSaveAdf;

    private boolean mIsLearningMode;
    private AdfScheduler mAdfScheduler;
    private LearningEvaluator mLearningEvaluator;


    public TangoManager(
            TangoUpdater tangoUpdater,
            TangoPointCloudManager pointCloudManager,
            WallyRenderer wallyRenderer,
            WallyTangoUx tangoUx,
            TangoFactory tangoFactory,
            AdfManager adfManager,
            LearningEvaluator evaluator
    ) {
        mTangoUpdater = tangoUpdater;
        mRenderer = wallyRenderer;
        mTangoUx = tangoUx;
        TangoUx.StartParams params = new TangoUx.StartParams();
        params.showConnectionScreen = false;
        mTangoUx.start(params);
        mPointCloudManager = pointCloudManager;
        mTangoFactory = tangoFactory;
        mAdfManager = adfManager;
        mLearningEvaluator = evaluator;
        mTangoUpdater.addLocalizationListener(this);
    }

//    public TangoManager(TangoUpdater tangoUpdater, TangoPointCloudManager pointCloudManager, WallyRenderer wallyRenderer,
//                        WallyTangoUx tangoUx, TangoFactory tangoFactory, AdfManager adfManager, LearningEvaluator evaluator) {
//        this(tangoUpdater, pointCloudManager, wallyRenderer, tangoUx, tangoFactory, adfManager, evaluator);
//    }

    /**
     * Calculates and stores the fixed transformations between the device and
     * the various sensors to be used later for transformations between frames.
     */
    private static DeviceExtrinsics setupExtrinsics(Tango tango) {
        // Create camera to IMU transform.
        TangoCoordinateFramePair framePair = new TangoCoordinateFramePair();
        framePair.baseFrame = TangoPoseData.COORDINATE_FRAME_IMU;
        framePair.targetFrame = TangoPoseData.COORDINATE_FRAME_CAMERA_COLOR;
        TangoPoseData imuTrgbPose = tango.getPoseAtTime(0.0, framePair);

        // Create device to IMU transform.
        framePair.targetFrame = TangoPoseData.COORDINATE_FRAME_DEVICE;
        TangoPoseData imuTdevicePose = tango.getPoseAtTime(0.0, framePair);

        // Create depth camera to IMU transform.
        framePair.targetFrame = TangoPoseData.COORDINATE_FRAME_CAMERA_DEPTH;
        TangoPoseData imuTdepthPose = tango.getPoseAtTime(0.0, framePair);

        return new DeviceExtrinsics(imuTdevicePose, imuTrgbPose, imuTdepthPose);
    }


    public synchronized void onPause() {
        Log.d(TAG, "Enter onPause()");
        // Synchronize against disconnecting while the service is being used
        // in OpenGL thread or in UI thread.

        // NOTE: DO NOT lock against this same object in the Tango callback thread.
        // Tango.disconnect will block here until all Tango callback calls are finished.
        // If you lock against this object in a Tango callback thread it will cause a deadlock.
        if (mIsConnected) {
            mIsConnected = false;
            mRenderer.getCurrentScene().clearFrameCallbacks();
            mTango.disconnectCamera(TangoCameraIntrinsics.TANGO_CAMERA_COLOR);
            // We need to invalidate the connected texture ID,
            // this will cause re-connection in the OpenGL thread after resume
            mConnectedTextureIdGlThread = INVALID_TEXTURE_ID;
        }
        if (mTango != null) {
            mTango.disconnect();
        }
        mTangoUpdater.setTangoLocalization(false);
    }

    public synchronized void onResume() {
        Log.d(TAG, "Enter onResume()");
        if (savedAdf != null) {
            startLocalizing(savedAdf);
            // TODO: what happens if can't localize on saved ADF?
        } else {
            mAdfScheduler = new AdfScheduler(mAdfManager)
                    .withTimeout(ADF_LOCALIZAION_TIMEOUT_MS)
                    .addCallback(adfSchedulerCallback());
            // TODO: probably should be injected in constructor
            mAdfScheduler.start();
        }
    }

    private Callback<AdfInfo> adfSchedulerCallback() {
        return new Callback<AdfInfo>() {
            @Override
            public void onResult(AdfInfo result) {
                if (mIsLocalized) {
                    return;
                }
                Log.d(TAG, "adfSchedulerCallback onResult: " + result);
                if (result == null) {
                    startLearning();
                } else {
                    startLocalizing(result);
                }
            }

            @Override
            public void onError(Exception e) {
                Log.d(TAG, "adfSchedulerCallback onError: " + e);
            }
        };
    }


    private synchronized void prepareForLearning() {
        mAdfScheduler.finish();
        mIsLearningMode = true;
        mLearningEvaluator.addLearningEvaluatorListener(new LearningEvaluator.LearningEvaluatorListener() {
            @Override
            public void onLearningFinish() {
                mIsReadyToSaveAdf = true;
                mTangoUpdater.removeValidPoserListener(mLearningEvaluator);
                if (isLocalized()) {
                    finishLearning();
                }
            }

            @Override
            public void onLearningFailed() {
                //TODO
            }
        });
        mTangoUpdater.addValidPoseListener(mLearningEvaluator);
        mTangoUx.showCustomMessage("Learning new room...");
    }

    private void finishLearning() {
        Log.d(TAG, "finishLearning() called with: " + "");
        saveAdf();
        mTangoUx.showCustomMessage("New room was learned.");
        onPause();
        localizeWithLearnedAdf(currentAdf);
    }

    private synchronized void saveAdf() {
        Log.d(TAG, "saveAdf() called with: " + "");
        String uuid = mTango.saveAreaDescription();
        mIsLearningMode = false;
        AdfInfo adfInfo = new AdfInfo().withUuid(uuid).withMetaData(new AdfMetaData(uuid, uuid, null));
        savedAdf = adfInfo;
        currentAdf = adfInfo;
        mIsReadyToSaveAdf = false;
    }

    private synchronized void startLearning() {
        Log.d(TAG, "startLearning() called with: " + "");
        prepareForLearning();
        currentAdf = null;
        savedAdf = null;
        if (mIsConnected) {
            onPause();
        }
        mTango = mTangoFactory.getTangoForLearning(getRunnable());
    }

    private synchronized void localizeWithLearnedAdf(final AdfInfo adf){
        Log.d(TAG, "localizeWithLearnedAdf() called with: " + "adf = [" + adf + "]");
        currentAdf = adf;
        mTango = mTangoFactory.getTangoWithUuid(getRunnable(), adf.getUuid());
    }

    private synchronized void startLocalizing(final AdfInfo adf) {
        Log.d(TAG, "startLocalizing() called with: " + "adf = [" + adf + "]");
        currentAdf = adf; //TODO mchirdeba tu ara?
        if (!mIsConnected) {
            mTango = mTangoFactory.getTango(new TangoFactory.RunnableWithError() {
                @Override
                public void run() {
                    getRunnable().run();
                    if (isAdfImported(adf)) {
                        mTango.experimentalLoadAreaDescription(adf.getUuid());
                    } else {
                        mTango.experimentalLoadAreaDescriptionFromFile(adf.getPath());
                    }
                }

                @Override
                public void onError(Exception e) {
                    getRunnable().onError(e);
                }
            });
        } else {
            if (isAdfImported(adf)) {
                mTango.experimentalLoadAreaDescription(adf.getUuid());
            } else {
                mTango.experimentalLoadAreaDescriptionFromFile(adf.getPath());
            }
        }

    }

    private TangoFactory.RunnableWithError getRunnable() {
        return new TangoFactory.RunnableWithError() {
            @Override
            public void run() {
                finishTangoConnection();
                connectRenderer();
                mIsConnected = true;
            }

            @Override
            public void onError(Exception e) {
                mTangoUx.showTangoOutOfDate();
            }
        };
    }

    public boolean isConnected() {
        return mIsConnected;
    }

    /**
     * Finds plane pose in the middle of the screen.
     */
    public TangoPoseData findPlaneInMiddle() {
        return doFitPlane(0.5f, 0.5f, mRgbTimestampGlThread);
    }

    public Pose getDevicePoseInFront() {
        TangoPoseData devicePose =
                mTango.getPoseAtTime(mRgbTimestampGlThread, FRAME_PAIR);
        Pose p = ScenePoseCalculator.toOpenGlCameraPose(devicePose, mExtrinsics);

        Vector3 addto = p.getOrientation().multiply(new Vector3(0, 0, -1));
        Quaternion rot = new Quaternion().fromAngleAxis(addto, 180);

        return new Pose(p.getPosition().add(addto), p.getOrientation().multiply(rot));

    }

    private void finishTangoConnection() {
        mTango.connectListener(mTangoUpdater.getFramePairs(), mTangoUpdater);

        // Get extrinsics from device for use in transforms. This needs
        // to be done after connecting Tango and listeners.
        mExtrinsics = setupExtrinsics(mTango);
        mIntrinsics = mTango.getCameraIntrinsics(TangoCameraIntrinsics.TANGO_CAMERA_COLOR);
    }

    private boolean isAdfImported(AdfInfo adf) {
        String uuid = adf.getUuid();
        if (uuid == null) return false;
        ArrayList<String> l = mTango.listAreaDescriptions();
        for (String id : l) {
            if (id.equals(uuid)) return true;
        }
        return false;
    }

    /**
     * Connects the view and renderer to the color camara and callbacks.
     */
    private void connectRenderer() {
        // Register a Rajawali Scene Frame Callback to update the scene camera pose whenever a new
        // RGB frame is rendered.
        // (@see https://github.com/Rajawali/Rajawali/wiki/Scene-Frame-Callbacks)
        mRenderer.getCurrentScene().registerFrameCallback(new ASceneFrameCallback() {
            @Override
            public void onPreFrame(long sceneTime, double deltaTime) {
                // NOTE: This is called from the OpenGL render thread, after all the renderer
                // onRender callbacks had a chance to run and before scene objects are rendered
                // into the scene.

                // Prevent concurrent access to {@code mIsFrameAvailableTangoThread} from the Tango
                // callback thread and service disconnection from an onPause event.
                synchronized (TangoManager.this) {
                    // Don't execute any tango API actions if we're not connected to the service
                    if (!mIsConnected) {
                        return;
                    }

                    // Set-up scene camera projection to match RGB camera intrinsics
                    if (!mRenderer.isSceneCameraConfigured()) {
                        mRenderer.setProjectionMatrix(mIntrinsics);
                    }

                    // Connect the camera texture to the OpenGL Texture if necessary
                    // NOTE: When the OpenGL context is recycled, Rajawali may re-generate the
                    // texture with a different ID.
                    if (mConnectedTextureIdGlThread != mRenderer.getTextureId()) {
                        mTango.connectTextureId(TangoCameraIntrinsics.TANGO_CAMERA_COLOR,
                                mRenderer.getTextureId());
                        mConnectedTextureIdGlThread = mRenderer.getTextureId();
                        Log.d(TAG, "connected to texture id: " + mRenderer.getTextureId());
                    }

                    // If there is a new RGB camera frame available, update the texture with it
                    if (mTangoUpdater.isFrameAvailableTangoThread()) {
                        mRgbTimestampGlThread =
                                mTango.updateTexture(TangoCameraIntrinsics.TANGO_CAMERA_COLOR);
                        mTangoUpdater.setFrameAvailableTangoThread(false);
                    }

                    // If a new RGB frame has been rendered, update the camera pose to match.
                    if (mRgbTimestampGlThread > mCameraPoseTimestamp) {
                        // Calculate the device pose at the camera frame update time.
                        try {
                            TangoPoseData lastFramePose = mTango.getPoseAtTime(mRgbTimestampGlThread, FRAME_PAIR);
                            if (lastFramePose.statusCode == TangoPoseData.POSE_VALID) {
                                // Update the camera pose from the renderer
                                mRenderer.updateRenderCameraPose(lastFramePose, mExtrinsics);
                                mCameraPoseTimestamp = lastFramePose.timestamp;
                            } else {
                                //Log.v(TAG, "Can't get device pose at time: " + mRgbTimestampGlThread);
                            }
                        } catch (TangoException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onPreDraw(long sceneTime, double deltaTime) {

            }

            @Override
            public void onPostFrame(long sceneTime, double deltaTime) {

            }

            @Override
            public boolean callPreFrame() {
                return true;
            }
        });
    }

    /**
     * Use the TangoSupport library with point cloud data to calculate the plane
     * of the world feature pointed at the location the camera is looking.
     * It returns the pose of the fitted plane in a TangoPoseData structure.
     */
    private TangoPoseData doFitPlane(float u, float v, double rgbTimestamp) {
        TangoXyzIjData xyzIj = mPointCloudManager.getLatestXyzIj();

        if (xyzIj == null) {
            return null;
        }

        // We need to calculate the transform between the color camera at the
        // time the user clicked and the depth camera at the time the depth
        // cloud was acquired.
        TangoPoseData colorTdepthPose = TangoSupport.calculateRelativePose(
                rgbTimestamp, TangoPoseData.COORDINATE_FRAME_CAMERA_COLOR,
                xyzIj.timestamp, TangoPoseData.COORDINATE_FRAME_CAMERA_DEPTH);


        // Perform plane fitting with the latest available point cloud data.
        TangoSupport.IntersectionPointPlaneModelPair intersectionPointPlaneModelPair =
                TangoSupport.fitPlaneModelNearClick(xyzIj, mIntrinsics,
                        colorTdepthPose, u, v);

        // Get the device pose at the time the plane data was acquired.
        TangoPoseData devicePose = mTango.getPoseAtTime(xyzIj.timestamp, FRAME_PAIR);

        // Update the AR object location.

        return ScenePoseCalculator.planeFitToTangoWorldPose(
                intersectionPointPlaneModelPair.intersectionPoint,
                intersectionPointPlaneModelPair.planeModel, devicePose, mExtrinsics);
    }


    @Override
    public void localized() {
        Log.d(TAG, "localized() called with: " + "");
        mIsLocalized = true;
        mAdfScheduler.finish();
        Log.d(TAG, "localized() mAdfScheduler.finish() was called");
        if (currentAdf != null) savedAdf = currentAdf;
        if (mIsReadyToSaveAdf && mIsLearningMode) {
            finishLearning();
        }
        if (!mIsLearningMode) {
            mTangoUx.hideCustomMessage();
        }
    }

    @Override
    public void notLocalized() {
        Log.d(TAG, "notLocalized() called with: " + "");
        mIsLocalized = false;
        if (mIsLearningMode) {
            mTangoUx.showCustomMessage("Learning New Room. Walk Around");
        } else {
            mTangoUx.showCustomMessage("Walk Around");
        }
    }

    public synchronized boolean isLocalized() {
        return mIsLocalized;
    }


    public AdfInfo getCurrentAdf() {
        return currentAdf;
    }

    public boolean isLearningMode() {
        return mIsLearningMode;
    }

}
