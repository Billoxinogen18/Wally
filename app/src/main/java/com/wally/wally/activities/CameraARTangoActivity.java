package com.wally.wally.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import com.google.atap.tango.ux.TangoUxLayout;
import com.google.atap.tangoservice.Tango;
import com.google.atap.tangoservice.TangoPoseData;
import com.projecttango.tangosupport.TangoPointCloudManager;
import com.wally.wally.R;
import com.wally.wally.Utils;
import com.wally.wally.components.WallyTangoUx;
import com.wally.wally.datacontroller.content.Content;
import com.wally.wally.tango.ActiveContentScaleGestureDetector;
import com.wally.wally.tango.ContentFitter;
import com.wally.wally.tango.TangoManager;
import com.wally.wally.tango.TangoUpdater;
import com.wally.wally.tango.VisualContentManager;
import com.wally.wally.tango.WallyRenderer;

import org.rajawali3d.surface.RajawaliSurfaceView;

import java.util.Arrays;
import java.util.List;

/**
 * Created by shota on 5/21/16.
 */
public class CameraARTangoActivity extends CameraARActivity implements ContentFitter.OnContentFitListener {
    private static final String TAG = CameraARTangoActivity.class.getSimpleName();
    private static final String ARG_ADF_UUID = "ARG_ADF_UUID";

    private String mAdfUuid;

    private TangoManager mTangoManager;
    private FloatingActionButton mFinishFitting;
    private View mLayoutFitting;
    private List<View> mNonFittingModeViews;


    public static Intent newIntent(Context context, @Nullable String uuid) {
        Intent i = new Intent(context, CameraARTangoActivity.class);
        i.putExtra(ARG_ADF_UUID, uuid);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        return i;
    }

    @Override
    public void onDeleteContent(Content selectedContent) {
        mTangoManager.removeContent(selectedContent);
    }

    @Override
    public void onSaveContent(Content content) {
        content.withUuid(mAdfUuid);
    }

    @Override
    public void onCreatedContent(Content contentCreated, boolean isEditMode) {
        if (isEditMode) {
            // remove content and start new fitting.
            mTangoManager.removeContent(contentCreated);
        }
        mTangoManager.onContentCreated(contentCreated);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mLayoutFitting = findViewById(R.id.layout_fitting);
        mNonFittingModeViews = Arrays.asList(findViewById(R.id.btn_map), findViewById(R.id.btn_new_post));
        mFinishFitting = (FloatingActionButton) findViewById(R.id.btn_finish_fitting);
        RajawaliSurfaceView mSurfaceView = (RajawaliSurfaceView) findViewById(R.id.rajawali_surface);

        Context context = getBaseContext();

        TangoUxLayout mTangoUxLayout = (TangoUxLayout) findViewById(R.id.layout_tango_ux);
        mAdfUuid = getIntent().getStringExtra(ARG_ADF_UUID);

        VisualContentManager visualContentManager = new VisualContentManager();

        WallyRenderer renderer = new WallyRenderer(context, visualContentManager);

        mSurfaceView.setSurfaceRenderer(renderer);
        WallyTangoUx tangoUx = new WallyTangoUx(context);

        TangoPointCloudManager pointCloudManager = new TangoPointCloudManager();

        tangoUx.setLayout(mTangoUxLayout);

        TangoUpdater tangoUpdater = new TangoUpdater(tangoUx,mSurfaceView,pointCloudManager);
        ScaleGestureDetector scaleDetector = new ScaleGestureDetector(getBaseContext(),
                new ActiveContentScaleGestureDetector(visualContentManager));

        mTangoManager = new TangoManager(getBaseContext(), tangoUpdater, mTangoUxLayout,
                pointCloudManager, visualContentManager, renderer, tangoUx, mAdfUuid, scaleDetector);
        mTangoManager.setOnContentSelectedListener(this);
        mTangoManager.setOnContentFitListener(this);
        mTangoManager.restoreState(savedInstanceState);

        mSurfaceView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mTangoManager.onSurfaceTouch(event);
                return true;
            }
        });

        if (!Utils.hasADFPermissions(getBaseContext())) {
            Log.i(TAG, "onCreate: Didn't had ADF permission, requesting permission");
            requestADFPermission();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mTangoManager.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTangoManager.onResume();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mTangoManager.onSaveInstanceState(outState);
    }


    @Override
    public void onContentFit(final TangoPoseData pose) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mFinishFitting.setEnabled(pose != null);
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
        mTangoManager.cancelFitting();
    }

    public void onFinishFittingClick(View view) {
        mTangoManager.finishFitting();
    }

    private void requestADFPermission() {
        startActivityForResult(
                Tango.getRequestPermissionIntent(Tango.PERMISSIONTYPE_ADF_LOAD_SAVE),
                Tango.TANGO_INTENT_ACTIVITYCODE);
    }
}
