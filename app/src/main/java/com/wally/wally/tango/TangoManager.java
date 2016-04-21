package com.wally.wally.tango;

import android.content.Context;
import android.util.Log;

import com.google.atap.tango.ux.TangoUx;
import com.google.atap.tango.ux.TangoUxLayout;
import com.google.atap.tangoservice.Tango;
import com.google.atap.tangoservice.TangoCameraIntrinsics;
import com.google.atap.tangoservice.TangoConfig;
import com.google.atap.tangoservice.TangoCoordinateFramePair;
import com.google.atap.tangoservice.TangoEvent;
import com.google.atap.tangoservice.TangoOutOfDateException;
import com.google.atap.tangoservice.TangoPoseData;
import com.google.atap.tangoservice.TangoXyzIjData;
import com.projecttango.rajawali.DeviceExtrinsics;
import com.projecttango.tangosupport.TangoPointCloudManager;
import com.wally.wally.WallyRenderer;

import org.rajawali3d.scene.ASceneFrameCallback;
import org.rajawali3d.surface.RajawaliSurfaceView;

import java.util.ArrayList;

/**
 * Created by shota on 4/21/16.
 */
public class TangoManager implements Tango.OnTangoUpdateListener {
    private static final String TAG = TangoManager.class.getSimpleName();
    private static final int INVALID_TEXTURE_ID = -1;

    public static final TangoCoordinateFramePair FRAME_PAIR = new TangoCoordinateFramePair(
            TangoPoseData.COORDINATE_FRAME_AREA_DESCRIPTION,
            TangoPoseData.COORDINATE_FRAME_DEVICE);


    private RajawaliSurfaceView mSurfaceView;
    private WallyRenderer mRenderer;
    private TangoCameraIntrinsics mIntrinsics;
    private DeviceExtrinsics mExtrinsics;
    private TangoPointCloudManager mPointCloudManager;
    private Tango mTango;
    private TangoUx mTangoUx;
    private String adfUuid;
    private double mCameraPoseTimestamp = 0;
    private boolean mIsConnected;

    // Texture rendering related fields
    // NOTE: Naming indicates which thread is in charge of updating this variable
    private int mConnectedTextureIdGlThread = INVALID_TEXTURE_ID;
    private boolean mIsFrameAvailableTangoThread;
    private double mRgbTimestampGlThread;


    public TangoManager(Context context, RajawaliSurfaceView rajawaliSurfaceView, TangoUxLayout tangoUxLayout, String adfUuid){
        mSurfaceView = rajawaliSurfaceView;
        mRenderer = new WallyRenderer(context.getApplicationContext());
        mSurfaceView.setSurfaceRenderer(mRenderer);
        mTango = new Tango(context);
        mTangoUx = new TangoUx(context);
        this.adfUuid = adfUuid;

        TangoUxLayout mTangoUxLayout = tangoUxLayout;
        mTangoUx.setLayout(mTangoUxLayout);

        mPointCloudManager = new TangoPointCloudManager();
    }


    public synchronized void onPause(){
        // Synchronize against disconnecting while the service is being used in the OpenGL thread or
        // in the UI thread.
        // NOTE: DO NOT lock against this same object in the Tango callback thread. Tango.disconnect
        // will block here until all Tango callback calls are finished. If you lock against this
        // object in a Tango callback thread it will cause a deadlock.
        if (mIsConnected) {
            mRenderer.getCurrentScene().clearFrameCallbacks();
            mTango.disconnectCamera(TangoCameraIntrinsics.TANGO_CAMERA_COLOR);
            // We need to invalidate the connected texture ID so that we cause a re-connection
            // in the OpenGL thread after resume
            mConnectedTextureIdGlThread = INVALID_TEXTURE_ID;
            mTango.disconnect();
            mTangoUx.stop();
            mIsConnected = false;
        }

    }

    public synchronized void onResume(){
        // Synchronize against disconnecting while the service is being used in the OpenGL thread or
        // in the UI thread.
        if (!mIsConnected) {
            try {
                TangoUx.StartParams params = new TangoUx.StartParams();
                mTangoUx.start(params);
                mTangoUx.setUxExceptionEventListener(UxExceptionEvents.getInstance());
                connectTango();
                mIsConnected = true;
            } catch (TangoOutOfDateException e) {
                if (mTangoUx != null) {
                    mTangoUx.showTangoOutOfDate();
                }
            }
        }

    }

