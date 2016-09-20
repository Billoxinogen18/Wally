package com.wally.wally.tango.states;

import android.opengl.Matrix;
import android.util.Log;
import android.view.Surface;

import com.google.atap.tangoservice.Tango;
import com.google.atap.tangoservice.TangoCameraIntrinsics;
import com.google.atap.tangoservice.TangoCoordinateFramePair;
import com.google.atap.tangoservice.TangoErrorException;
import com.google.atap.tangoservice.TangoException;
import com.google.atap.tangoservice.TangoOutOfDateException;
//import com.google.atap.tangoservice.TangoPointCloudData;
import com.google.atap.tangoservice.TangoPointCloudData;
import com.google.atap.tangoservice.TangoPoseData;
import com.google.atap.tangoservice.TangoXyzIjData;
import com.projecttango.rajawali.DeviceExtrinsics;
import com.projecttango.rajawali.Pose;
import com.projecttango.rajawali.ScenePoseCalculator;
import com.projecttango.tangosupport.TangoPointCloudManager;
import com.projecttango.tangosupport.TangoSupport;
import com.wally.wally.adf.AdfInfo;
import com.wally.wally.controllers.main.CameraARTangoActivity;
import com.wally.wally.events.WallyEvent;
import com.wally.wally.events.WallyEventListener;
import com.wally.wally.renderer.WallyRenderer;
import com.wally.wally.tango.TangoFactory;
import com.wally.wally.tango.TangoUpdater;
import com.wally.wally.tango.TangoUtils;

import org.rajawali3d.math.Matrix4;
import org.rajawali3d.math.Quaternion;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.scene.ASceneFrameCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by shota on 8/9/16.
 * Tango states are:
 * 1. Not Connected
 * 2. Connected: Learning
 * 3. Connected: Try to identify area
 * 4. Connected: area identified
 * 5. Connected: Learning : localization lost
 * 6. Connected: Try to identify area : localization lost
 * 7. Connected: area identified : localization lost
 */
public abstract class TangoState implements TangoUpdater.TangoUpdaterListener {
    private final String TAG = this.getClass().getSimpleName();
    private static final int INVALID_TEXTURE_ID = -1;
    private static final TangoCoordinateFramePair FRAME_PAIR =
            new TangoCoordinateFramePair(
                    TangoPoseData.COORDINATE_FRAME_AREA_DESCRIPTION,
                    TangoPoseData.COORDINATE_FRAME_DEVICE
            );

    protected Tango mTango;
    protected boolean mIsLocalized;
    protected boolean mIsConnected;
    protected TangoUpdater mTangoUpdater;
    protected TangoFactory mTangoFactory;
    protected List<WallyEventListener> mEventListeners;

    protected TangoPointCloudManager mPointCloudManager;
    protected WallyRenderer mRenderer;

    protected TangoCameraIntrinsics mIntrinsics;
    protected DeviceExtrinsics mExtrinsics;
    protected double mCameraPoseTimestamp = 0;


    // NOTE: suffix indicates which thread is in charge of updating
    protected double mRgbTimestampGlThread;
    //private boolean mIsFrameAvailableTangoThread;
    protected AtomicBoolean mIsFrameAvailableTangoThread = new AtomicBoolean(false);
    protected int mConnectedTextureIdGlThread = INVALID_TEXTURE_ID;
    protected TangoStateConnector mFailStateConnector;
    protected TangoStateConnector mSuccessStateConnector;


    public TangoState(TangoUpdater tangoUpdater,
                      TangoFactory tangoFactory,
                      WallyRenderer wallyRenderer,
                      TangoPointCloudManager pointCloudManager) {
        mRenderer = wallyRenderer;
        mTangoUpdater = tangoUpdater;
        mPointCloudManager = pointCloudManager;
        mTangoFactory = tangoFactory;
        mEventListeners = new ArrayList<>();
    }

    public TangoState withFailStateConnector(TangoStateConnector connector) {
        mFailStateConnector = connector;
        return this;
    }

    public TangoState withSuccessStateConnector(TangoStateConnector connector) {
        mSuccessStateConnector = connector;
        return this;
    }


