package com.wally.wally.controllers.main;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.location.LocationServices;
import com.google.atap.tango.ux.TangoUxLayout;
import com.google.atap.tangoservice.Tango;
import com.google.atap.tangoservice.TangoPoseData;
import com.wally.wally.App;
import com.wally.wally.R;
import com.wally.wally.Utils;
import com.wally.wally.adf.AdfInfo;
import com.wally.wally.adf.AdfService;
import com.wally.wally.components.PersistentDialogFragment;
import com.wally.wally.config.CameraTangoActivityConstants;
import com.wally.wally.config.Config;
import com.wally.wally.datacontroller.DataController.FetchResultCallback;
import com.wally.wally.datacontroller.DataControllerFactory;
import com.wally.wally.datacontroller.content.Content;
import com.wally.wally.datacontroller.utils.SerializableLatLng;
import com.wally.wally.renderer.VisualContentManager;
import com.wally.wally.tango.ContentFitter;
import com.wally.wally.tango.MainFactory;
import com.wally.wally.tango.TangoDriver;
import com.wally.wally.tango.TangoUpdater;
import com.wally.wally.tango.WallyTangoUx;

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

    private WallyTangoUx mTangoUx;
    private TangoDriver mTangoDriver;
    private boolean mExplainAdfPermission;

    private View mLayoutFitting;
    private List<View> mNonFittingModeViews;
    private FloatingActionButton mFinishFitting;
    private FloatingActionButton mCreateNewContent;
    private FloatingActionButton mFinishFittingFab;


    private ContentFitter mContentFitter;
    private VisualContentManager mVisualContentManager;

    private Content editableContent;
    private SerializableLatLng mLocalizationLocation;
    private MainFactory mMainFactory;


    public static Intent newIntent(Context context) {
        return new Intent(context, CameraARTangoActivity.class);
    }

    private void start() {
        Context context = getBaseContext();
        mLayoutFitting = findViewById(R.id.layout_fitting);
        mFinishFittingFab = (FloatingActionButton) mLayoutFitting.findViewById(R.id.btn_finish_fitting);
        mCreateNewContent = (FloatingActionButton) findViewById(R.id.btn_new_post);
        mNonFittingModeViews = Arrays.asList(findViewById(R.id.btn_map), findViewById(R.id.btn_new_post));
        mFinishFitting = (FloatingActionButton) findViewById(R.id.btn_finish_fitting);
        TangoUxLayout tangoUxLayout = (TangoUxLayout) findViewById(R.id.layout_tango_ux);
        RajawaliSurfaceView surfaceView = (RajawaliSurfaceView) findViewById(R.id.rajawali_surface);
        mMainFactory = new MainFactory(context, mTipView, tangoUxLayout, surfaceView, this, this);
        mTangoDriver = mMainFactory.getTangoDriver();
        mVisualContentManager = mMainFactory.getVisualContentManager();
        mTangoUx = mMainFactory.getTangoUx();

        if (!Utils.hasADFPermissions(context)) {
            Log.i(TAG, "Request adf permission");
            requestADFPermission();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        restoreState(savedInstanceState);
        start();
    }

    @Override
    public boolean isNewContentFabVisible() {
        return mTangoDriver.isTangoLocalized() && !mTangoDriver.isLearningState();
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
                requestExportPermission(mTangoDriver.getAdf());
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
                mContentFitter = mMainFactory.getContentFitter(c, this);
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
        mContentFitter = mMainFactory.getContentFitter(contentCreated, this);
        mContentFitter.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        onFitStatusChange(true);

    }

    @Override
    protected void onPause() {
        super.onPause();
        mTangoDriver.pause();

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
            mTangoDriver.resume();

            if (mContentFitter != null) {
                if (mContentFitter.isCancelled()) {
                    mContentFitter = mMainFactory.getContentFitter(mContentFitter.getContent(), this);
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
                if (!mTangoDriver.isLearningState()) {
                    mCreateNewContent.setVisibility(View.VISIBLE);
                    if (!mVisualContentManager.getStaticVisualContentToAdd().hasNext()) {
                        fetchContentForAdf(mTangoDriver.getAdf().getUuid());
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