    /**
     * Configures the Tango service and connects it to callbacks.
     */
    private void connectTango() {
        TangoConfig config = mTango.getConfig(
                TangoConfig.CONFIG_TYPE_DEFAULT);

        ArrayList<String> fullUUIDList = mTango.listAreaDescriptions();
        String tempAdfUuid = adfUuid;
        // Load the latest ADF if ADFs are found.
        if (fullUUIDList.size() > 0) {
            if (adfUuid == null || adfUuid.equals("")) {
                tempAdfUuid = fullUUIDList.get(fullUUIDList.size() - 1);
            }
            config.putString(TangoConfig.KEY_STRING_AREADESCRIPTION, tempAdfUuid);
        }

        config.putBoolean(TangoConfig.KEY_BOOLEAN_LOWLATENCYIMUINTEGRATION, true);
        config.putBoolean(TangoConfig.KEY_BOOLEAN_DEPTH, true);
        config.putBoolean(TangoConfig.KEY_BOOLEAN_COLORCAMERA, true);
        config.putBoolean(TangoConfig.KEY_BOOLEAN_AUTORECOVERY, true);

        mTango.connect(config);

        ArrayList<TangoCoordinateFramePair> framePairs = new ArrayList<>();
        framePairs.add(new TangoCoordinateFramePair(
                TangoPoseData.COORDINATE_FRAME_AREA_DESCRIPTION,
                TangoPoseData.COORDINATE_FRAME_START_OF_SERVICE));
        mTango.connectListener(framePairs, this);

        // Get extrinsics from device for use in transforms. This needs
        // to be done after connecting Tango and listeners.
        mExtrinsics = setupExtrinsics(mTango);
        mIntrinsics = mTango.getCameraIntrinsics(TangoCameraIntrinsics.TANGO_CAMERA_COLOR);
    }


    @Override
    public void onPoseAvailable(TangoPoseData pose) {
        if (mTangoUx != null) {
            mTangoUx.updatePoseStatus(pose.statusCode);
        }
        if (pose.baseFrame == TangoPoseData.COORDINATE_FRAME_AREA_DESCRIPTION &&
                pose.targetFrame == TangoPoseData.COORDINATE_FRAME_START_OF_SERVICE &&
                pose.statusCode == TangoPoseData.POSE_VALID) {
            connectRenderer();
        }
    }

    @Override
    public void onFrameAvailable(int cameraId) {
        // Check if the frame available is for the camera we want and update its frame
        // on the view.
        if (cameraId == TangoCameraIntrinsics.TANGO_CAMERA_COLOR) {
            // Mark a camera frame is available for rendering in the OpenGL thread
            mIsFrameAvailableTangoThread = true;
            mSurfaceView.requestRender();
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
        if (mTangoUx != null) {
            mTangoUx.updateTangoEvent(event);
        }
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
                synchronized (this) {
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
                    if (mIsFrameAvailableTangoThread) {
                        mRgbTimestampGlThread =
                                mTango.updateTexture(TangoCameraIntrinsics.TANGO_CAMERA_COLOR);
                        mIsFrameAvailableTangoThread = false;
                    }

                    // If a new RGB frame has been rendered, update the camera pose to match.
                    if (mRgbTimestampGlThread > mCameraPoseTimestamp) {
                        // Calculate the device pose at the camera frame update time.
                        TangoPoseData lastFramePose = mTango.getPoseAtTime(mRgbTimestampGlThread, FRAME_PAIR);
                        if (lastFramePose.statusCode == TangoPoseData.POSE_VALID) {
                            // Update the camera pose from the renderer
                            mRenderer.updateRenderCameraPose(lastFramePose, mExtrinsics);
                            mCameraPoseTimestamp = lastFramePose.timestamp;
                        } else {
                            Log.w(TAG, "Can't get device pose at time: " + mRgbTimestampGlThread);
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

}