    public synchronized final void pause() {
        Log.d(TAG, "pause. Thread = " + Thread.currentThread());
        disconnect();
        fireEvent(WallyEvent.createEventWithId(WallyEvent.ON_PAUSE));
        pauseHook();
    }

    protected abstract void pauseHook();

    public synchronized final void resume() {
        Log.d(TAG, "resume Thread = " + Thread.currentThread());
        fireEvent(WallyEvent.createEventWithId(WallyEvent.ON_RESUME));
        resumeHook();
    }

    protected abstract void resumeHook();

    private void disconnect() {
        Log.d(TAG, "Disconnect Tango");
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

            if (mTango != null) {
                mTango.disconnect();
                mTango = null;
            }
        }
        mTangoUpdater.setTangoLocalization(false);
    }

    /**
     * / * Finds plane pose in the middle of the screen.
     */
    public float[] findPlaneInMiddle() {
        return doFitPlane(0.5f, 0.5f, mRgbTimestampGlThread);
    }

//    public Pose getDevicePoseInFront() {
//        TangoPoseData devicePose =
//                mTango.getPoseAtTime(mRgbTimestampGlThread, FRAME_PAIR);
//        Pose p = ScenePoseCalculator.toOpenGlCameraPose(devicePose, mExtrinsics);
//
//        Vector3 addTo = p.getOrientation().multiply(new Vector3(0, 0, -1));
//        Quaternion rot = new Quaternion().fromAngleAxis(addTo, 180);
//
//        return new Pose(p.getPosition().add(addTo), p.getOrientation().multiply(rot));
//
//    }

    public Pose getDevicePoseInFront() {
        TangoPoseData devicePose =
                TangoSupport.getPoseAtTime(mRgbTimestampGlThread, FRAME_PAIR.baseFrame,
                        FRAME_PAIR.targetFrame, TangoSupport.TANGO_SUPPORT_ENGINE_OPENGL, 0);

        Pose p = ScenePoseCalculator.toOpenGlCameraPose(devicePose, mExtrinsics);

        Vector3 addTo = p.getOrientation().multiply(new Vector3(0, 0, -1));
        Quaternion rot = new Quaternion().fromAngleAxis(addTo, 180);

        return new Pose(p.getPosition().add(addTo), p.getOrientation().multiply(rot));

    }

    @Override
    public void onFrameAvailable() {
        mIsFrameAvailableTangoThread.set(true);
    }

    @Override
    public void onLocalization(boolean localization) {
        Log.d(TAG, "onLocalization - " + localization);
        mIsLocalized = localization;
    }

    @Override
    public int priority() {
        return 10;
    }

    public void addEventListener(WallyEventListener listener) {
        mEventListeners.add(listener);
    }

    public boolean isLearningState() {
        return false;
    }

    public boolean isConnected() {
        return mIsConnected;
    }

    protected TangoFactory.RunnableWithError getTangoInitializer() {
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
                Log.e(TAG, "onError: " + e);
                if (e instanceof TangoOutOfDateException) {
                    fireTangoOutOfDate();
                }
            }
        };
    }

    /**
     * Use the TangoSupport library with point cloud data to calculate the plane
     * of the world feature pointed at the location the camera is looking.
     * It returns the pose of the fitted plane in a TangoPoseData structure.
     */
    private float[] doFitPlane(float u, float v, double rgbTimestamp) {
        //TangoXyzIjData xyzIj = mPointCloudManager.getLatestXyzIj();
        TangoPointCloudData tangoPointCloudData = mPointCloudManager.getLatestPointCloud();

        if (tangoPointCloudData == null) {
            return null;
        }

        // We need to calculate the transform between the color camera at the
        // time the user clicked and the depth camera at the time the depth
        // cloud was acquired.
        TangoPoseData depthPose = TangoSupport.calculateRelativePose(
                rgbTimestamp, TangoPoseData.COORDINATE_FRAME_CAMERA_COLOR,
                tangoPointCloudData.timestamp, TangoPoseData.COORDINATE_FRAME_CAMERA_DEPTH);

        try{
            // Perform plane fitting with the latest available point cloud data.
            TangoSupport.IntersectionPointPlaneModelPair intersectionPointPlaneModelPair =
                    TangoSupport.fitPlaneModelNearPoint(tangoPointCloudData,
                            depthPose, u, v);

            // Get the device pose at the time the plane data was acquired.
            //TangoPoseData devicePose = mTango.getPoseAtTime(tangoPointCloudData.timestamp, FRAME_PAIR);
            TangoSupport.TangoMatrixTransformData transform =
                    TangoSupport.getMatrixTransformAtTime(tangoPointCloudData.timestamp,
                            TangoPoseData.COORDINATE_FRAME_AREA_DESCRIPTION,
                            TangoPoseData.COORDINATE_FRAME_CAMERA_DEPTH,
                            TangoSupport.TANGO_SUPPORT_ENGINE_OPENGL,
                            TangoSupport.TANGO_SUPPORT_ENGINE_TANGO);

            if (transform.statusCode == TangoPoseData.POSE_VALID) {
                float[] openGlTPlane = calculatePlaneTransform(
                        intersectionPointPlaneModelPair.intersectionPoint,
                        intersectionPointPlaneModelPair.planeModel, transform.matrix);
                //Matrix4 m = new Matrix4(openGlTPlane);
                //return ScenePoseCalculator.matrixToTangoPose(m);

                return openGlTPlane;
            } else {
                Log.w(TAG, "Can't get depth camera transform at time " + tangoPointCloudData.timestamp);
                return null;
            }
        } catch (TangoException e) {
            Log.d(TAG, "Failed to fit plane");
        }
        return null;

        // Update the AR object location.

//        return ScenePoseCalculator.planeFitToTangoWorldPose(
//                intersectionPointPlaneModelPair.intersectionPoint,
//                intersectionPointPlaneModelPair.planeModel, devicePose, mExtrinsics);
    }


    /**
     * Calculate the pose of the plane based on the position and normal orientation of the plane
     * and align it with gravity.
     */
    private float[] calculatePlaneTransform(double[] point, double normal[],
                                            float[] openGlTdepth) {
        // Vector aligned to gravity.
        float[] openGlUp = new float[]{0, 1, 0, 0};
        float[] depthTOpenGl = new float[16];
        Matrix.invertM(depthTOpenGl, 0, openGlTdepth, 0);
        float[] depthUp = new float[4];
        Matrix.multiplyMV(depthUp, 0, depthTOpenGl, 0, openGlUp, 0);
        // Create the plane matrix transform in depth frame from a point, the plane normal and the
        // up vector.
        float[] depthTplane = matrixFromPointNormalUp(point, normal, depthUp);
        float[] openGlTplane = new float[16];
        Matrix.multiplyMM(openGlTplane, 0, openGlTdepth, 0, depthTplane, 0);
        return openGlTplane;
    }

    /**
     * Calculates a transformation matrix based on a point, a normal and the up gravity vector.
     * The coordinate frame of the target transformation will a right handed system with Z+ in
     * the direction of the normal and Y+ up.
     */
    private float[] matrixFromPointNormalUp(double[] point, double[] normal, float[] up) {
        float[] zAxis = new float[]{(float) normal[0], (float) normal[1], (float) normal[2]};
        normalize(zAxis);
        float[] xAxis = crossProduct(up, zAxis);
        normalize(xAxis);
        float[] yAxis = crossProduct(zAxis, xAxis);
        normalize(yAxis);
        float[] m = new float[16];
        Matrix.setIdentityM(m, 0);
        m[0] = xAxis[0];
        m[1] = xAxis[1];
        m[2] = xAxis[2];
        m[4] = yAxis[0];
        m[5] = yAxis[1];
        m[6] = yAxis[2];
        m[8] = zAxis[0];
        m[9] = zAxis[1];
        m[10] = zAxis[2];
        m[12] = (float) point[0];
        m[13] = (float) point[1];
        m[14] = (float) point[2];
        return m;
    }

    /**
     * Normalize a vector.
     */
    private void normalize(float[] v) {
        double norm = Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]);
        v[0] /= norm;
        v[1] /= norm;
        v[2] /= norm;
    }

    /**
     * Cross product between two vectors following the right hand rule.
     */
    private float[] crossProduct(float[] v1, float[] v2) {
        float[] result = new float[3];
        result[0] = v1[1] * v2[2] - v2[1] * v1[2];
        result[1] = v1[2] * v2[0] - v2[2] * v1[0];
        result[2] = v1[0] * v2[1] - v2[0] * v1[1];
        return result;
    }


    /**
     * Connects the view and renderer to the color camera and callbacks.
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
                try {
                    synchronized (TangoState.this) {
                        // Don't execute any tango API actions if we're not connected to the service
                        if (!mIsConnected || mIntrinsics == null) {
                            return;
                        }

                        // Set-up scene camera projection to match RGB camera intrinsics
//                        if (!mRenderer.isSceneCameraConfigured()) {
//                            mRenderer.setProjectionMatrix(TangoUtils.projectionMatrixFromCameraIntrinsics(mIntrinsics));
//                        }

                        if (!mRenderer.isSceneCameraConfigured()) {
                            mRenderer.setProjectionMatrix(
                                    projectionMatrixFromCameraIntrinsics(mIntrinsics));
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
                            //TangoPoseData lastFramePose = mTango.getPoseAtTime(mRgbTimestampGlThread, FRAME_PAIR);
                            TangoPoseData lastFramePose = TangoSupport.getPoseAtTime(
                                    mRgbTimestampGlThread,
                                    TangoPoseData.COORDINATE_FRAME_AREA_DESCRIPTION,
                                    TangoPoseData.COORDINATE_FRAME_CAMERA_COLOR,
                                    TangoSupport.TANGO_SUPPORT_ENGINE_OPENGL, Surface.ROTATION_0);

                            if (lastFramePose.statusCode == TangoPoseData.POSE_VALID) {
                                // Update the camera pose from the renderer
                                mRenderer.updateRenderCameraPose(lastFramePose);
                                mCameraPoseTimestamp = lastFramePose.timestamp;
                            } else {
                                // When the pose status is not valid, it indicates the tracking has
                                // been lost. In this case, we simply stop rendering.
                                //
                                // This is also the place to display UI to suggest the user walk
                                // to recover tracking.
//                                Log.w(TAG, "Can't get device pose at time: " +
//                                        mRgbTimestampGlThread);
                            }

                        }
                    }
                    // Avoid crashing the application due to unhandled exceptions
                } catch (TangoErrorException e) {
                    Log.e(TAG, "Tango API call error within the OpenGL render thread", e);
                } catch (Throwable t) {
                    Log.e(TAG, "Exception on the OpenGL thread", t);
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
     * Use Tango camera intrinsics to calculate the projection Matrix for the Rajawali scene.
     */
    private static float[] projectionMatrixFromCameraIntrinsics(TangoCameraIntrinsics intrinsics) {
        // Uses frustumM to create a projection matrix taking into account calibrated camera
        // intrinsic parameter.
        // Reference: http://ksimek.github.io/2013/06/03/calibrated_cameras_in_opengl/
        float near = 0.1f;
        float far = 100;

        float xScale = near / (float) intrinsics.fx;
        float yScale = near / (float) intrinsics.fy;
        float xOffset = (float) (intrinsics.cx - (intrinsics.width / 2.0)) * xScale;
        // Color camera's coordinates has y pointing downwards so we negate this term.
        float yOffset = (float) -(intrinsics.cy - (intrinsics.height / 2.0)) * yScale;

        float m[] = new float[16];
        Matrix.frustumM(m, 0,
                xScale * (float) -intrinsics.width / 2.0f - xOffset,
                xScale * (float) intrinsics.width / 2.0f - xOffset,
                yScale * (float) -intrinsics.height / 2.0f - yOffset,
                yScale * (float) intrinsics.height / 2.0f - yOffset,
                near, far);
        return m;
    }


    private void fireTangoOutOfDate() {
        fireEvent(WallyEvent.createEventWithId(WallyEvent.TANGO_OUT_OF_DATE));
    }

    protected void fireEvent(WallyEvent event) {
        for (WallyEventListener listener : mEventListeners) {
            listener.onWallyEvent(event);
        }
    }

    public boolean isLocalized() {
        return mIsLocalized;
    }

    public AdfInfo getAdf() {
        throw new UnsupportedOperationException("State does not provide Adf");
    }

    public Tango getTango(){
        return mTango;
    }

    public interface StateChangeListener {
        void onStateChange(TangoState nextTangoState);
        boolean canChangeState();
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }
}
