package com.wally.wally.activities;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.atap.tango.ux.TangoUxLayout;
import com.google.atap.tangoservice.Tango;
import com.google.atap.tangoservice.TangoPoseData;
import com.projecttango.tangosupport.TangoPointCloudManager;
import com.wally.wally.App;
import com.wally.wally.R;
import com.wally.wally.Utils;
import com.wally.wally.adfCreator.AdfCreatorActivity;
import com.wally.wally.components.WallyTangoUx;
import com.wally.wally.datacontroller.adf.ADFService;
import com.wally.wally.datacontroller.adf.AdfMetaData;
import com.wally.wally.datacontroller.adf.AdfSyncInfo;
import com.wally.wally.datacontroller.callbacks.Callback;
import com.wally.wally.datacontroller.callbacks.FetchResultCallback;
import com.wally.wally.datacontroller.content.Content;
import com.wally.wally.fragments.ImportExportPermissionDialogFragment;
import com.wally.wally.tango.ActiveContentScaleGestureDetector;
import com.wally.wally.tango.ContentFitter;
import com.wally.wally.tango.LocalizationListener;
import com.wally.wally.tango.TangoFactory;
import com.wally.wally.tango.TangoManager;
import com.wally.wally.tango.TangoUpdater;
import com.wally.wally.tango.VisualContentManager;
import com.wally.wally.tango.WallyRenderer;

import org.rajawali3d.surface.RajawaliSurfaceView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Created by shota on 5/21/16.
 */
public class CameraARTangoActivity extends CameraARActivity implements ContentFitter.OnContentFitListener, LocalizationListener, ImportExportPermissionDialogFragment.ImportExportPermissionListener {
    private static final String TAG = CameraARTangoActivity.class.getSimpleName();
    private static final String ARG_ADF_SYNC_INFO = "ARG_ADF_SYNC_INFO";

    private static final int RC_REQ_ADF_CREATE = 21;
    private static final int RC_REQ_ADF_EXPORT = 20;

    private AdfSyncInfo mAdfSyncInfo;
    private String mAdfUuid;

    private TangoManager mTangoManager;
    private FloatingActionButton mFinishFitting;
    private View mLayoutFitting;
    private FloatingActionButton mFinishFittingFab;
    private List<View> mNonFittingModeViews;


    private ContentFitter mContentFitter;
    private VisualContentManager mVisualContentManager;
    private TangoUpdater mTangoUpdater;
    private TangoPointCloudManager mPointCloudManager;
    private WallyRenderer mRenderer;
    private WallyTangoUx mTangoUx;
    private TangoFactory mTangoFactory;


    //testing
    private List<AdfSyncInfo> mAdfList;


    /**
     * Redirects to ADF chooser, because activity is being started without ADF.
     * ADF chooser will start {@link CameraARTangoActivity} with chosen ADF file.
     */
    public static Intent newIntent(Context context) {
        return ADFChooser.newIntent(context);
    }

