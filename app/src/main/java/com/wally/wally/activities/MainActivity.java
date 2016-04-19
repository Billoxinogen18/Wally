/*
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wally.wally.activities;

import com.google.atap.tangoservice.Tango;
import com.google.atap.tangoservice.Tango.OnTangoUpdateListener;
import com.google.atap.tangoservice.TangoCameraIntrinsics;
import com.google.atap.tangoservice.TangoConfig;
import com.google.atap.tangoservice.TangoCoordinateFramePair;
import com.google.atap.tangoservice.TangoEvent;
import com.google.atap.tangoservice.TangoException;
import com.google.atap.tangoservice.TangoOutOfDateException;
import com.google.atap.tangoservice.TangoPoseData;
import com.google.atap.tangoservice.TangoXyzIjData;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.transition.Scene;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.rajawali3d.math.Matrix4;
import org.rajawali3d.math.Quaternion;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.scene.ASceneFrameCallback;
import org.rajawali3d.surface.RajawaliSurfaceView;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import com.projecttango.rajawali.DeviceExtrinsics;
import com.projecttango.rajawali.ScenePoseCalculator;
import com.projecttango.tangosupport.TangoPointCloudManager;
import com.projecttango.tangosupport.TangoSupport;
import com.wally.wally.R;
import com.wally.wally.WallyRenderer;

/**
 * An example showing how to build a very simple point to point measurement app
 * in Java. It uses the TangoSupportLibrary to do depth calculations using
 * the PointCloud data. Whenever the user clicks on the camera display, a point
 * is recorded from the PointCloud data closest to the point of the touch.
 * consecutive touches are used as the two points for a distance measurement.
 * <p/>
 * Note that it is important to include the KEY_BOOLEAN_LOWLATENCYIMUINTEGRATION
 * configuration parameter in order to achieve best results synchronizing the
 * Rajawali virtual world with the RGB camera.
 * <p/>
 * For more details on the augmented reality effects, including color camera texture rendering,
 * see java_augmented_reality_example or java_hello_video_example.
 */
public class MainActivity extends Activity implements View.OnTouchListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    // The interval at which we'll update our UI debug text in milliseconds.
    // This is the rate at which we query for distance data.
    private static final int UPDATE_UI_INTERVAL_MS = 100;

    public static final TangoCoordinateFramePair FRAME_PAIR = new TangoCoordinateFramePair(
            TangoPoseData.COORDINATE_FRAME_AREA_DESCRIPTION,
            TangoPoseData.COORDINATE_FRAME_DEVICE);

