package com.wally.wally.tango;

import android.net.Uri;
import android.util.Log;

import com.bumptech.glide.util.Util;
import com.google.atap.tango.ux.TangoUx;
import com.google.atap.tangoservice.Tango;
import com.google.atap.tangoservice.TangoCameraIntrinsics;
import com.google.atap.tangoservice.TangoConfig;
import com.google.atap.tangoservice.TangoCoordinateFramePair;
import com.google.atap.tangoservice.TangoException;
import com.google.atap.tangoservice.TangoOutOfDateException;
import com.google.atap.tangoservice.TangoPoseData;
import com.google.atap.tangoservice.TangoXyzIjData;
import com.projecttango.rajawali.DeviceExtrinsics;
import com.projecttango.rajawali.Pose;
import com.projecttango.rajawali.ScenePoseCalculator;
import com.projecttango.tangosupport.TangoPointCloudManager;
import com.projecttango.tangosupport.TangoSupport;
import com.wally.wally.Utils;
import com.wally.wally.adfCreator.AdfInfo;
import com.wally.wally.adfCreator.AdfManager;
import com.wally.wally.components.WallyTangoUx;
import com.wally.wally.datacontroller.adf.AdfMetaData;

import org.rajawali3d.math.Quaternion;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.scene.ASceneFrameCallback;

import java.util.ArrayList;

/**
 * Created by shota on 4/21/16.
 */
public class TangoManager implements LocalizationListener{
    public static final TangoCoordinateFramePair FRAME_PAIR = new TangoCoordinateFramePair(
            TangoPoseData.COORDINATE_FRAME_AREA_DESCRIPTION,
            TangoPoseData.COORDINATE_FRAME_DEVICE);
    private static final String TAG = TangoManager.class.getSimpleName();
    private static final int INVALID_TEXTURE_ID = -1;
    private static final long ADF_LOCALIZAION_TIMEOUT = 15000;


    private TangoCameraIntrinsics mIntrinsics;
    private DeviceExtrinsics mExtrinsics;
    private TangoPointCloudManager mPointCloudManager;
    private Tango mTango;
    private WallyTangoUx mTangoUx;

    private WallyRenderer mRenderer;

    private String mAdfUuid;
    private double mCameraPoseTimestamp = 0;

    private boolean mIsConnected;

    // Texture rendering related fields
    // NOTE: Naming indicates which thread is in charge of updating this variable
    private int mConnectedTextureIdGlThread = INVALID_TEXTURE_ID;
    private double mRgbTimestampGlThread;

    private TangoUpdater mTangoUpdater;
    private TangoFactory mTangoFactory;

    //testing adflist
    private AdfManager mAdfManager;
    private AdfInfo currentAdf = null;
    private AdfInfo savedAdf = null;
    private long mLocalizationTimeout = ADF_LOCALIZAION_TIMEOUT;

    private boolean mIsLocalized;



    public TangoManager(TangoUpdater tangoUpdater, TangoPointCloudManager pointCloudManager, WallyRenderer wallyRenderer,
                        WallyTangoUx tangoUx, TangoFactory tangoFactory, AdfManager adfManager, long localizationTimeout) {
        mTangoUpdater = tangoUpdater;
        mRenderer = wallyRenderer;
        mTangoUx = tangoUx;
        mPointCloudManager = pointCloudManager;
        mTangoFactory = tangoFactory;
        mAdfManager = adfManager;
        mLocalizationTimeout = localizationTimeout;
    }

    public TangoManager(TangoUpdater tangoUpdater, TangoPointCloudManager pointCloudManager, WallyRenderer wallyRenderer,
                        WallyTangoUx tangoUx, TangoFactory tangoFactory, AdfManager adfManager) {
        this(tangoUpdater,pointCloudManager,wallyRenderer,tangoUx,tangoFactory,adfManager,ADF_LOCALIZAION_TIMEOUT);
    }

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
        // Synchronize against disconnecting while the service is being used in the OpenGL thread or
        // in the UI thread.
        // NOTE: DO NOT lock against this same object in the Tango callback thread. Tango.disconnect
        // will block here until all Tango callback calls are finished. If you lock against this
        // object in a Tango callback thread it will cause a deadlock.
        if (mIsConnected) {
            if (mIsLocalized){
                savedAdf = currentAdf;
            } else {
                savedAdf = null;
            }
            mIsConnected = false;
            mRenderer.getCurrentScene().clearFrameCallbacks();
            mTango.disconnectCamera(TangoCameraIntrinsics.TANGO_CAMERA_COLOR);
            // We need to invalidate the connected texture ID so that we cause a re-connection
            // in the OpenGL thread after resume
            mConnectedTextureIdGlThread = INVALID_TEXTURE_ID;
        }
        mTango.disconnect();
        mTangoUx.stop();

