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
import com.wally.wally.adf.AdfInfo;
import com.wally.wally.adf.AdfManager;
import com.wally.wally.adf.AdfScheduler;
import com.wally.wally.analytics.WallyAnalytics;
import com.wally.wally.config.Config;
import com.wally.wally.config.TangoManagerConstants;
import com.wally.wally.renderer.WallyRenderer;

import org.rajawali3d.math.Quaternion;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.scene.ASceneFrameCallback;

public class TangoManager implements LocalizationListener {
    public static final TangoCoordinateFramePair FRAME_PAIR =
            new TangoCoordinateFramePair(
                    TangoPoseData.COORDINATE_FRAME_AREA_DESCRIPTION,
                    TangoPoseData.COORDINATE_FRAME_DEVICE
            );
    private static final String TAG = TangoManager.class.getSimpleName();
    private static final int INVALID_TEXTURE_ID = -1;
    private final WallyAnalytics mAnalytics;
    private Config mConfig;


    private Tango mTango;
    private WallyTangoUx mTangoUx;
    private DeviceExtrinsics mExtrinsics;
    private TangoCameraIntrinsics mIntrinsics;
    private TangoPointCloudManager mPointCloudManager;

    private WallyRenderer mRenderer;

    private double mCameraPoseTimestamp = 0;

    // NOTE: suffix indicates which thread is in charge of updating
    private double mRgbTimestampGlThread;
    private int mConnectedTextureIdGlThread = INVALID_TEXTURE_ID;

    private TangoUpdater mTangoUpdater;
    private TangoFactory mTangoFactory;

    private AdfInfo savedAdf;
    private AdfInfo currentAdf;
    private AdfManager mAdfManager;
    private AdfScheduler mAdfScheduler;
    private LearningEvaluator mLearningEvaluator;

    private boolean mIsLocalized;
    private boolean mIsConnected;
    private boolean mIsLearningMode;
    private boolean mIsReadyToSaveAdf;

    private Thread mLocalizationWatchdog;
    private LocalizationState localizationState = LocalizationState.NONE;
    private int adfCounter = 0;
    private long timeForAdfLocalization;

