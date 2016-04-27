/*
 * Copyright 2016 Wally ltd. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wally.wally.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.google.atap.tango.ux.TangoUxLayout;
import com.google.atap.tangoservice.Tango;
import com.google.atap.tangoservice.TangoPoseData;
import com.projecttango.rajawali.Pose;
import com.projecttango.rajawali.ScenePoseCalculator;
import com.wally.wally.App;
import com.wally.wally.ContentSelectListener;
import com.wally.wally.R;
import com.wally.wally.Utils;
import com.wally.wally.datacontroller.Callback;
import com.wally.wally.datacontroller.content.Content;
import com.wally.wally.datacontroller.content.TangoData;
import com.wally.wally.fragments.NewContentDialogFragment;
import com.wally.wally.tango.ContentFitter;
import com.wally.wally.tango.TangoManager;
import com.wally.wally.tango.VisualContentManager;

import org.rajawali3d.surface.RajawaliSurfaceView;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;


public class MainActivity extends AppCompatActivity implements
        NewContentDialogFragment.NewContentDialogListener,
        ContentFitter.FittingStatusListener, ContentSelectListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String ARG_ADF_UUID = "ARG_ADF_UUID";

    private TangoManager mTangoManager;
    private ContentFitter mContentFitter;

    private List<View> mNonFittingModeViews;
    private View mLayoutFitting;
    private FloatingActionButton mFinishFitting;
    private String adfUuid;

    /**
     * Returns intent to start main activity.
     *
     * @param uuid can be null, if we want to start with learning mode.
     */
    public static Intent newIntent(Context context, @Nullable String uuid) {
        Intent i = new Intent(context, MainActivity.class);
        i.putExtra(ARG_ADF_UUID, uuid);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        return i;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RajawaliSurfaceView mSurfaceView = (RajawaliSurfaceView) findViewById(R.id.rajawali_surface);
        TangoUxLayout mTangoUxLayout = (TangoUxLayout) findViewById(R.id.layout_tango_ux);
        adfUuid = getIntent().getStringExtra(ARG_ADF_UUID);
        mTangoManager = new TangoManager(getBaseContext(), mSurfaceView, mTangoUxLayout, adfUuid, this);

        mLayoutFitting = findViewById(R.id.layout_fitting);
        mNonFittingModeViews = Arrays.asList(findViewById(R.id.btn_map), findViewById(R.id.btn_new_post));
        mFinishFitting = (FloatingActionButton) findViewById(R.id.btn_finish_fitting);

        if (Utils.hasNoADFPermissions(getBaseContext())) {
            Log.i(TAG, "onCreate: Didn't had ADF permission, requesting permission");
            requestADFPermission();
        }

        fetchContentForAdf(adfUuid);

        // Restore states here
        if (savedInstanceState != null && savedInstanceState.containsKey("FITTING_CONTENT")) {
            Content c = (Content) savedInstanceState.getSerializable("FITTING_CONTENT");
            mContentFitter = new ContentFitter(getBaseContext(), c, mTangoManager);
        }
    }

    private void fetchContentForAdf(String adfUuid) {
        ((App) getApplicationContext()).getDataController().fetch(adfUuid, new Callback<Collection<Content>>() {
            @Override
            public void call(final Collection<Content> result, Exception e) {
                Log.d(TAG, "call() called with: " + "result = [" + result + "], e = [" + e + "]");
                final VisualContentManager visualContentManager = mTangoManager
                        .getVisualContentManager();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        for (Content c : result) {
                            Bitmap bitmap = Utils.createBitmapFromContent(c, getBaseContext());
                            Pose pose = c.getTangoData().getPose();
                            visualContentManager.addStaticContent(
                                    new VisualContentManager.VisualContent(bitmap, pose, c));
                        }
                        mTangoManager.setVisualContentManager(visualContentManager);
                    }
                }).start();
            }
        });
    }

    private void saveActiveContent(Content content, Pose pose) {
        TangoData tangoData = new TangoData(pose);
        content.withTangoData(tangoData).withUuid(adfUuid);
        ((App) getApplicationContext()).getDataController().save(content);
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
        // Synchronize against disconnecting while the service is being used in the OpenGL thread or
        // in the UI thread.
        // TODO check if here shoud be synchronized or not
        if (Utils.hasNoADFPermissions(getBaseContext())) {
            Log.i(TAG, "onResume: Didn't have ADF permission returning.");
            return;

        }
        mTangoManager.onResume();
        if (mContentFitter != null) {
            if (mContentFitter.isCancelled()) {
                mContentFitter = new ContentFitter(getBaseContext(), mContentFitter.getContent(), mTangoManager);
            }
            mContentFitter.setFittingStatusListener(this);
            mContentFitter.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    public void onNewContentClick(View v) {
        NewContentDialogFragment dialog = new NewContentDialogFragment();
        dialog.show(getSupportFragmentManager(), "NewContentDialogFragment");
    }

    public void onBtnMapClick(View v) {
        Intent mapIntent = new Intent(getBaseContext(), MapsActivity.class);
        startActivity(mapIntent);
    }

    @Override
    public void onContentCreated(Content content) {
        if (mContentFitter != null) {
            Log.e(TAG, "onContentCreated: called when content was already fitting");
            return;
        }
        mContentFitter = new ContentFitter(getBaseContext(), content, mTangoManager);
        mContentFitter.setFittingStatusListener(this);
        mContentFitter.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        changeFittingMode(true);
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

    public void cancelFitting(View v) {
        mContentFitter.cancel(true);
        mContentFitter = null;
        changeFittingMode(false);
    }

    public void finishFitting(View v) {
        mContentFitter.finishFitting();
        changeFittingMode(false);

        // TODO save content
        saveActiveContent(mContentFitter.getContent(), ScenePoseCalculator.toOpenGLPose(mContentFitter.getPose()));
        mContentFitter = null;

    }

    private void changeFittingMode(boolean startFittingMode) {
        mLayoutFitting.setVisibility(startFittingMode ? View.VISIBLE : View.GONE);
        for (View v : mNonFittingModeViews) {
            v.setVisibility(startFittingMode ? View.GONE : View.VISIBLE);
        }
    }

    private void requestADFPermission() {
        startActivityForResult(
                Tango.getRequestPermissionIntent(Tango.PERMISSIONTYPE_ADF_LOAD_SAVE),
                Tango.TANGO_INTENT_ACTIVITYCODE);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mContentFitter != null) {
            outState.putSerializable("FITTING_CONTENT", mContentFitter.getContent());
        }
    }

    @Override
    public void onContentSelected(Content c) {
        Log.d(TAG, "onContentSelected() called with: " + "c = [" + c + "]");
    }
}
