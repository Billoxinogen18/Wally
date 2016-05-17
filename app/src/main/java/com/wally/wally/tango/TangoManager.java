package com.wally.wally.tango;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

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
import com.projecttango.rajawali.Pose;
import com.projecttango.rajawali.ScenePoseCalculator;
import com.projecttango.tangosupport.TangoPointCloudManager;
import com.projecttango.tangosupport.TangoSupport;
import com.wally.wally.App;
import com.wally.wally.OnContentSelectedListener;
import com.wally.wally.datacontroller.Callback;
import com.wally.wally.datacontroller.content.Content;
import com.wally.wally.datacontroller.content.TangoData;

import org.rajawali3d.scene.ASceneFrameCallback;
import org.rajawali3d.surface.RajawaliSurfaceView;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by shota on 4/21/16.
 */
public class TangoManager implements Tango.OnTangoUpdateListener, ScaleGestureDetector.OnScaleGestureListener, OnVisualContentSelectedListener {
    public static final TangoCoordinateFramePair FRAME_PAIR = new TangoCoordinateFramePair(
            TangoPoseData.COORDINATE_FRAME_AREA_DESCRIPTION,
            TangoPoseData.COORDINATE_FRAME_DEVICE);
    private static final String TAG = TangoManager.class.getSimpleName();
    private static final int INVALID_TEXTURE_ID = -1;

    private Context mContext;

    private TangoCameraIntrinsics mIntrinsics;
    private DeviceExtrinsics mExtrinsics;
    private TangoPointCloudManager mPointCloudManager;
    private Tango mTango;
    private TangoUx mTangoUx;

    private RajawaliSurfaceView mSurfaceView;
    private WallyRenderer mRenderer;
    private VisualContentManager mVisualContentManager;

    private ContentFitter mContentFitter;
    private ScaleGestureDetector mScaleDetector;

    private String mAdfUuid;
    private double mCameraPoseTimestamp = 0;

    private boolean mIsConnected;

    // Texture rendering related fields
    // NOTE: Naming indicates which thread is in charge of updating this variable
    private int mConnectedTextureIdGlThread = INVALID_TEXTURE_ID;
    private boolean mIsFrameAvailableTangoThread;
    private double mRgbTimestampGlThread;

    private OnContentSelectedListener onContentSelectedListener;
    private ContentFitter.OnContentFitListener onContentFitListener;