    public TangoManager(
            Config config,
            WallyAnalytics analytics,
            TangoUpdater tangoUpdater,
            TangoPointCloudManager pointCloudManager,
            WallyRenderer wallyRenderer,
            WallyTangoUx tangoUx,
            TangoFactory tangoFactory,
            AdfManager adfManager,
            LearningEvaluator evaluator
    ) {
        mConfig = config;
        mAnalytics = analytics;
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

    public synchronized void onPause() {
        Log.d(TAG, "Enter onPause()");
        // Synchronize against disconnecting while the service is being used
        // in OpenGL thread or in UI thread.

        // NOTE: DO NOT lock against this same object in the Tango callback thread.
        // Tango.disconnect will block here until all Tango callback calls are finished.
        // If you lock against this object in a Tango callback thread it will cause a deadlock.
        if (mIsConnected) {
            mRenderer.getCurrentScene().clearFrameCallbacks();
            mTango.disconnectCamera(TangoCameraIntrinsics.TANGO_CAMERA_COLOR);
            // We need to invalidate the connected texture ID,
            // this will cause re-connection in the OpenGL thread after resume
            mConnectedTextureIdGlThread = INVALID_TEXTURE_ID;

            mIsConnected = false;
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
            localizationState = LocalizationState.AFTER_ON_RESUME;
            startLocalizationWatchDog();
            // TODO: what happens if can't localize on saved ADF?
        } else {
            int timeout = mConfig.getInt(TangoManagerConstants.LOCALIZATION_TIMEOUT);
            adfCounter = 0;
            mAdfScheduler = new AdfScheduler(mAdfManager)
                    .withTimeout(timeout)
                    .addListener(createAdfSchedulerListener());
            // TODO: probably should be injected in constructor
            mAdfScheduler.start();
        }
    }

    private AdfScheduler.AdfSchedulerListener createAdfSchedulerListener() {
        return new AdfScheduler.AdfSchedulerListener() {
            @Override
            public void onNewAdfSchedule(AdfInfo info) {
                adfCounter++;
                logLocalization(false);
                if (mIsLocalized) {
                    return;
                }
                if (info == null) {
                    mAnalytics.logAdfNumberBeforeLearning(adfCounter);
                    startLearning();
                } else {
                    localizationState = LocalizationState.AFTER_DOWNLOAD;
                    startLocalizing(info);
                }
            }

            @Override
            public void onScheduleFinish() {
                // TODO rethink this shit!
                this.onNewAdfSchedule(null);
            }
        };
    }

    private void prepareForLearning() {
        mAdfScheduler.finish();
        mIsLearningMode = true;
        mIsReadyToSaveAdf = false;
        mLearningEvaluator.addLearningEvaluatorListener(new LearningEvaluator.LearningEvaluatorListener() {
            @Override
            public void onLearningFinish() {
                mIsReadyToSaveAdf = true;
                mTangoUpdater.removeValidPoserListener(mLearningEvaluator);
                if (mIsLocalized) {
                    finishLearning();
                }
            }

            @Override
            public void onLearningFailed() {
                mTangoUpdater.removeValidPoserListener(mLearningEvaluator);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        startLearning();
                    }
                }).start();
            }
        });
        mTangoUpdater.addValidPoseListener(mLearningEvaluator);
    }

    private synchronized void finishLearning() {
        Log.d(TAG, "finishLearning() called with: " + "");
        saveAdf();
        String msg = mConfig.getString(TangoManagerConstants.NEW_ROOM_LEARNED);
        mTangoUx.showCustomMessage(msg, 500);
        onPause();
        localizeWithLearnedAdf(currentAdf);
    }

    private void saveAdf() {
        Log.d(TAG, "saveAdf() called with: " + "");
        String uuid = mTango.saveAreaDescription();
        mIsLearningMode = false;
        AdfInfo info = new AdfInfo().withUuid(uuid);
        savedAdf = info;
        currentAdf = info;
        mIsReadyToSaveAdf = false;
        mAnalytics.onAdfCreate();
    }

    private synchronized void startLearning() {
        Log.d(TAG, "startLearning()");
        String msg = mConfig.getString(TangoManagerConstants.LEARNING_AREA);
        mTangoUx.showCustomMessage(msg);
        prepareForLearning();
        currentAdf = null;
        savedAdf = null;
        if (mIsConnected) {
            onPause();
        }
        mTango = mTangoFactory.getTangoForLearning(getRunnable());
    }

    private void startLocalizationWatchDog() {
        mLocalizationWatchdog = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(20000);
                } catch (InterruptedException e) {
                    return;
                }
                logLocalization(false);
                savedAdf = null;
                onPause();
                onResume();
            }
        });
        mLocalizationWatchdog.start();
    }

    private void localizeWithLearnedAdf(final AdfInfo adf) {
        Log.d(TAG, "localizeWithLearnedAdf() called with: " + "adf = [" + adf + "]");
        timeForAdfLocalization = System.currentTimeMillis();

        currentAdf = adf;

        String msg = mConfig.getString(TangoManagerConstants.LOCALIZING_IN_NEW_AREA);
        mTangoUx.showCustomMessage(msg);
        mTango = mTangoFactory.getTangoWithUuid(getRunnable(), adf.getUuid());
        localizationState = LocalizationState.AFTER_LEARNING;
        startLocalizationWatchDog();
    }

    private synchronized void startLocalizing(final AdfInfo adf) {
        Log.d(TAG, "startLocalizing() called with: " + "adf = [" + adf + "]");
        timeForAdfLocalization = System.currentTimeMillis();
        String msg = mConfig.getString(TangoManagerConstants.LOCALIZING_IN_KNOWN_AREA);
        mTangoUx.showCustomMessage(msg);
        currentAdf = adf; //TODO mchirdeba tu ara?
        if (!mIsConnected) {
            final TangoFactory.RunnableWithError r = getRunnable();
            mTango = mTangoFactory.getTango(new TangoFactory.RunnableWithError() {
                @Override
                public void run() {
                    r.run();
                    loadAdf(mTango, adf);
                }

                @Override
                public void onError(Exception e) {
                    r.onError(e);
                }
            });
        } else {
            loadAdf(mTango, adf);
        }
    }

    private void loadAdf(Tango tango, AdfInfo adf) {
        if (TangoUtils.isAdfImported(tango, adf.getUuid())) {
            tango.experimentalLoadAreaDescription(adf.getUuid());
        } else {
            tango.experimentalLoadAreaDescriptionFromFile(adf.getPath());
        }
    }

    private TangoFactory.RunnableWithError getRunnable() {
        return new TangoFactory.RunnableWithError() {
            @Override
            public void run() {
                // Connect TangoUpdater and tango
                mTango.connectListener(mTangoUpdater.getFramePairs(), mTangoUpdater);
                // Get extrinsics (needs to be done after connecting Tango and listeners)
                mExtrinsics = TangoUtils.getDeviceExtrinsics(mTango);
                mIntrinsics = mTango.getCameraIntrinsics(TangoCameraIntrinsics.TANGO_CAMERA_COLOR);
                connectRenderer();
                mIsConnected = true;
            }

            @Override
            public void onError(Exception e) {
                mTangoUx.showTangoOutOfDate();
            }
        };
    }

    /**
     * / * Finds plane pose in the middle of the screen.
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

    /**
     * Connects the view and renderer to the color camara and callbacks.
     */
    private void connectRenderer() {
        // Register a Rajawali Scene Frame Callback to update the scene camera pose whenever a new
        // RGB frame is rendered.
        // (@see https://github.com/Rajawali/Rajawali/wiki/Scene-Frame-Callbacks)
        mRenderer.getCurrentScene().clearFrameCallbacks();
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

    public boolean isLocalized() {
        return mIsLocalized;
    }

    @Override
    public void localized() {
        Log.d(TAG, "localized() called with: " + "");

        if (mLocalizationWatchdog != null) {
            mLocalizationWatchdog.interrupt();
        }

        mIsLocalized = true;
        mAdfScheduler.finish();
        if (currentAdf != null) savedAdf = currentAdf;
        if (mIsReadyToSaveAdf && mIsLearningMode) {
            finishLearning();
        }
        if (!mIsLearningMode) {
            //mTangoUx.hideCustomMessage();
            String msg = mConfig.getString(TangoManagerConstants.LOCALIZED);
            mTangoUx.showCustomMessage(msg, 1000);
        }
        logLocalization(true);
    }

    @Override
    public void notLocalized() {
        Log.d(TAG, "notLocalized() called with: " + "");
        mIsLocalized = false;
        String msg;
        if (mIsLearningMode) {
            msg = mConfig.getString(TangoManagerConstants.LOCALIZATION_LOST_IN_LEARNING);
            mLearningEvaluator.stop();
        } else {
            msg = mConfig.getString(TangoManagerConstants.LOCALIZATION_LOST);
        }
        mTangoUx.showCustomMessage(msg);
    }

    public AdfInfo getCurrentAdf() {
        return currentAdf;
    }

    public boolean isLearningMode() {
        return mIsLearningMode;
    }

    public boolean isConnected() {
        return mIsConnected;
    }

    private void logLocalization(boolean success){
        if (localizationState == LocalizationState.AFTER_LEARNING){
            mAnalytics.onLocalizeOnNewAdf(success);
            localizationState = LocalizationState.NONE;
        } else if (localizationState == LocalizationState.AFTER_ON_RESUME){
            mAnalytics.onLocalizeAfterResume(success);
            localizationState = LocalizationState.NONE;
        } else if (localizationState == LocalizationState.AFTER_DOWNLOAD){
            mAnalytics.onLocalizeOnDownloadedAdf(success);
            localizationState = LocalizationState.NONE;
            if (success){
                mAnalytics.logAdfNumberBeforeLocalization(adfCounter);
            }
        }
        if (success && localizationState != LocalizationState.AFTER_LEARNING){
            mAnalytics.logLocalizationTimeForAdf("NAN", System.currentTimeMillis() - timeForAdfLocalization);
        }
    }

    enum LocalizationState {
        NONE, AFTER_LEARNING, AFTER_ON_RESUME, AFTER_DOWNLOAD
    }
}