//    public static final TangoCoordinateFramePair FRAME_PAIR_2 = new TangoCoordinateFramePair(
//            TangoPoseData.COORDINATE_FRAME_AREA_DESCRIPTION,
//            TangoPoseData.COORDINATE_FRAME_DEVICE);

    private static final int INVALID_TEXTURE_ID = -1;

    private RajawaliSurfaceView mSurfaceView;
    private WallyRenderer mRenderer;
    private TangoCameraIntrinsics mIntrinsics;
    private DeviceExtrinsics mExtrinsics;
    private TangoPointCloudManager mPointCloudManager;
    private Tango mTango;
    private AtomicBoolean mIsConnected = new AtomicBoolean(false);
    private double mCameraPoseTimestamp = 0;

    // Texture rendering related fields
    // NOTE: Naming indicates which thread is in charge of updating this variable
    private int mConnectedTextureIdGlThread = INVALID_TEXTURE_ID;
    private AtomicBoolean mIsFrameAvailableTangoThread = new AtomicBoolean(false);
    private double mRgbTimestampGlThread;

    private TextView mDistanceMeasure;
    private TextView mIsValid;
    private TextView mIsLocalized;
    private TextView mExtraData;

    private boolean mIsPoseValid;
    private boolean mIsPoseLocalized;
    private TangoPoseData mLastPose;

    // Handles the debug text UI update loop.
    private Handler mHandler = new Handler();
    private Matrix4 mTangoPosition;
    private double mPoseTimestamp;
    private Matrix4 mDeviceTMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDistanceMeasure = (TextView) findViewById(R.id.distance_textview);
        mIsValid = (TextView) findViewById(R.id.is_valid_textview);
        mIsLocalized = (TextView) findViewById(R.id.is_localized_textview);
        mExtraData = (TextView) findViewById(R.id.extra_data_textview);

        mSurfaceView = (RajawaliSurfaceView) findViewById(R.id.rajawali_surface);
        mRenderer = new WallyRenderer(this);
        mSurfaceView.setSurfaceRenderer(mRenderer);
        mSurfaceView.setOnTouchListener(this);
        mTango = new Tango(this);
        mPointCloudManager = new TangoPointCloudManager();

        if (!hasADFPermissions()) {
            Log.i(TAG, "onCreate: Didn't had ADF permission, requesting permission");
            startActivityForResult(
                    Tango.getRequestPermissionIntent(Tango.PERMISSIONTYPE_ADF_LOAD_SAVE),
                    Tango.TANGO_INTENT_ACTIVITYCODE);
        }
    }

    private boolean hasADFPermissions() {
        return Tango.hasPermission(getBaseContext(), Tango.PERMISSIONTYPE_ADF_LOAD_SAVE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Synchronize against disconnecting while the service is being used in the OpenGL thread or
        // in the UI thread.
        // NOTE: DO NOT lock against this same object in the Tango callback thread. Tango.disconnect
        // will block here until all Tango callback calls are finished. If you lock against this
        // object in a Tango callback thread it will cause a deadlock.
        synchronized (this) {
            if (mIsConnected.compareAndSet(true, false)) {
                mRenderer.getCurrentScene().clearFrameCallbacks();
                mTango.disconnectCamera(TangoCameraIntrinsics.TANGO_CAMERA_COLOR);
                // We need to invalidate the connected texture ID so that we cause a re-connection
                // in the OpenGL thread after resume
                mConnectedTextureIdGlThread = INVALID_TEXTURE_ID;
                mTango.disconnect();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Synchronize against disconnecting while the service is being used in the OpenGL thread or
        // in the UI thread.
        synchronized (this) {
            if (!hasADFPermissions()) {
                Log.i(TAG, "onResume: Didn't have ADF permission returning.");
                return;
            }
            if (mIsConnected.compareAndSet(false, true)) {
                try {
                    connectTango();
                } catch (TangoOutOfDateException e) {
                    Toast.makeText(getApplicationContext(),
                            R.string.exception_out_of_date,
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
        mHandler.post(mUpdateUiLoopRunnable);
    }

    /**
     * Configures the Tango service and connects it to callbacks.
     */
    private void connectTango() {
        // Use default configuration for Tango Service, plus low latency
        // IMU integration.


        TangoConfig config = mTango.getConfig(
                TangoConfig.CONFIG_TYPE_DEFAULT);
        // NOTE: Low latency integration is necessary to achieve a
        // precise alignment of virtual objects with the RBG image and
        // produce a good AR effect.

        ArrayList<String> fullUUIDList = mTango.listAreaDescriptions();

        // Load the latest ADF if ADFs are found.
        if (fullUUIDList.size() > 0) {
//            Log.i(TAG, "Load ADF: " + fullUUIDList.get(fullUUIDList.size() - 1));
            config.putString(TangoConfig.KEY_STRING_AREADESCRIPTION,
                    fullUUIDList.get(fullUUIDList.size() - 1));
        }
        config.putBoolean(TangoConfig.KEY_BOOLEAN_LOWLATENCYIMUINTEGRATION, true);
        config.putBoolean(TangoConfig.KEY_BOOLEAN_DEPTH, true);
        config.putBoolean(TangoConfig.KEY_BOOLEAN_COLORCAMERA, true);
        config.putBoolean(TangoConfig.KEY_BOOLEAN_AUTORECOVERY, true);
        //config.putBoolean(TangoConfig.KEY_BOOLEAN_LEARNINGMODE, true);

        mTango.connect(config);

        // No need to add any coordinate frame pairs since we are not
        // using pose data. So just initialize.
        ArrayList<TangoCoordinateFramePair> framePairs = new ArrayList<>();
        framePairs.add(new TangoCoordinateFramePair(TangoPoseData.COORDINATE_FRAME_AREA_DESCRIPTION,
                TangoPoseData.COORDINATE_FRAME_START_OF_SERVICE));
        mTango.connectListener(framePairs, new OnTangoUpdateListener() {
            @Override
            public void onPoseAvailable(TangoPoseData pose) {
                if (pose.baseFrame == TangoPoseData.COORDINATE_FRAME_AREA_DESCRIPTION &&
                        pose.targetFrame == TangoPoseData.COORDINATE_FRAME_START_OF_SERVICE &&
                        pose.statusCode == TangoPoseData.POSE_VALID) {
                    mIsPoseLocalized = true;
                    mLastPose = pose;
                    connectRenderer();

//                    // Construct matrix for the start of service with respect to device from the pose.
//                    Matrix4 ssTd = ScenePoseCalculator.tangoPoseToMatrix(pose);
//
//                    // Calculate matrix for the camera in the Unity world, taking into account offsets.
//                    mTangoPosition = ScenePoseCalculator.OPENGL_T_TANGO_WORLD.multiply(ssTd).multiply(ScenePoseCalculator.DEPTH_CAMERA_T_OPENGL_CAMERA);
//
//                    // Extract final position, rotation.
//
//                    // Other pose data -- Pose time.
//                    mPoseTimestamp = mCameraPoseTimestamp;
//
//                    loadEarth();
                }
                mHandler.post(mUpdateUiLoopRunnable);
            }

            @Override
            public void onFrameAvailable(int cameraId) {
                // Check if the frame available is for the camera we want and update its frame
                // on the view.
                if (cameraId == TangoCameraIntrinsics.TANGO_CAMERA_COLOR) {
                    // Mark a camera frame is available for rendering in the OpenGL thread
                    mIsFrameAvailableTangoThread.set(true);
                    mSurfaceView.requestRender();
                }
            }

            @Override
            public void onXyzIjAvailable(TangoXyzIjData xyzIj) {
                // Save the cloud and point data for later use.
                mPointCloudManager.updateXyzIj(xyzIj);
            }

            @Override
            public void onTangoEvent(TangoEvent event) {
                // We are not using OnPoseAvailable for this app.
            }
        });

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
                synchronized (MainActivity.this) {
                    // Don't execute any tango API actions if we're not connected to the service
                    if (!mIsConnected.get()) {
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
                    if (mIsFrameAvailableTangoThread.compareAndSet(true, false)) {
                        mRgbTimestampGlThread =
                                mTango.updateTexture(TangoCameraIntrinsics.TANGO_CAMERA_COLOR);
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

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
            // Calculate click location in u,v (0;1) coordinates.
            float u = motionEvent.getX() / view.getWidth();
            float v = motionEvent.getY() / view.getHeight();

            try {
                // Place point near the clicked point using the latest point cloud data
                // Synchronize against concurrent access to the RGB timestamp in the OpenGL thread
                // and a possible service disconnection due to an onPause event.
                TangoPoseData newPose;
                synchronized (this) {
                    newPose = doFitPlane(u, v, mRgbTimestampGlThread);
                }
                if (newPose != null) {
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                    SharedPreferences.Editor editor = prefs.edit();

                    if (!prefs.contains("x_")) {
                        editor.putFloat("x_", (float) newPose.translation[0]);
                        editor.putFloat("y_", (float) newPose.translation[1]);
                        editor.putFloat("z_", (float) newPose.translation[2]);

                        editor.putFloat("0_", (float) newPose.rotation[0]);
                        editor.putFloat("1_", (float) newPose.rotation[1]);
                        editor.putFloat("2_", (float) newPose.rotation[2]);
                        editor.putFloat("3_", (float) newPose.rotation[3]);
                    } else {
                        newPose.translation[0] = prefs.getFloat("x_", 0);
                        newPose.translation[1] = prefs.getFloat("y_", 0);
                        newPose.translation[2] = prefs.getFloat("z_", 0);

                        newPose.rotation[0] = prefs.getFloat("0_", 0);
                        newPose.rotation[1] = prefs.getFloat("1_", 0);
                        newPose.rotation[2] = prefs.getFloat("2_", 0);
                        newPose.rotation[3] = prefs.getFloat("3_", 0);

                        editor.remove("x_");
                        editor.remove("y_");
                        editor.remove("z_");

                        editor.remove("0_");
                        editor.remove("1_");
                        editor.remove("2_");
                        editor.remove("3_");
                    }
                    editor.apply();
                    // Update a line endpoint to the touch location.
                    // This update is made thread safe by the renderer
                    mRenderer.setLine(newPose);


//                    Matrix4 uwTDevice = mTangoPosition;
//                    Matrix4 uwTMarker = new Matrix4().
//                            translate(rgbPoint).
//                            rotate(Quaternion.getIdentity()).
//                            scale(Vector3.ONE);
//                    mDeviceTMarker = uwTDevice.inverse().multiply(uwTMarker);


                } else {
                    Log.w(TAG, "Point was null.");
                }

            } catch (TangoException t) {
                Toast.makeText(getApplicationContext(),
                        R.string.failed_measurement,
                        Toast.LENGTH_SHORT).show();
                Log.e(TAG, getString(R.string.failed_measurement), t);
            } catch (SecurityException t) {
                Toast.makeText(getApplicationContext(),
                        R.string.failed_permissions,
                        Toast.LENGTH_SHORT).show();
                Log.e(TAG, getString(R.string.failed_permissions), t);
            }
        }
        return true;
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
        TangoPoseData devicePose =
                mTango.getPoseAtTime(xyzIj.timestamp, FRAME_PAIR);

        // Update the AR object location.
        TangoPoseData planeFitPose = ScenePoseCalculator.planeFitToTangoWorldPose(
                intersectionPointPlaneModelPair.intersectionPoint,
                intersectionPointPlaneModelPair.planeModel, devicePose, mExtrinsics);

        return planeFitPose;
    }

    /**
     * Use the TangoSupport library with point cloud data to calculate the depth
     * of the point closest to where the user touches the screen. It returns a
     * Vector3 in openGL world space.
     */
    private Vector3 getDepthAtTouchPosition(float u, float v, double rgbTimestamp) {
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

        float[] point = TangoSupport.getDepthAtPointNearestNeighbor(xyzIj, mIntrinsics,
                colorTdepthPose, u, v);
        if (point == null) {
            return null;
        }

        TangoPoseData myPose = mTango.getPoseAtTime(rgbTimestamp, new TangoCoordinateFramePair(TangoPoseData.COORDINATE_FRAME_START_OF_SERVICE, TangoPoseData.COORDINATE_FRAME_AREA_DESCRIPTION));
        Log.d("SHIT", myPose.toString());

        return ScenePoseCalculator.getPointInEngineFrame(
                new Vector3(point[0], point[1], point[2]),
                ScenePoseCalculator.matrixToTangoPose(mExtrinsics.getDeviceTDepthCamera()),
                mTango.getPoseAtTime(rgbTimestamp, FRAME_PAIR));
    }


    private void loadEarth() {

    }


    // Debug text UI update loop, updating at 10Hz.
    private Runnable mUpdateUiLoopRunnable = new Runnable() {
        public void run() {
            updateUi();
            mHandler.postDelayed(this, UPDATE_UI_INTERVAL_MS);
        }
    };

    @SuppressLint("SetTextI18n")
    private synchronized void updateUi() {
        try {
            mDistanceMeasure.setText("Not yet ready");
            mIsValid.setText(mIsPoseValid ? "valid pose" : "invalid pose");
            mIsLocalized.setText(mIsPoseLocalized ? "localized" : "Not localized!");
            mExtraData.setText(mLastPose != null ? mLastPose.toString() : "NULL");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
