package com.wally.wally.controllers.main;

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
import com.google.atap.tango.ux.TangoUxLayout;
import com.google.atap.tangoservice.Tango;
import com.google.atap.tangoservice.TangoPoseData;
import com.projecttango.tangosupport.TangoPointCloudManager;
import com.wally.wally.App;
import com.wally.wally.R;
import com.wally.wally.Utils;
import com.wally.wally.adf.AdfInfo;
import com.wally.wally.adf.AdfManager;
import com.wally.wally.adf.AdfScheduler;
import com.wally.wally.adf.AdfService;
import com.wally.wally.components.PersistentDialogFragment;
import com.wally.wally.config.CameraTangoActivityConstants;
import com.wally.wally.config.Config;
import com.wally.wally.datacontroller.DataController.FetchResultCallback;
import com.wally.wally.datacontroller.DataControllerFactory;
import com.wally.wally.datacontroller.content.Content;
import com.wally.wally.datacontroller.utils.SerializableLatLng;
import com.wally.wally.renderer.ActiveContentScaleGestureDetector;
import com.wally.wally.renderer.VisualContentManager;
import com.wally.wally.renderer.WallyRenderer;
import com.wally.wally.tango.ContentFitter;
import com.wally.wally.tango.LearningEvaluator;
import com.wally.wally.tango.ProgressAggregator;
import com.wally.wally.tango.TangoFactory;
import com.wally.wally.tango.TangoManager;
import com.wally.wally.tango.TangoUpdater;
import com.wally.wally.tango.WallyTangoUx;
import com.wally.wally.tip.LocalTipService;
import com.wally.wally.tip.TipService;

