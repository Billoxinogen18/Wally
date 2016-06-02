package com.wally.wally.activities;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
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
import com.wally.wally.App;
import com.wally.wally.R;
import com.wally.wally.Utils;
import com.wally.wally.components.WallyTangoUx;
import com.wally.wally.datacontroller.callbacks.FetchResultCallback;
import com.wally.wally.datacontroller.content.Content;
import com.wally.wally.tango.ActiveContentScaleGestureDetector;
import com.wally.wally.tango.ContentFitter;
import com.wally.wally.tango.TangoFactory;
import com.wally.wally.tango.TangoManager;
import com.wally.wally.tango.TangoUpdater;
import com.wally.wally.tango.VisualContentManager;
import com.wally.wally.tango.WallyRenderer;

import org.rajawali3d.surface.RajawaliSurfaceView;

import java.util.Arrays;
import java.util.Collection;
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


    private ContentFitter mContentFitter;
    private VisualContentManager mVisualContentManager;


    public static Intent newIntent(Context context, @Nullable String uuid) {
        Intent i = new Intent(context, CameraARTangoActivity.class);
        i.putExtra(ARG_ADF_UUID, uuid);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        return i;
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

        mVisualContentManager = new VisualContentManager();
        fetchContentForAdf(context,mAdfUuid);

        final WallyRenderer renderer = new WallyRenderer(context, mVisualContentManager);
        renderer.setOnContentSelectListener(this);

        mSurfaceView.setSurfaceRenderer(renderer);
        WallyTangoUx tangoUx = new WallyTangoUx(context);

        TangoPointCloudManager pointCloudManager = new TangoPointCloudManager();

        tangoUx.setLayout(mTangoUxLayout);

        TangoUpdater tangoUpdater = new TangoUpdater(tangoUx,mSurfaceView,pointCloudManager, mVisualContentManager);
        TangoFactory tangoFactory = new TangoFactory(context);
        mTangoManager = new TangoManager(tangoUpdater, pointCloudManager, renderer, tangoUx, tangoFactory, mAdfUuid);
        restoreState(savedInstanceState);


        final ScaleGestureDetector scaleDetector = new ScaleGestureDetector(getBaseContext(),
                new ActiveContentScaleGestureDetector(mVisualContentManager));

        mSurfaceView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //TODO check!
                scaleDetector.onTouchEvent(event);
                renderer.onTouchEvent(event);
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
                mContentFitter = new ContentFitter(mContentFitter.getContent(), mTangoManager, mVisualContentManager,this);
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
}
