package com.wally.wally.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
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
import com.wally.wally.adfCreator.AdfInfo;
import com.wally.wally.adfCreator.AdfManager;
import com.wally.wally.components.WallyTangoUx;
import com.wally.wally.datacontroller.adf.ADFService;
import com.wally.wally.datacontroller.callbacks.Callback;
import com.wally.wally.datacontroller.callbacks.FetchResultCallback;
import com.wally.wally.datacontroller.content.Content;
import com.wally.wally.datacontroller.utils.SerializableLatLng;
import com.wally.wally.fragments.ImportExportPermissionDialogFragment;
import com.wally.wally.fragments.PersistentDialogFragment;
import com.wally.wally.tango.ActiveContentScaleGestureDetector;
import com.wally.wally.tango.ContentFitter;
import com.wally.wally.tango.LearningEvaluator;
import com.wally.wally.tango.LocalizationListener;
import com.wally.wally.tango.TangoFactory;
import com.wally.wally.tango.TangoManager;
import com.wally.wally.tango.TangoUpdater;
import com.wally.wally.tango.VisualContentManager;
import com.wally.wally.tango.WallyRenderer;

import org.rajawali3d.surface.RajawaliSurfaceView;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class CameraARTangoActivity extends CameraARActivity implements
        ContentFitter.OnContentFitListener,
        LocalizationListener,
        ImportExportPermissionDialogFragment.ImportExportPermissionListener,
        PersistentDialogFragment.PersistentDialogListener {
    private static final String TAG = CameraARTangoActivity.class.getSimpleName();
    // Permission Denied explain codes
    private static final int RC_EXPLAIN_ADF = 14;
    // Permission Request codes
    private static final int RC_REQ_AREA_LEARNING = 17;
    private int REQUEST_CODE_MY_LOCATION = 1;
    private boolean mExplainAdfPermission;

    private TangoManager mTangoManager;
    private FloatingActionButton mCreateNewContent;
    private FloatingActionButton mFinishFitting;
    private View mLayoutFitting;
    private FloatingActionButton mFinishFittingFab;
    private List<View> mNonFittingModeViews;


    private ContentFitter mContentFitter;
    private VisualContentManager mVisualContentManager;
    private WallyRenderer mRenderer;
    private SerializableLatLng mLocalizationLocation;

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

        Context context = getBaseContext();

        TangoUxLayout mTangoUxLayout = (TangoUxLayout) findViewById(R.id.layout_tango_ux);

        mVisualContentManager = new VisualContentManager();
//        fetchContentForAdf(context, mAdfUuid);

        mRenderer = new WallyRenderer(context, mVisualContentManager, this);

        mSurfaceView.setSurfaceRenderer(mRenderer);
        WallyTangoUx tangoUx = new WallyTangoUx(context);
        LearningEvaluator evaluator = new LearningEvaluator();

        TangoPointCloudManager pointCloudManager = new TangoPointCloudManager();

        tangoUx.setLayout(mTangoUxLayout);
        TangoUpdater tangoUpdater = new TangoUpdater(tangoUx, mSurfaceView, pointCloudManager);
        tangoUpdater.addLocalizationListener(this);

        TangoFactory tangoFactory = new TangoFactory(context);

        AdfManager adfManager = App.getInstance().getAdfManager();
        mTangoManager = new TangoManager(tangoUpdater, pointCloudManager, mRenderer, tangoUx,
                tangoFactory, adfManager, evaluator);
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
        switch (requestCode) {
            case RC_EXPLAIN_ADF:
                requestADFPermission();
                break;
        }
    }

    @Override
    public void onDialogNegativeClicked(int requestCode) {
        finish();
        System.exit(0);
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
        AdfInfo adfInfo = mTangoManager.getCurrentAdf();

        adfInfo.getMetaData().setLatLng(mLocalizationLocation);

        content.withUuid(adfInfo.getUuid());
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
        adfInfo.withUploaded(true);
    }

    @Override
    public void onPermissionDenied(AdfInfo adfInfo) {
//TODO !!!
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
        final ADFService s = App.getInstance().getDataController().getADFService();

        if (Utils.checkLocationPermission(this) && mGoogleApiClient.isConnected()) {
            Utils.getNewLocation(mGoogleApiClient, new Callback<SerializableLatLng>() {
                @Override
                public void onResult(SerializableLatLng result) {
                    adfInfo.getMetaData().setLatLng(result);
                    Utils.getAddressForLocation(CameraARTangoActivity.this, result, new Callback<String>() {
                        @Override
                        public void onResult(String address) {
                            adfInfo.getMetaData().setName(address);
                            s.upload(Utils.getAdfFilePath(adfInfo.getUuid()), adfInfo.getMetaData());
                        }

                        @Override
                        public void onError(Exception e) {
                            adfInfo.getMetaData().setName(null);
                            s.upload(Utils.getAdfFilePath(adfInfo.getUuid()), adfInfo.getMetaData());
                        }
                    });
                }

                @Override
                public void onError(Exception e) {
                    s.upload(Utils.getAdfFilePath(adfInfo.getUuid()), adfInfo.getMetaData());
                }
            });
        } else {
            s.upload(Utils.getAdfFilePath(adfInfo.getUuid()), adfInfo.getMetaData());
        }
    }


    @Override
    public void localized() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mVisualContentManager.localized();
                mFinishFittingFab.setEnabled(true);
                mCreateNewContent.setVisibility(View.VISIBLE);
            }
        });
        if (!mTangoManager.isLearningMode())
            fetchContentForAdf(getBaseContext(), mTangoManager.getCurrentAdf().getUuid());
        setLocalizationLocation();
    }

    private void setLocalizationLocation() {
        if (Utils.checkLocationPermission(getBaseContext())) {
            Location myLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

            if (myLocation == null) {
                Toast.makeText(getBaseContext(), "Couldn't get user location", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "saveActiveContent: Cannot get user location");
            } else {
                mLocalizationLocation = new SerializableLatLng(myLocation.getLatitude(), myLocation.getLongitude());
            }
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_CODE_MY_LOCATION);
        }
    }


    @Override
    public void notLocalized() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mVisualContentManager.notLocalized();
                mFinishFittingFab.setEnabled(false);
                mCreateNewContent.setVisibility(View.GONE);
            }
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_MY_LOCATION) {
            if (Utils.checkLocationPermission(this)) {
                setLocalizationLocation();
            }
            // TODO show error that user can't add content without location permission
        }
    }

}