import org.rajawali3d.surface.RajawaliSurfaceView;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class CameraARTangoActivity extends CameraARActivity implements
        ContentFitter.OnContentFitListener,
        TangoUpdater.LocalizationListener,
        ImportExportPermissionDialogFragment.ImportExportPermissionListener {

    private static final String TAG = CameraARTangoActivity.class.getSimpleName();
    // Permission Denied explain codes
    private static final int RC_EXPLAIN_ADF = 14;
    private static final int RC_EXPLAIN_ADF_EXPORT = 19;
    // Permission Request codes
    private static final int RC_REQ_AREA_LEARNING = 17;
    private static final int RC_SET_LOCALIZATION_LOCATION = 102;
    private boolean mExplainAdfPermission;

    private TangoManager mTangoManager;
    private WallyTangoUx mTangoUx;
    private FloatingActionButton mCreateNewContent;
    private FloatingActionButton mFinishFitting;
    private View mLayoutFitting;
    private FloatingActionButton mFinishFittingFab;
    private List<View> mNonFittingModeViews;


    private ContentFitter mContentFitter;
    private VisualContentManager mVisualContentManager;
    private WallyRenderer mRenderer;
    private SerializableLatLng mLocalizationLocation;
    private Content editableContent;

    public static Intent newIntent(Context context) {
        return new Intent(context, CameraARTangoActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mLayoutFitting = findViewById(R.id.layout_fitting);
        mFinishFittingFab = (FloatingActionButton) mLayoutFitting.findViewById(R.id.btn_finish_fitting);
        mCreateNewContent = (FloatingActionButton) findViewById(R.id.btn_new_post);
        mNonFittingModeViews = Arrays.asList(findViewById(R.id.btn_map), findViewById(R.id.btn_new_post));
        mFinishFitting = (FloatingActionButton) findViewById(R.id.btn_finish_fitting);
        RajawaliSurfaceView mSurfaceView = (RajawaliSurfaceView) findViewById(R.id.rajawali_surface);
        Config config = Config.getInstance();
        Context context = getBaseContext();

        TangoUxLayout mTangoUxLayout = (TangoUxLayout) findViewById(R.id.layout_tango_ux);

        mVisualContentManager = new VisualContentManager();
//        fetchContentForAdf(context, mAdfUuid);

        mRenderer = new WallyRenderer(context, mVisualContentManager, this);

        mSurfaceView.setSurfaceRenderer(mRenderer);
        mTangoUx = new WallyTangoUx(context);
        LearningEvaluator evaluator = new LearningEvaluator(config);

        TangoPointCloudManager pointCloudManager = new TangoPointCloudManager();

        mTangoUx.setLayout(mTangoUxLayout);
        TangoUpdater tangoUpdater = new TangoUpdater(mTangoUx, mSurfaceView, pointCloudManager);
        tangoUpdater.addLocalizationListener(this);

        TangoFactory tangoFactory = new TangoFactory(context);

        AdfManager adfManager = App.getInstance().getAdfManager();

        TipService tipService = new LocalTipService(Utils.getAssetContentAsString(getBaseContext(), "tips.json"));

        AdfScheduler adfScheduler = new AdfScheduler(adfManager);

        ProgressAggregator progressAggregator = new ProgressAggregator();
        progressAggregator.addProgressReporter(adfScheduler, 0.4);
        progressAggregator.addProgressReporter(evaluator, 0.6);

        mTangoManager = new TangoManager(config, mAnalytics, tangoUpdater,
                pointCloudManager, mRenderer, mTangoUx, mTipView, tangoFactory, adfManager, adfScheduler, evaluator, tipService);
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

    @Override
    public boolean isNewContentFabVisible() {
        return mTangoManager.isLocalized() && !mTangoManager.isLearningMode();
    }

    private void requestADFPermission() {
        startActivityForResult(
                Tango.getRequestPermissionIntent(Tango.PERMISSIONTYPE_ADF_LOAD_SAVE), RC_REQ_AREA_LEARNING);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_REQ_AREA_LEARNING) {
            if (resultCode != RESULT_OK) {
                mExplainAdfPermission = true;
            }
        }
    }

    @Override
    public void onDialogPositiveClicked(int requestCode) {
        super.onDialogPositiveClicked(requestCode);
        switch (requestCode) {
            case RC_EXPLAIN_ADF:
                requestADFPermission();
                break;
            case RC_EXPLAIN_ADF_EXPORT:
                requestExportPermission(mTangoManager.getCurrentAdf());
                break;
        }
    }

    @Override
    public void onDialogNegativeClicked(int requestCode) {
        super.onDialogNegativeClicked(requestCode);
        switch (requestCode) {
            case RC_EXPLAIN_ADF:
                finish();
                System.exit(0);
                break;
            case RC_EXPLAIN_ADF_EXPORT:
                break;
        }
    }

    private void fetchContentForAdf(String adfUuid) {
        DataControllerFactory.getDataControllerInstance()
                .fetchForUuid(adfUuid, new FetchResultCallback() {

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
        mAnalytics.onContentDelete();
    }

    @Override
    public void onSaveContent(Content content) {
        AdfInfo adfInfo = mTangoManager.getCurrentAdf();
        content.withUuid(adfInfo.withCreationLocation(mLocalizationLocation).getUuid());
        if (!adfInfo.isUploaded()) requestExportPermission(adfInfo);
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
        Config config = Config.getInstance();
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
        if (mExplainAdfPermission) {
            mExplainAdfPermission = false;
            PersistentDialogFragment.newInstance(
                    this,
                    RC_EXPLAIN_ADF,
                    R.string.explain_adf_permission,
                    R.string.give_permission,
                    R.string.close_application)
                    .show(getSupportFragmentManager(), PersistentDialogFragment.TAG);
        }

        if (Utils.hasADFPermissions(this)) {
            mTangoManager.onResume();

            if (mContentFitter != null) {
                if (mContentFitter.isCancelled()) {
                    mContentFitter = new ContentFitter(mContentFitter.getContent(), mTangoManager, mVisualContentManager, this);
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
                Utils.getAddressForLocation(CameraARTangoActivity.this, result, new Utils.Callback<String>() {
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
    public void onLocalize() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mVisualContentManager.visualContentRestoreAndShow();
                mFinishFittingFab.setEnabled(true);
                if (!mTangoManager.isLearningMode()) {
                    mCreateNewContent.setVisibility(View.VISIBLE);
                    if (!mVisualContentManager.getStaticVisualContentToAdd().hasNext()) {
                        fetchContentForAdf(mTangoManager.getCurrentAdf().getUuid());
                    }
                }
                setLocalizationLocation();
            }
        });
    }

    @Override
    public void onNotLocalize() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mVisualContentManager.visualContentSaveAndClear();
                mFinishFittingFab.setEnabled(false);
                mCreateNewContent.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onLocationPermissionGranted(int locationRequestCode) {
        super.onLocationPermissionGranted(locationRequestCode);
        if (locationRequestCode == RC_SET_LOCALIZATION_LOCATION) {
            setLocalizationLocation();
        }
    }

    private void setLocalizationLocation() {
        if (!Utils.checkHasLocationPermission(this)) {
            requestLocationPermission(RC_SET_LOCALIZATION_LOCATION);
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
}
