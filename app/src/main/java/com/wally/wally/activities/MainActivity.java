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


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.google.atap.tango.ux.TangoUxLayout;
import com.google.atap.tangoservice.Tango;
import com.google.atap.tangoservice.TangoPoseData;
import com.projecttango.rajawali.Pose;
import com.projecttango.rajawali.ScenePoseCalculator;
import com.wally.wally.App;
import com.wally.wally.LoginManager;
import com.wally.wally.OnContentSelectedListener;
import com.wally.wally.R;
import com.wally.wally.Utils;
import com.wally.wally.datacontroller.content.Content;
import com.wally.wally.datacontroller.content.TangoData;
import com.wally.wally.fragments.NewContentDialogFragment;
import com.wally.wally.tango.ContentFitter;
import com.wally.wally.tango.TangoManager;

import org.rajawali3d.surface.RajawaliSurfaceView;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements
        OnContentSelectedListener,
        ContentFitter.OnContentFitListener,
        NewContentDialogFragment.NewContentDialogListener,
        LoginManager.LoginListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String ARG_ADF_UUID = "ARG_ADF_UUID";

    private String mAdfUuid;

    private TangoManager mTangoManager;
    private LoginManager mLoginManager;

    private FloatingActionButton mFinishFitting;
    private View mLayoutFitting;
    private List<View> mNonFittingModeViews;

    private View mSelectedMenuView;
    private long mLastSelectTime;
    private Content mSelectedContent;

    private ProgressDialog mProgressDialog;


    public static Intent newIntent(Context context, @Nullable String uuid) {
        Intent i = new Intent(context, MainActivity.class);
        i.putExtra(ARG_ADF_UUID, uuid);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        return i;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        mLayoutFitting = findViewById(R.id.layout_fitting);
        mNonFittingModeViews = Arrays.asList(findViewById(R.id.btn_map), findViewById(R.id.btn_new_post));
        mFinishFitting = (FloatingActionButton) findViewById(R.id.btn_finish_fitting);
        mSelectedMenuView = findViewById(R.id.layout_content_select);

        // Initialize managers
        RajawaliSurfaceView mSurfaceView = (RajawaliSurfaceView) findViewById(R.id.rajawali_surface);
        TangoUxLayout mTangoUxLayout = (TangoUxLayout) findViewById(R.id.layout_tango_ux);
        mAdfUuid = getIntent().getStringExtra(ARG_ADF_UUID);
        mTangoManager = new TangoManager(getBaseContext(), mSurfaceView, mTangoUxLayout, mAdfUuid);
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

        mLoginManager = new LoginManager(this);
        mLoginManager.setLoginListener(this);

        if (Utils.hasNoADFPermissions(getBaseContext())) {
            Log.i(TAG, "onCreate: Didn't had ADF permission, requesting permission");
            requestADFPermission();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        hideProgress();
        mTangoManager.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!mLoginManager.isLoggedIn()) {
            showProgress();
            mLoginManager.tryLogin();
        }
        // Synchronize against disconnecting while the service is being used in the OpenGL thread or
        // in the UI thread.
        if (Utils.hasNoADFPermissions(getBaseContext())) {
            Log.i(TAG, "onResume: Didn't have ADF permission returning.");
            return;

        }
        mTangoManager.onResume();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("mSelectedContent", mSelectedContent);
        mTangoManager.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mSelectedContent = (Content) savedInstanceState.getSerializable("mSelectedContent");
        mTangoManager.removeContent(mSelectedContent);
        onContentSelected(mSelectedContent);
    }

    public void onNewContentClick(View v) {
        NewContentDialogFragment.newInstance().show(getSupportFragmentManager(), "NewContentDialogFragment");
    }

    public void onBtnMapClick(View v) {
        Intent mapIntent = new Intent(getBaseContext(), MapsActivity.class);
        startActivity(mapIntent);
    }

    public void onEditSelectedContentClick(View view) {
        Log.d(TAG, "editSelectedContent() called with: " + "view = [" + view + "]");
        if (mSelectedContent == null) {
            Log.e(TAG, "editSelectedContent: when mSelectedContent is NULL");
            return;
        }
        NewContentDialogFragment.newInstance(mSelectedContent).show(getSupportFragmentManager(), "edit_content");
    }

    public void onDeleteSelectedContentClick(View view) {
        Log.d(TAG, "deleteSelectedContent() called with: " + "view = [" + view + "]");
        if (mSelectedContent == null) {
            Log.e(TAG, "deleteSelectedContent: when mSelectedContent is NULL");
            return;
        }
        ((App) getApplicationContext()).getDataController().delete(mSelectedContent);
        mTangoManager.removeContent(mSelectedContent);
    }


    @Override
    public void onContentSelected(Content content) {
        mSelectedContent = content;
        runOnUiThread(new Runnable() {
            @SuppressWarnings("ConstantConditions")
            @Override
            public void run() {
                mSelectedMenuView.setVisibility(mSelectedContent == null ? View.GONE : View.VISIBLE);
                mLastSelectTime = System.currentTimeMillis();

                mSelectedMenuView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // Hide iff user didn't click after
                        if (mLastSelectTime + 3000 <= System.currentTimeMillis()) {
                            mSelectedMenuView.setVisibility(View.GONE);
                            mSelectedContent = null;
                        }
                    }
                }, 3000);
            }
        });
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
    public void onContentFittingFinished(Content content, TangoPoseData pose, double scale) {
        saveActiveContent(content, ScenePoseCalculator.toOpenGLPose(pose), scale);
    }

    @Override
    public void onFitStatusChange(boolean fittingStarted) {
        mLayoutFitting.setVisibility(fittingStarted ? View.VISIBLE : View.GONE);
        for (View v : mNonFittingModeViews) {
            v.setVisibility(fittingStarted ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public void onContentCreated(Content content, boolean isEditMode) {
        if (isEditMode) {
            // remove content and start new fitting.
            mTangoManager.removeContent(content);
        }
        mTangoManager.onContentCreated(content);
    }

    public void onCancelFittingClick(View view) {
        mTangoManager.cancelFitting();
    }

    public void onFinishFittingClick(View view) {
        mTangoManager.finishFitting();
    }

    @Override
    public void onLogin(String userName) {
        Log.d(TAG, "onLogin() called with: " + "userName = [" + userName + "]");
        hideProgress();
        // TODO update views
    }

    private void saveActiveContent(Content content, Pose pose, double scale) {
        TangoData tangoData = new TangoData(pose);
        tangoData.setScale(scale);
        content.withTangoData(tangoData).withUuid(mAdfUuid);
        ((App) getApplicationContext()).getDataController().save(content);
    }


    private void showProgress() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
        mProgressDialog = ProgressDialog.show(this, null, "Loading...", true);
    }

    private void hideProgress() {
        if (mProgressDialog != null) {
            mProgressDialog.hide();
        }
    }

    private void requestADFPermission() {
        startActivityForResult(
                Tango.getRequestPermissionIntent(Tango.PERMISSIONTYPE_ADF_LOAD_SAVE),
                Tango.TANGO_INTENT_ACTIVITYCODE);
    }
}
