package com.wally.wally.controllers.main;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.location.LocationServices;
import com.google.atap.tango.ux.TangoUxLayout;
import com.wally.wally.App;
import com.wally.wally.R;
import com.wally.wally.Utils;
import com.wally.wally.adf.AdfInfo;
import com.wally.wally.adf.AdfService;
import com.wally.wally.components.PersistentDialogFragment;
import com.wally.wally.config.CameraTangoActivityConstants;
import com.wally.wally.config.Config;
import com.wally.wally.config.WallyConfig;
import com.wally.wally.controllers.main.factory.MainFactory;
import com.wally.wally.controllers.main.factory.TangoDriverFactory;
import com.wally.wally.datacontroller.DBController;
import com.wally.wally.objects.content.SerializableLatLng;
import com.wally.wally.events.WallyEvent;
import com.wally.wally.events.WallyEventListener;
import com.wally.wally.objects.content.Content;
import com.wally.wally.progressUtils.ProgressListener;
import com.wally.wally.progressUtils.ProgressReporter;
import com.wally.wally.renderer.VisualContentManager;
import com.wally.wally.tango.ContentFitter;
import com.wally.wally.tango.TangoDriver;
import com.wally.wally.tango.TangoUpdater;
import com.wally.wally.ux.WallyTangoUx;