        mTangoUpdater.setTangoLocalization(false);
    }

    public synchronized void onResume() {
        // Synchronize against disconnecting while the service is being used in the OpenGL thread or
        // in the UI thread.
//        if (!mIsConnected) {
//            TangoUx.StartParams params = new TangoUx.StartParams();
//            params.showConnectionScreen = false;
//            mTangoUx.start(params);
//            mTango = mTangoFactory.getTango(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        TangoSupport.initialize();
//                        connectTango();
//                        connectRenderer();
//                        mIsConnected = true;
//                    } catch (TangoOutOfDateException e) {
//                        if (mTangoUx != null) {
//                            mTangoUx.showTangoOutOfDate();
//                        }
//                    }
//                }
//            });
//        }

        resume();
    }

    private synchronized void resume(){
        if (savedAdf != null){
            currentAdf = savedAdf;
            localizeWithAdf(currentAdf);
        } else {
            while(true){
                if (mAdfManager.hasAdf()){
                    if (mAdfManager.isAdfReady()) {
                        currentAdf = mAdfManager.getAdf();
                        long localizationStarted = System.currentTimeMillis();
                        localizeWithAdf(currentAdf);

                        waitForLocalizationOrTimeout(localizationStarted, ADF_LOCALIZAION_TIMEOUT);
                        if (mIsLocalized) {
                            break;
                        } else {
                            if (!mAdfManager.hasAdf()) break;
                        }
                    } else {
                        mTangoUx.showCustomMessage("Waiting for ADF to download");
                        Utils.sleep(500);
                    }
                }
            }
            if (!mIsLocalized){
                startAreaLearning();
                initFinishThread();
            }
        }
    }

    private void initFinishThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Utils.sleep(20000);
                String uuid = mTango.saveAreaDescription();
                AdfInfo adfInfo = new AdfInfo().withUuid(uuid).withMetaData(new AdfMetaData(uuid, uuid, null));
                localizeWithAdf(adfInfo);
            }
        });
    }

    private void localizeWithAdf(final AdfInfo adf){
        // Synchronize against disconnecting while the service is being used in the OpenGL thread or
        // in the UI thread.
        if (!mIsConnected) {
            TangoUx.StartParams params = new TangoUx.StartParams();
            params.showConnectionScreen = false;
            mTangoUx.start(params);
            mTango = mTangoFactory.getTango(new Runnable() {
                @Override
                public void run() {
                    try {
                        TangoSupport.initialize();
                        connectTango(adf);
                        connectRenderer();
                        mIsConnected = true;
                    } catch (TangoOutOfDateException e) {
                        if (mTangoUx != null) {
                            mTangoUx.showTangoOutOfDate();
                        }
                    }
                }
            });
        }
    }


    private void startAreaLearning(){
        // Synchronize against disconnecting while the service is being used in the OpenGL thread or
        // in the UI thread.
        if (!mIsConnected) {
            TangoUx.StartParams params = new TangoUx.StartParams();
            params.showConnectionScreen = false;
            mTangoUx.start(params);
            mTango = mTangoFactory.getTango(new Runnable() {
                @Override
                public void run() {
                    try {
                        TangoSupport.initialize();
                        connectTangoForAreaLearning();
                        connectRenderer();
                        mIsConnected = true;
                    } catch (TangoOutOfDateException e) {
                        if (mTangoUx != null) {
                            mTangoUx.showTangoOutOfDate();
                        }
                    }
                }
            });
        }
    }

    private void waitForLocalizationOrTimeout(long localizationStarted, long timeout){
        while (true){
            Utils.sleep(100);
            if (mIsLocalized) break;
            long timeNow = System.currentTimeMillis();
            if (localizationStarted - timeNow > timeout){
                break;
            }
        }
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

    public Pose getDevicePoseInFront(){
        TangoPoseData devicePose =
                mTango.getPoseAtTime(mRgbTimestampGlThread, FRAME_PAIR);
        Pose p = ScenePoseCalculator.toOpenGlCameraPose(devicePose,mExtrinsics);

        Vector3 addto = p.getOrientation().multiply(new Vector3(0,0,-1));
        Quaternion rot = new Quaternion().fromAngleAxis(addto, 180);

        return new Pose(p.getPosition().add(addto), p.getOrientation().multiply(rot));

    }

    /**
     * Configures the Tango service and connects it to callbacks.
     */
    private void connectTango(AdfInfo adf) {
        TangoConfig config = mTango.getConfig(
                TangoConfig.CONFIG_TYPE_DEFAULT);
        config.putBoolean(TangoConfig.KEY_BOOLEAN_LOWLATENCYIMUINTEGRATION, true);
        config.putBoolean(TangoConfig.KEY_BOOLEAN_DEPTH, true);
        config.putBoolean(TangoConfig.KEY_BOOLEAN_COLORCAMERA, true);
        config.putBoolean(TangoConfig.KEY_BOOLEAN_AUTORECOVERY, true);


        if (adf.isImported()){
            config.putString(TangoConfig.KEY_STRING_AREADESCRIPTION, adf.getUuid());
        } else {

        }




        mTango.connect(config);

        ArrayList<TangoCoordinateFramePair> framePairs = new ArrayList<>();
        framePairs.add(
                new TangoCoordinateFramePair(
                        TangoPoseData.COORDINATE_FRAME_AREA_DESCRIPTION,
                        TangoPoseData.COORDINATE_FRAME_START_OF_SERVICE));
        framePairs.add(
                new TangoCoordinateFramePair(
                        TangoPoseData.COORDINATE_FRAME_START_OF_SERVICE,
                        TangoPoseData.COORDINATE_FRAME_DEVICE
                ));
        mTango.connectListener(framePairs, mTangoUpdater);

        // Get extrinsics from device for use in transforms. This needs
        // to be done after connecting Tango and listeners.
        mExtrinsics = setupExtrinsics(mTango);
        mIntrinsics = mTango.getCameraIntrinsics(TangoCameraIntrinsics.TANGO_CAMERA_COLOR);
    }


    private void connectTangoForAreaLearning() {
        TangoConfig config = mTango.getConfig(
                TangoConfig.CONFIG_TYPE_DEFAULT);
        config.putBoolean(TangoConfig.KEY_BOOLEAN_LOWLATENCYIMUINTEGRATION, true);
        config.putBoolean(TangoConfig.KEY_BOOLEAN_DEPTH, true);
        config.putBoolean(TangoConfig.KEY_BOOLEAN_COLORCAMERA, true);
        config.putBoolean(TangoConfig.KEY_BOOLEAN_AUTORECOVERY, true);
        config.putBoolean(TangoConfig.KEY_BOOLEAN_LEARNINGMODE, true);


        mTango.connect(config);

        ArrayList<TangoCoordinateFramePair> framePairs = new ArrayList<>();
        framePairs.add(
                new TangoCoordinateFramePair(
                        TangoPoseData.COORDINATE_FRAME_START_OF_SERVICE,
                        TangoPoseData.COORDINATE_FRAME_DEVICE
                ));
        mTango.connectListener(framePairs, mTangoUpdater);

        // Get extrinsics from device for use in transforms. This needs
        // to be done after connecting Tango and listeners.
        mExtrinsics = setupExtrinsics(mTango);
        mIntrinsics = mTango.getCameraIntrinsics(TangoCameraIntrinsics.TANGO_CAMERA_COLOR);
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
    public synchronized void localized() {
        mIsLocalized = true;
        savedAdf = currentAdf;
    }

    @Override
    public synchronized void notLocalized() {
        mIsLocalized = false;
    }


    public AdfInfo getCurrentAdf() {
        return currentAdf;
    }
}