    public TangoManager(Context context, RajawaliSurfaceView rajawaliSurfaceView, TangoUxLayout tangoUxLayout, String adfUuid) {
        mContext = context;
        mSurfaceView = rajawaliSurfaceView;
        mAdfUuid = adfUuid;

        mVisualContentManager = new VisualContentManager();

        mRenderer = new WallyRenderer(context, mVisualContentManager);
        mRenderer.setOnContentSelectListener(this);

        mSurfaceView.setSurfaceRenderer(mRenderer);
        mTangoUx = new TangoUx(context);

        mPointCloudManager = new TangoPointCloudManager();
        mScaleDetector = new ScaleGestureDetector(context, this);

        mTangoUx.setLayout(tangoUxLayout);

        fetchContentForAdf(adfUuid);
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

    private void fetchContentForAdf(String adfUuid) {
        ((App) mContext.getApplicationContext()).getDataController().fetchByUUID(adfUuid, new Callback<Collection<Content>>() {
            @Override
            public void call(final Collection<Content> result, Exception e) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        for (Content c : result) {
                            Log.d(TAG, c.toString());
                            mVisualContentManager.addStaticContentToBeRenderedOnScreen(new VisualContent(c));
                        }
                    }
                }).start();
            }
        });
    }

    public VisualContentManager getVisualContentManager() {
        return mVisualContentManager;
    }


    public synchronized void onPause() {
        // Synchronize against disconnecting while the service is being used in the OpenGL thread or
        // in the UI thread.
        // NOTE: DO NOT lock against this same object in the Tango callback thread. Tango.disconnect
        // will block here until all Tango callback calls are finished. If you lock against this
        // object in a Tango callback thread it will cause a deadlock.
        if (mIsConnected) {
            mIsConnected = false;
            mRenderer.getCurrentScene().clearFrameCallbacks();
            mTango.disconnectCamera(TangoCameraIntrinsics.TANGO_CAMERA_COLOR);
            // We need to invalidate the connected texture ID so that we cause a re-connection
            // in the OpenGL thread after resume
            mConnectedTextureIdGlThread = INVALID_TEXTURE_ID;
            mTango.disconnect();
            mTangoUx.stop();
        }

        if (mContentFitter != null) {
            mContentFitter.cancel(true);
        }

    }

    public void onResume() {
        // Synchronize against disconnecting while the service is being used in the OpenGL thread or
        // in the UI thread.
        if (!mIsConnected) {
            TangoUx.StartParams params = new TangoUx.StartParams();
            mTangoUx.start(params);
            mTango = new Tango(mContext, new Runnable() {
                @Override
                public void run() {
                    try {
                        connectTango();
                        mIsConnected = true;
                    } catch (TangoOutOfDateException e) {
                        if (mTangoUx != null) {
                            mTangoUx.showTangoOutOfDate();
                        }
                    }
                }
            });
        }

        if (mContentFitter != null) {
            if (mContentFitter.isCancelled()) {
                mContentFitter = new ContentFitter(mContentFitter.getContent(), this);
            }
            mContentFitter.setFittingStatusListener(onContentFitListener);
            mContentFitter.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            onContentFitListener.onFitStatusChange(true);
        }

    }

    /**
     * Configures the Tango service and connects it to callbacks.
     */
    private void connectTango() {
        TangoConfig config = mTango.getConfig(
                TangoConfig.CONFIG_TYPE_DEFAULT);

        ArrayList<String> fullUUIDList = mTango.listAreaDescriptions();
        String tempAdfUuid = mAdfUuid;
        // Load the latest ADF if ADFs are found.
        if (fullUUIDList.size() > 0) {
            if (mAdfUuid == null || mAdfUuid.equals("")) {
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

    public boolean isConnected() {
        return mIsConnected;
    }

    /**
     * Finds plane pose in the middle of the screen.
     */
    public TangoPoseData findPlaneInMiddle() {
        return doFitPlane(0.5f, 0.5f, mRgbTimestampGlThread);
    }

    public void setActiveContent(TangoPoseData pose, Content content) {
        Pose glPose = ScenePoseCalculator.toOpenGLPose(pose);
        content.withTangoData(new TangoData(glPose));

        ActiveVisualContent activeVisualContent = new ActiveVisualContent(content);
        mVisualContentManager.setActiveContentToBeRenderedOnScreen(activeVisualContent);
    }

    public void updateActiveContent(TangoPoseData newPose) {
        if (mVisualContentManager.getActiveContent() != null) {
            mVisualContentManager.getActiveContent().setNewPose(ScenePoseCalculator.toOpenGLPose(newPose));
        }
    }

    public void addActiveToStaticContent() {
        mVisualContentManager.activeContentAddingFinished();
        removeActiveContent();
    }

    public void removeActiveContent() {
        if (mVisualContentManager.getActiveContent() != null) {
            mRenderer.removeActiveContent(mVisualContentManager.getActiveContent());
        } else {
            Log.e(TAG, "removeActiveContent() : There was no active content to remove");
        }
    }

    public void removeContent(Content content) {
        VisualContent vc = mVisualContentManager.findVisualContentByContent(content);
        if (vc != null) {
            mRenderer.removeStaticContent(vc);
        } else {
            Log.d(TAG, "removeContent() called with: " + "content = [" + content + "]  VisualContent not found");
        }

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

        return ScenePoseCalculator.planeFitToTangoWorldPose(
                intersectionPointPlaneModelPair.intersectionPoint,
                intersectionPointPlaneModelPair.planeModel, devicePose, mExtrinsics);
    }

    public void setOnContentSelectedListener(OnContentSelectedListener onContentSelectedListener) {
        this.onContentSelectedListener = onContentSelectedListener;
    }

    public void onSurfaceTouch(MotionEvent event) {
        mScaleDetector.onTouchEvent(event);
        mRenderer.onTouchEvent(event);
    }

    public void setOnContentFitListener(ContentFitter.OnContentFitListener onContentFitListener) {
        this.onContentFitListener = onContentFitListener;
    }

    public void onSaveInstanceState(Bundle outState) {
        if (mContentFitter != null) {
            outState.putSerializable("FITTING_CONTENT", mContentFitter.getContent());
        }
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        float scale = detector.getScaleFactor() != 0 ? detector.getScaleFactor() : 1f;
        if (mVisualContentManager.isActiveContentRenderedOnScreen()) {
            mVisualContentManager.getActiveContent().scaleContent(scale);
        } else {
            Log.e(TAG, "onScale() was called but active content is not on screen");
        }
        return true;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {

    }

    @Override
    public void onVisualContentSelected(VisualContent c) {
        onContentSelectedListener.onContentSelected(c != null ? c.getContent() : null);

    }

    public void onContentCreated(Content content) {
        if (mContentFitter != null) {
            Log.e(TAG, "onContentCreated: called when content was already fitting");
            return;
        }
        mContentFitter = new ContentFitter(content, this);
        mContentFitter.setFittingStatusListener(onContentFitListener);
        mContentFitter.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        onContentFitListener.onFitStatusChange(true);
    }

    public void restoreState(Bundle savedInstanceState) {
        // Restore states here
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("FITTING_CONTENT")) {
                Content c = (Content) savedInstanceState.getSerializable("FITTING_CONTENT");
                mContentFitter = new ContentFitter(c, this);
            }
        }
    }

    public void cancelFitting() {
        mContentFitter.cancel(true);
        mContentFitter = null;

        onContentFitListener.onFitStatusChange(false);
    }

    public void finishFitting() {
        mContentFitter.finishFitting();
        mContentFitter = null;
        onContentFitListener.onFitStatusChange(false);
    }


}