import org.rajawali3d.surface.RajawaliSurfaceView;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class CameraARTangoActivity extends CameraARActivity implements
        WallyEventListener,
        ContentFitter.OnContentFitListener,
        TangoUpdater.TangoUpdaterListener,
        ImportExportPermissionDialogFragment.ImportExportPermissionListener,
        ProgressListener {

    private static final String TAG = CameraARTangoActivity.class.getSimpleName();
    private static final int RC_EXPLAIN_ADF_EXPORT = 19;
    // Permission Request codes
    private static final int RC_SET_LOCALIZATION_LOCATION = 102;

    private WallyTangoUx mTangoUx;
    private TangoDriver mTangoDriver;

    private FloatingActionButton mFinishFitting;
    private FloatingActionButton mFinishFittingFab;
    private View mLayoutFitting;
    private List<View> mNonFittingModeViews;

    private ContentFitter mContentFitter;
    private VisualContentManager mVisualContentManager;

    private Content editableContent;
    private SerializableLatLng mLocalizationLocation;
    private TangoDriverFactory mTangoDriverFactory;

    private RajawaliSurfaceView mSurfaceView;


    public static Intent newIntent(Context context) {
        return new Intent(context, CameraARTangoActivity.class);
    }

    private void start() {
        mLayoutFitting = findViewById(R.id.layout_fitting);
        mFinishFittingFab = (FloatingActionButton) mLayoutFitting.findViewById(R.id.btn_finish_fitting);
        mNonFittingModeViews = Arrays.asList(findViewById(R.id.btn_map), findViewById(R.id.new_post));
        mFinishFitting = (FloatingActionButton) findViewById(R.id.btn_finish_fitting);
        TangoUxLayout tangoUxLayout = (TangoUxLayout) findViewById(R.id.layout_tango_ux);
        mSurfaceView = (RajawaliSurfaceView) findViewById(R.id.rajawali_render_view);
        MainFactory mMainFactory = new MainFactory(mTipView, tangoUxLayout, this, mSurfaceView);
        mTangoDriverFactory = new TangoDriverFactory(mMainFactory);
        mTangoDriver = mTangoDriverFactory.getTangoDriver();
        mVisualContentManager = mMainFactory.getVisualContentManager();
        mTangoUx = mMainFactory.getTangoUx();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        restoreState(savedInstanceState);
        start();
    }

    @Override
    public boolean isNewContentCreationEnabled() {
        return mTangoDriver.isTangoLocalized() && !mTangoDriver.isLearningState();
    }

    @Override
    public void onDialogPositiveClicked(int requestCode) {
        super.onDialogPositiveClicked(requestCode);
        switch (requestCode) {
            case RC_EXPLAIN_ADF_EXPORT:
                requestExportPermission(mTangoDriver.getAdf());
                break;
        }
    }

    @Override
    public void onDialogNegativeClicked(int requestCode) {
        super.onDialogNegativeClicked(requestCode);
        switch (requestCode) {
            case RC_EXPLAIN_ADF_EXPORT:
                break;
        }
    }

    private void fetchContentForAdf(String adfUuid) {
        ((App)getApplicationContext()).getDataController()
                .fetchForUuid(adfUuid, new DBController.ResultCallback() {
                    @Override
                    public void onResult(final Collection<Content> result) {
                        mVisualContentManager.createStaticContent(result);
                    }
                });
    }

    private void restoreState(Bundle savedInstanceState) {
        // Restore states here
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("FITTING_CONTENT")) {
                Content c = (Content) savedInstanceState.getSerializable("FITTING_CONTENT");
                mContentFitter = mTangoDriverFactory.getContentFitter(c, this);
            }
        }
    }

    @Override
    public void onDeleteContent(Content selectedContent) {
        mVisualContentManager.removePendingStaticContent(selectedContent);
    }

    @Override
    public void onSaveContent(Content content) {
        AdfInfo adfInfo = mTangoDriver.getAdf();
        content.withUuid(adfInfo.withCreationLocation(mLocalizationLocation).getUuid());
        if (!adfInfo.isUploaded()) {
            requestExportPermission(adfInfo);
        }
    }

    private void requestExportPermission(AdfInfo adfInfo) {
        DialogFragment df = ImportExportPermissionDialogFragment
                .newInstance(adfInfo, ImportExportPermissionDialogFragment.EXPORT);
        df.show(getSupportFragmentManager(), ImportExportPermissionDialogFragment.TAG);
    }

    @Override
    public void onPermissionGranted(AdfInfo adfInfo) {
        startUploadingAdf(adfInfo);
        adfInfo.withUploadedStatus(true);
    }

    @Override
    public void onPermissionDenied(AdfInfo adfInfo) {
        Config config = WallyConfig.getInstance();
        String message = config.getString(CameraTangoActivityConstants.ADF_EXPORT_EXPLAIN_MSG);
        String positiveText = config.getString(CameraTangoActivityConstants.ADF_EXPORT_EXPLAIN_PST_BTN);
        String negativeText = config.getString(CameraTangoActivityConstants.ADF_EXPORT_EXPLAIN_NGT_BTN);
        DialogFragment df = PersistentDialogFragment.newInstance(RC_EXPLAIN_ADF_EXPORT, null, message, positiveText, negativeText, false);
        df.show(getSupportFragmentManager(), PersistentDialogFragment.TAG);
    }

    @Override
    public void onContentCreated(Content contentCreated, boolean isEditMode) {
        if (isEditMode) {
            // remove content and start new fitting.
            mVisualContentManager.removePendingStaticContent(contentCreated);
            Log.d(TAG, "onContentCreated() deleted " + contentCreated);
            editableContent = new Content(contentCreated);
        } else {
            editableContent = null;
        }
        if (mContentFitter != null) {
            Log.e(TAG, "onContentCreated: called when content was already fitting");
            return;
        }
        mContentFitter = mTangoDriverFactory.getContentFitter(contentCreated, this);
        mContentFitter.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        onFitStatusChange(true);
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause() called with: Thread = " + Thread.currentThread());
        super.onPause();
        mSurfaceView.onPause();
        if (mTangoDriver.isTangoConnected()) {
            mTangoDriver.pause();
        }

        if (mContentFitter != null) {
            mContentFitter.cancel(true);
        }
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume() called with: Thread = " + Thread.currentThread());
        super.onResume();
        mSurfaceView.onResume();
        // Set render mode to RENDERMODE_CONTINUOUSLY to force getting onDraw callbacks until the
        // Tango service is properly set-up and we start getting onFrameAvailable callbacks.
        mSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

        if (Utils.hasADFPermissions(this)) {
            Log.d(TAG, "onResume() hasADFPermissions " + "");
            mTangoDriver.resume();

            if (mContentFitter != null) {
                if (mContentFitter.isCancelled()) {
                    mContentFitter = mTangoDriverFactory.getContentFitter(mContentFitter.getContent(), this);
                }
                mContentFitter.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                onFitStatusChange(true);
            }
        }
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mContentFitter != null) {
            outState.putSerializable("FITTING_CONTENT", mContentFitter.getContent());
        }
    }


    @Override
    public void onContentFit(final float[] pose) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mFinishFitting.setEnabled(pose != null && mVisualContentManager.isLocalized());
            }
        });
    }

    @Override
    public void onContentFittingFinished(Content content) {
        saveActiveContent(content);
    }

    @Override
    public void onFitStatusChange(boolean fittingStarted) {
        mLayoutFitting.setVisibility(fittingStarted ? View.VISIBLE : View.GONE);
        for (View v : mNonFittingModeViews) {
            v.setVisibility(fittingStarted ? View.GONE : View.VISIBLE);
        }
    }

    public void onCancelFittingClick(View view) {
        mContentFitter.cancel(true);
        mContentFitter = null;

        if (editableContent != null) {
            mVisualContentManager.addPendingStaticContent(editableContent);
            editableContent = null;
        }

        onFitStatusChange(false);
    }

    public void onFinishFittingClick(View view) {
        mContentFitter.finishFitting();
        mContentFitter = null;
        onFitStatusChange(false);
    }

    /**
     * Just syncs adf on cloud.
     * Note that we only sync when localized.
     */
    private void startUploadingAdf(final AdfInfo adfInfo) {
        if (!Utils.checkHasLocationPermission(this) || !mGoogleApiClient.isConnected()) {
            uploadAdf(adfInfo);
            return;
        }
        Utils.getNewLocation(mGoogleApiClient, new Utils.Callback<SerializableLatLng>() {
            @Override
            public void onResult(SerializableLatLng result) {
                adfInfo.withCreationLocation(result);
                Utils.getAddressForLocation(CameraARTangoActivity.this,
                        result.getLatitude(), result.getLongitude(), new Utils.Callback<String>() {
                    @Override
                    public void onResult(String address) {
                        uploadAdf(adfInfo.withName(address));
                    }

                    @Override
                    public void onError(Exception e) {
                        uploadAdf(adfInfo.withName(null));
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                uploadAdf(adfInfo);
            }
        });
    }

    private void uploadAdf(AdfInfo info) {
        AdfService adfService = App.getInstance().getAdfService();
        String path = Utils.getAdfFilePath(info.getUuid());
        adfService.upload(info.withPath(path));
    }

    @Override
    public void onLocalization(boolean localization) {
        if (localization) {
            onLocalize();
        } else {
            onNotLocalize();
        }
    }

    @Override
    public void onFrameAvailable() {
    }

    @Override
    public int priority() {
        return 100;
    }

    private void onLocalize() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mVisualContentManager.visualContentRestoreAndShow();
                mFinishFittingFab.setEnabled(true);
                if (!mTangoDriver.isLearningState()) {
                    if (!mVisualContentManager.getStaticVisualContentToAdd().hasNext()) {
                        fetchContentForAdf(mTangoDriver.getAdf().getUuid());
                    }
                }
                setLocalizationLocation();
            }
        });
    }

    private void onNotLocalize() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mVisualContentManager.visualContentSaveAndClear();
                mFinishFittingFab.setEnabled(false);
            }
        });
    }

    @Override
    public void onPermissionsGranted(int permissionReqCode) {
        super.onPermissionsGranted(permissionReqCode);
        if (permissionReqCode == RC_SET_LOCALIZATION_LOCATION) {
            setLocalizationLocation();
        }
    }

    private void setLocalizationLocation() {
        if (!Utils.checkHasLocationPermission(this)) {
            requestPermissions(RC_SET_LOCALIZATION_LOCATION);
            return;
        }
        Location myLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (myLocation == null) {
            Toast.makeText(getBaseContext(), "Couldn't get user location", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "saveActiveContent: Cannot get user location");
        } else {
            mLocalizationLocation = new SerializableLatLng(myLocation.getLatitude(), myLocation.getLongitude());
        }
    }


    @Override
    public void onMapClose() {
        super.onMapClose();
        mTangoUx.setVisible(true);
    }

    @Override
    public void onMapOpen() {
        super.onMapOpen();
        mTangoUx.setVisible(false);
    }

    @Override
    public void onProgressUpdate(ProgressReporter self, final double progress) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mNewContentButton.setVisibility(View.VISIBLE);
                mNewContentButton.setProgress((int) (progress * 100));
            }
        });
    }

    @Override
    public void onWallyEvent(WallyEvent event) {
        if (WallyEvent.TANGO_READY.equals(event.getId())) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mNewContentButton.setProgress(100);
                    mNewContentButton.setVisibility(View.VISIBLE);
                }
            });
        } else if (WallyEvent.LOCALIZATION_START.equals(event.getId())) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mNewContentButton.setVisibility(View.GONE);
                }
            });
        }
    }
}
