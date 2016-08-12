package com.wally.wally.tango.states;

import android.util.Log;

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
import com.wally.wally.renderer.WallyRenderer;
import com.wally.wally.tango.EventListener;
import com.wally.wally.tango.TangoFactory;
import com.wally.wally.tango.TangoUpdater;
import com.wally.wally.tango.TangoUtils;

import org.rajawali3d.math.Quaternion;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.scene.ASceneFrameCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
public class TangoState implements TangoUpdater.TangoUpdaterListener{
    private static final String TAG = TangoState.class.getSimpleName();
    private static final int INVALID_TEXTURE_ID = -1;
    private static final TangoCoordinateFramePair FRAME_PAIR =
            new TangoCoordinateFramePair(
                    TangoPoseData.COORDINATE_FRAME_AREA_DESCRIPTION,
                    TangoPoseData.COORDINATE_FRAME_DEVICE
            );

    protected Tango mTango;
    protected Executor mExecutor;
    protected boolean mIsLocalized;
    protected TangoUpdater mTangoUpdater;
    protected TangoFactory mTangoFactory;
    protected List<EventListener> mEventListeners;
    protected Map<Class, TangoState> mTangoStatePool;

    private TangoPointCloudManager mPointCloudManager;
    private WallyRenderer mRenderer;
    private StateChangeListener mStateChangeListener;

    private boolean mIsConnected;
    private TangoCameraIntrinsics mIntrinsics;
    private DeviceExtrinsics mExtrinsics;
    private double mCameraPoseTimestamp = 0;



    // NOTE: suffix indicates which thread is in charge of updating
    private double mRgbTimestampGlThread;
    private boolean mIsFrameAvailableTangoThread;
    private int mConnectedTextureIdGlThread = INVALID_TEXTURE_ID;


    public TangoState(Executor executor,
                      TangoUpdater tangoUpdater,
                      TangoFactory tangoFactory,
                      WallyRenderer wallyRenderer,
                      Map<Class, TangoState> tangoStatePool,
                      TangoPointCloudManager pointCloudManager){
        mRenderer = wallyRenderer;
        mTangoUpdater = tangoUpdater;
        mPointCloudManager = pointCloudManager;
        mTangoFactory = tangoFactory;
        mTangoStatePool = tangoStatePool;
        mExecutor = executor;
        mEventListeners = new ArrayList<>();
    }


    public synchronized void pause(){
        Log.d(TAG, "pause. Thread = " + Thread.currentThread());
        disconnect();
    }

    public synchronized void resume(){
        Log.d(TAG, "resume Thread = " + Thread.currentThread());

    }

    public void disconnect(){
        Log.d(TAG, "Disconnect Tango");
        // Synchronize against disconnecting while the service is being used
        // in OpenGL thread or in UI thread.

        // NOTE: DO NOT lock against this same object in the Tango callback thread.
        // Tango.disconnect will block here until all Tango callback calls are finished.
        // If you lock against this object in a Tango callback thread it will cause a deadlock.
        if (mIsConnected) {
            Log.d(TAG, "disconnect - mIsConnected = true");
            mRenderer.getCurrentScene().clearFrameCallbacks();
            mTango.disconnectCamera(TangoCameraIntrinsics.TANGO_CAMERA_COLOR);
            Log.d(TAG, "disconnect - remove renderer" );

            // We need to invalidate the connected texture ID,
            // this will cause re-connection in the OpenGL thread after resume
            mConnectedTextureIdGlThread = INVALID_TEXTURE_ID;

            mIsConnected = false;
            Log.d(TAG, "disconnect - mIsConnected = false");

        }
        if (mTango != null) {
            mTango.disconnect();
        }
        Log.d(TAG, "disconnect - disconnected");

        mTangoUpdater.setTangoLocalization(false);
        Log.d(TAG, "disconnect - localization false");

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

        Vector3 addTo = p.getOrientation().multiply(new Vector3(0, 0, -1));
        Quaternion rot = new Quaternion().fromAngleAxis(addTo, 180);

        return new Pose(p.getPosition().add(addTo), p.getOrientation().multiply(rot));

    }

    @Override
    public synchronized void onFrameAvailable() {
        mIsFrameAvailableTangoThread = true;
    }

    @Override
    public void onLocalization(boolean localization) {
        Log.d(TAG, "onLocalization - " + localization);
        mIsLocalized = localization;
    }

    public void addEventListener(EventListener listener){
        mEventListeners.add(listener);
    }

    public boolean removeEventListener(EventListener listener){
        return mEventListeners.remove(listener);
    }

    public void setStateChangeListener(StateChangeListener listener){
        mStateChangeListener = listener;
    }

    public boolean isLearningState() {
        return false;
    }

    public boolean isConnected() {
        return mIsConnected;
    }

//    protected <T extends TangoBase> T getTangoState(Class<T> cl){
//        return (T)mTangoStatePool.get(cl);
//    }

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
                fireTangoOutOfDate();
            }
        };
    }


    protected void changeState(TangoState nextTango){
        mStateChangeListener.onStateChange(nextTango);
        resetTangoUpdaterListener(nextTango);
    }

    private void resetTangoUpdaterListener(TangoUpdater.TangoUpdaterListener toAdd){
        mTangoUpdater.removeTangoUpdaterListener(this);
        mTangoUpdater.addTangoUpdaterListener(toAdd);
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
        TangoPoseData depthPose = TangoSupport.calculateRelativePose(
                rgbTimestamp, TangoPoseData.COORDINATE_FRAME_CAMERA_COLOR,
                xyzIj.timestamp, TangoPoseData.COORDINATE_FRAME_CAMERA_DEPTH);


        // Perform plane fitting with the latest available point cloud data.
        TangoSupport.IntersectionPointPlaneModelPair intersectionPointPlaneModelPair =
                TangoSupport.fitPlaneModelNearClick(xyzIj, mIntrinsics,
                        depthPose, u, v);

        // Get the device pose at the time the plane data was acquired.
        TangoPoseData devicePose = mTango.getPoseAtTime(xyzIj.timestamp, FRAME_PAIR);

        // Update the AR object location.

        return ScenePoseCalculator.planeFitToTangoWorldPose(
                intersectionPointPlaneModelPair.intersectionPoint,
                intersectionPointPlaneModelPair.planeModel, devicePose, mExtrinsics);
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
                synchronized (TangoState.this) {
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

    private void fireTangoOutOfDate(){
        for (EventListener listener: mEventListeners){
            listener.onTangoOutOfDate();
        }
    }

    public boolean isLocalized() {
        return mIsLocalized;
    }

    public AdfInfo getAdf() {
        throw new UnsupportedOperationException("State does not provide Adf");
    }

    public interface StateChangeListener{
        void onStateChange(TangoState nextTangoState);
    }

    public interface Executor {
        void execute(Runnable runnable);
    }
}
