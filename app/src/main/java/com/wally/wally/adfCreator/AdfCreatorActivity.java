package com.wally.wally.adfCreator;

import android.content.Context;
import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.atap.tango.ux.TangoUxLayout;
import com.google.atap.tangoservice.Tango;
import com.google.atap.tangoservice.TangoCameraIntrinsics;
import com.google.atap.tangoservice.TangoConfig;
import com.google.atap.tangoservice.TangoCoordinateFramePair;
import com.google.atap.tangoservice.TangoErrorException;
import com.google.atap.tangoservice.TangoEvent;
import com.google.atap.tangoservice.TangoInvalidException;
import com.google.atap.tangoservice.TangoOutOfDateException;
import com.google.atap.tangoservice.TangoPoseData;
import com.google.atap.tangoservice.TangoXyzIjData;
import com.wally.wally.R;
import com.wally.wally.components.WallyTangoUx;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class AdfCreatorActivity extends AppCompatActivity implements SetAdfNameDialog.CallbackListener,
        SaveAdfTask.SaveAdfListener {

    public static final String KEY_ADF_NAME = "ADF_NAME";
    public static final String KEY_ADF_UUID = "ADF_UUID";
    public static final String KEY_SET_NAME = "SET_NAME";
    private static final String TAG = AdfCreatorActivity.class.getSimpleName();
    private static final long DELAY_TIME = 5000;
    private static final int INVALID_TEXTURE_ID = 0;
    private Tango mTango;
    private WallyTangoUx mTangoUx;
    private TangoConfig mConfig;
    // Long-running task to save the ADF.
    private SaveAdfTask mSaveAdfTask;
    private GLSurfaceView mSurfaceView;
    private AdfCreatorRenderer mRenderer;

    // NOTE: Naming indicates which thread is in charge of updating this variable
    private int mConnectedTextureIdGlThread = INVALID_TEXTURE_ID;
    private AtomicBoolean mIsFrameAvailableTangoThread = new AtomicBoolean(false);
    private boolean mIsConnected = false;

    private Runnable mFinishLearningRunnable = new Runnable() {
        @Override
        public void run() {
            if(mSetName) {
                showSetAdfNameDialog();
            }else{
                saveAdf(null);
            }
        }
    };

    private Handler mMainThreadHandler = new Handler(Looper.getMainLooper());
    private boolean mSetName;


    public static Intent newIntent(Context from, boolean setName) {
        Intent intent = new Intent(from, AdfCreatorActivity.class);
        intent.putExtra(KEY_SET_NAME, setName);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adf_creator);

        mSetName = getIntent().getBooleanExtra(KEY_SET_NAME, true);

        mSurfaceView = (GLSurfaceView) findViewById(R.id.surfaceview);
        setupRenderer();

        TangoUxLayout mTangoUxLayout = (TangoUxLayout) findViewById(R.id.layout_tango_ux);

        mTangoUx = new WallyTangoUx(getBaseContext());
        mTangoUx.setLayout(mTangoUxLayout);
    }

    /**
     * Implements SetAdfNameDialog.CallbackListener.
     */
    @Override
    public void onAdfNameOk(String name) {
        saveAdf(name);
    }

    /**
     * Implements SetAdfNameDialog.CallbackListener.
     */
    @Override
    public void onAdfNameCancelled() {
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();

        mSurfaceView.onPause();
        mMainThreadHandler.removeCallbacks(mFinishLearningRunnable);

        try {
            synchronized (this) {
                mTango.disconnectCamera(TangoCameraIntrinsics.TANGO_CAMERA_COLOR);
                // We need to invalidate the connected texture ID so that we cause a
                // re-connection in the OpenGL thread after resume
                mConnectedTextureIdGlThread = INVALID_TEXTURE_ID;
                mIsConnected = false;
                mTango.disconnect();
            }
        } catch (TangoErrorException e) {
            Log.e(TAG, getString(R.string.tango_error), e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Initialize Tango Service as a normal Android Service, since we call
        // mTango.disconnect() in onPause, this will unbind Tango Service, so
        // every time when onResume gets called, we should create a new Tango object.
        mTango = new Tango(AdfCreatorActivity.this, new Runnable() {
            // Pass in a Runnable to be called from UI thread when Tango is ready,
            // this Runnable will be running on a new thread.
            // When Tango is ready, we can call Tango functions safely here only
            // when there is no UI thread changes involved.
            @Override
            public void run() {
                mConfig = setTangoConfig(mTango);

                synchronized (AdfCreatorActivity.this) {
                    // Re-attach listeners.
                    try {
                        setUpTangoListeners();
                    } catch (TangoErrorException e) {
                        Log.e(TAG, getString(R.string.tango_error), e);
                    } catch (SecurityException e) {
                        Log.e(TAG, getString(R.string.no_permissions), e);
                    }

                    // Connect to the tango service (start receiving pose updates).
                    try {
                        mTango.connect(mConfig);
                        mIsConnected = true;
                    } catch (TangoOutOfDateException e) {
                        Log.e(TAG, getString(R.string.tango_out_of_date_exception), e);
                    } catch (TangoErrorException e) {
                        Log.e(TAG, getString(R.string.tango_error), e);
                    } catch (TangoInvalidException e) {
                        Log.e(TAG, getString(R.string.tango_invalid), e);
                    }
                }
                mMainThreadHandler.postDelayed(mFinishLearningRunnable, DELAY_TIME);
                mSurfaceView.onResume();
            }
        });
    }

    /**
     * Sets up the tango configuration object. Make sure mTango object is initialized before
     * making this call.
     */
    private TangoConfig setTangoConfig(Tango tango) {
        TangoConfig config;
        config = tango.getConfig(TangoConfig.CONFIG_TYPE_DEFAULT);
        // Check if learning mode
        // Set learning mode to config.
        config.putBoolean(TangoConfig.KEY_BOOLEAN_LEARNINGMODE, true);
        config.putBoolean(TangoConfig.KEY_BOOLEAN_COLORCAMERA, true);


        return config;
    }

    /**
     * Set up the callback listeners for the Tango service, then begin using the Motion
     * Tracking API. This is called in response to the user clicking the 'Start' Button.
     */
    private void setUpTangoListeners() {
        // Set Tango Listeners for Poses Device wrt Start of Service, Device wrt
        // ADF and Start of Service wrt ADF
        ArrayList<TangoCoordinateFramePair> framePairs = new ArrayList<>();
        framePairs.add(new TangoCoordinateFramePair(
                TangoPoseData.COORDINATE_FRAME_START_OF_SERVICE,
                TangoPoseData.COORDINATE_FRAME_DEVICE));

        mTango.connectListener(framePairs, new Tango.OnTangoUpdateListener() {
            @Override
            public void onXyzIjAvailable(TangoXyzIjData xyzIj) {
                if (mTangoUx != null) {
                    mTangoUx.updateXyzCount(xyzIj.xyzCount);
                }
            }

            // Listen to Tango Events
            @Override
            public void onTangoEvent(final TangoEvent event) {
                if (mTangoUx != null) {
                    mTangoUx.updateTangoEvent(event);
                }
            }

            @Override
            public void onPoseAvailable(TangoPoseData pose) {
                if (mTangoUx != null) {
                    mTangoUx.updatePoseStatus(pose.statusCode);

                    if (pose.statusCode != TangoPoseData.POSE_VALID) {
                        mTangoUx.showCustomMessage("Hold Still");
                    } else {
                        mTangoUx.showCustomMessage("Walk around!");
                    }
                }
            }

            @Override
            public void onFrameAvailable(int cameraId) {
                if (cameraId == TangoCameraIntrinsics.TANGO_CAMERA_COLOR) {
                    // Note that the RGB data is not passed as a parameter here.
                    // Instead, this callback indicates that you can call
                    // the {@code updateTexture()} method to have the
                    // RGB data copied directly to the OpenGL texture at the native layer.
                    // Since that call needs to be done from the OpenGL thread, what we do here is
                    // set-up a flag to tell the OpenGL thread to do that in the next run.
                    // NOTE: Even if we are using a render by request method, this flag is still
                    // necessary since the OpenGL thread run requested below is not guaranteed
                    // to run in synchrony with this requesting call.
                    mIsFrameAvailableTangoThread.set(true);
                    // Trigger an OpenGL render to update the OpenGL scene with the new RGB data.
                    mSurfaceView.requestRender();
                }
            }
        });
    }


    private void setupRenderer() {
        mSurfaceView.setEGLContextClientVersion(2);
        mRenderer = new AdfCreatorRenderer(new AdfCreatorRenderer.RenderCallback() {
            @Override
            public void preRender() {
                // This is the work that you would do on your main OpenGL render thread.

                // Synchronize against concurrently disconnecting the service triggered from the
                // UI thread.
                synchronized (AdfCreatorActivity.this) {
                    // We need to be careful to not run any Tango-dependent code in the OpenGL
                    // thread unless we know the Tango service to be properly set-up and connected.
                    if (mIsConnected) {
                        // Connect the Tango SDK to the OpenGL texture ID where we are going to
                        // render the camera.
                        // NOTE: This must be done after both the texture is generated and the Tango
                        // service is connected.
                        if (mConnectedTextureIdGlThread == INVALID_TEXTURE_ID) {
                            mConnectedTextureIdGlThread = mRenderer.getTextureId();
                            mTango.connectTextureId(TangoCameraIntrinsics.TANGO_CAMERA_COLOR,
                                    mRenderer.getTextureId());
                        }

                        // If there is a new RGB camera frame available, update the texture and
                        // scene camera pose.
                        if (mIsFrameAvailableTangoThread.compareAndSet(true, false)) {
                            mTango.updateTexture(TangoCameraIntrinsics.TANGO_CAMERA_COLOR);
                        }
                    }
                }
            }
        });
        mSurfaceView.setRenderer(mRenderer);
        // In most cases, you want to use RENDERMODE_WHEN_DIRTY and drive the OpenGL loop from the
        // {@code onFrameAvailable} callback above. This will result on a frame rate of
        // approximately 30FPS, in synchrony with the RGB camera driver.
        mSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        // If you need to render at a higher rate (i.e.: if you are also displaying animations) you
        // can a different render mode here as well.
    }


    /**
     * Save the current Area Description File.
     * Performs saving on a background thread and displays a progress dialog.
     */
    private void saveAdf(String adfName) {
        mSaveAdfTask = new SaveAdfTask(this, this, mTango, adfName);
        mSaveAdfTask.execute();
    }

    /**
     * Handles failed save from mSaveAdfTask.
     */
    @Override
    public void onSaveAdfFailed(String adfName) {
        String toastMessage = String.format(
                getResources().getString(R.string.save_adf_failed_toast_format),
                adfName);
        Toast.makeText(this, toastMessage, Toast.LENGTH_LONG).show();
        mSaveAdfTask = null;
    }

    /**
     * Handles successful save from mSaveAdfTask.
     */
    @Override
    public void onSaveAdfSuccess(String adfName, String adfUuid) {
        String toastMessage = String.format(
                getResources().getString(R.string.save_adf_success_toast_format),
                adfName, adfUuid);
        Toast.makeText(this, toastMessage, Toast.LENGTH_LONG).show();
        mSaveAdfTask = null;

        Intent data = new Intent();
        data.putExtra(KEY_ADF_UUID, adfUuid);
        data.putExtra(KEY_ADF_NAME, adfName);

        setResult(RESULT_OK, data);
        finish();
    }

    /**
     * Shows a dialog for setting the ADF name.
     */
    private void showSetAdfNameDialog() {
        SetAdfNameDialog.newInstance().show(getSupportFragmentManager(), SetAdfNameDialog.TAG);
    }
}