    public static Intent newIntent(Context context, AdfSyncInfo adfSyncInfo) {
        Intent i = new Intent(context, CameraARTangoActivity.class);
        i.putExtra(ARG_ADF_SYNC_INFO, adfSyncInfo);
        return i;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mLayoutFitting = findViewById(R.id.layout_fitting);
        mFinishFittingFab = (FloatingActionButton) mLayoutFitting.findViewById(R.id.btn_finish_fitting);
        mNonFittingModeViews = Arrays.asList(findViewById(R.id.btn_map), findViewById(R.id.btn_new_post));
        mFinishFitting = (FloatingActionButton) findViewById(R.id.btn_finish_fitting);
        RajawaliSurfaceView mSurfaceView = (RajawaliSurfaceView) findViewById(R.id.rajawali_surface);

        Context context = getBaseContext();

        TangoUxLayout mTangoUxLayout = (TangoUxLayout) findViewById(R.id.layout_tango_ux);
        mAdfSyncInfo = (AdfSyncInfo) getIntent().getSerializableExtra(ARG_ADF_SYNC_INFO);
        mAdfUuid = mAdfSyncInfo.getAdfMetaData().getUuid();

        //testing
        mAdfList = new ArrayList<>();
        mAdfList.add(mAdfSyncInfo);

        mVisualContentManager = new VisualContentManager();
        fetchContentForAdf(context, mAdfUuid);

        mRenderer = new WallyRenderer(context, mVisualContentManager, this);

        mSurfaceView.setSurfaceRenderer(mRenderer);
        mTangoUx = new WallyTangoUx(context);

        mPointCloudManager = new TangoPointCloudManager();

        mTangoUx.setLayout(mTangoUxLayout);
        mTangoUpdater = new TangoUpdater(mTangoUx, mSurfaceView, mPointCloudManager);
        mTangoUpdater.addLocalizationListener(this);

        mTangoFactory = new TangoFactory(context);
        mTangoManager = new TangoManager(mTangoUpdater, mPointCloudManager, mRenderer, mTangoUx, mTangoFactory, mAdfUuid);
        restoreState(savedInstanceState);


        final ScaleGestureDetector scaleDetector = new ScaleGestureDetector(getBaseContext(),
                new ActiveContentScaleGestureDetector(mVisualContentManager));

        mSurfaceView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //TODO check!
                scaleDetector.onTouchEvent(event);
                mRenderer.onTouchEvent(event);
                return true;
            }
        });

        if (!Utils.hasADFPermissions(getBaseContext())) {
            Log.i(TAG, "onCreate: Didn't had ADF permission, requesting permission");
            requestADFPermission();
        }
    }

    private void fetchContentForAdf(Context context, String adfUuid) {
        ((App) context.getApplicationContext()).getDataController().fetchByUUID(adfUuid, new FetchResultCallback() {

            @Override
            public void onResult(final Collection<Content> result) {
                mVisualContentManager.createStaticContent(result);
            }

            @Override
            public void onError(Exception e) {
                // TODO write error
            }
        });
    }

    private void restoreState(Bundle savedInstanceState) {
        // Restore states here
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("FITTING_CONTENT")) {
                Content c = (Content) savedInstanceState.getSerializable("FITTING_CONTENT");
                mContentFitter = new ContentFitter(c, mTangoManager, mVisualContentManager, this);
            }
        }
    }

    @Override
    public void onDeleteContent(Content selectedContent) {
        mVisualContentManager.removePendingStaticContent(selectedContent);
    }

    @Override
    public void onSaveContent(Content content) {
        content.withUuid(mAdfUuid);
    }

    @Override
    public void onContentCreated(Content contentCreated, boolean isEditMode) {
        if (isEditMode) {
            // remove content and start new fitting.
            mVisualContentManager.removePendingStaticContent(contentCreated);
        }
        if (mContentFitter != null) {
            Log.e(TAG, "onContentCreated: called when content was already fitting");
            return;
        }
        mContentFitter = new ContentFitter(contentCreated, mTangoManager, mVisualContentManager, this);
        mContentFitter.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        onFitStatusChange(true);

    }

    @Override
    protected void onPause() {
        super.onPause();
        mTangoManager.onPause();

        if (mContentFitter != null) {
            mContentFitter.cancel(true);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTangoManager.onResume();

        if (mContentFitter != null) {
            if (mContentFitter.isCancelled()) {
                mContentFitter = new ContentFitter(mContentFitter.getContent(), mTangoManager, mVisualContentManager, this);
            }
            mContentFitter.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            onFitStatusChange(true);
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
    public void onContentFit(final TangoPoseData pose) {
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

        onFitStatusChange(false);
    }

    public void onFinishFittingClick(View view) {
        mContentFitter.finishFitting();
        mContentFitter = null;
        onFitStatusChange(false);
    }

    private void requestADFPermission() {
        startActivityForResult(
                Tango.getRequestPermissionIntent(Tango.PERMISSIONTYPE_ADF_LOAD_SAVE),
                Tango.TANGO_INTENT_ACTIVITYCODE);
    }

    /**
     * Just syncs adf on cloud.
     * Note that we only sync when localized.
     */
    private void startUploadingAdf() {
        mAdfSyncInfo.setIsSynchronized(true);
        ADFService s = App.getInstance().getDataController().getADFService();
        s.upload(Utils.getAdfFilePath(mAdfUuid), mAdfSyncInfo.getAdfMetaData());
    }

    /**
     * same {@link #localized()} method called in main thread.
     */
    public void localizedMainThread() {
        // Sync adf when localized
        if (!mAdfSyncInfo.isSynchronized()) {
            // Get new position if possible, or otherwise upload with old location.
            if (Utils.checkLocationPermission(this) && mGoogleApiClient.isConnected()) {
                Utils.getNewLocation(mGoogleApiClient, new Callback<LatLng>() {
                    @Override
                    public void onResult(LatLng result) {
                        mAdfSyncInfo.getAdfMetaData().setLatLng(result);
                        startUploadingAdf();
                    }

                    @Override
                    public void onError(Exception e) {
                        startUploadingAdf();
                    }
                });
            } else {
                startUploadingAdf();
            }
        }
        mVisualContentManager.localized();
        mFinishFittingFab.setEnabled(true);
    }

    @Override
    public void localized() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                localizedMainThread();
            }
        });
    }

    @Override
    public void notLocalized() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mVisualContentManager.notLocalized();
                mFinishFittingFab.setEnabled(false);
            }
        });
    }


    private void startCreatingNewAdf() {
        startActivityForResult(AdfCreatorActivity.newIntent(getBaseContext(), false), RC_REQ_ADF_CREATE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_REQ_ADF_CREATE) {
            if (resultCode == RESULT_OK) {
                String uuid = data.getStringExtra(AdfCreatorActivity.KEY_ADF_UUID);

                requestExportPermission(uuid);
            }
        }
    }

    private void requestExportPermission(String uuid) {
        DialogFragment df = ImportExportPermissionDialogFragment
                .newInstance(uuid, ImportExportPermissionDialogFragment.EXPORT, RC_REQ_ADF_EXPORT);
        df.show(getSupportFragmentManager(), ImportExportPermissionDialogFragment.TAG);
    }


    @Override
    public void onPermissionGranted(int reqCode, String uuid) {
        if (reqCode == RC_REQ_ADF_EXPORT) {
            startArWithSelectedAdf(uuid);
        }
    }

    private void startArWithSelectedAdf(String uuid) {
        if (Utils.checkLocationPermission(this)) {
            Location myLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

            if (myLocation == null) {
                Toast.makeText(this, "Couldn't get user location", Toast.LENGTH_SHORT).show();
                // TODO show error or something
                Log.e(TAG, "saveActiveContent: Cannot get user location");
            } else {
                LatLng currentLocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                AdfSyncInfo syncInfo = new AdfSyncInfo(
                        new AdfMetaData(uuid, uuid, currentLocation), true);

                reloadNewUuid(syncInfo);

            }
        }
    }

    private void reloadNewUuid(AdfSyncInfo syncInfo) {
        mAdfSyncInfo = syncInfo;
        mAdfUuid = syncInfo.getAdfMetaData().getUuid();

        mVisualContentManager.notLocalized(); //TODO to remove all static content
        fetchContentForAdf(getBaseContext(), mAdfUuid);
        mTangoManager.onPause();
        mTangoManager = new TangoManager(mTangoUpdater, mPointCloudManager, mRenderer, mTangoUx, mTangoFactory, mAdfUuid);

    }

    @Override
    public void onPermissionDenied(int reqCode, String uuid) {

    }
}
