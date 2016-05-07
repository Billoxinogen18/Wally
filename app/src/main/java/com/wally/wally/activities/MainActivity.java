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
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
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
import com.wally.wally.tango.VisualContent;
import com.wally.wally.tango.VisualContentManager;

import org.rajawali3d.surface.RajawaliSurfaceView;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;


public class MainActivity extends AppCompatActivity implements
        NewContentDialogFragment.NewContentDialogListener,
        ContentFitter.FittingStatusListener, ContentSelectListener, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String ARG_ADF_UUID = "ARG_ADF_UUID";
    private static final int REQUEST_CODE_SIGN_IN = 128;

    private TangoManager mTangoManager;
    private ContentFitter mContentFitter;

    private List<View> mNonFittingModeViews;
    private View mLayoutFitting;
    private FloatingActionButton mFinishFitting;
    private String adfUuid;

    private Content mSelectedContent;
    private long mLastSelectTime;
    private GoogleApiClient mGoogleApiClient;
    private ProgressDialog mProgressDialog;

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

    @SuppressWarnings("ConstantConditions")
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
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("FITTING_CONTENT")) {
                Content c = (Content) savedInstanceState.getSerializable("FITTING_CONTENT");
                mContentFitter = new ContentFitter(c, mTangoManager);
            }
            onContentSelected((Content) savedInstanceState.getSerializable("mSelectedContent"));
        }

        // TODO refactor WITH GioGoG
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestId()
                .requestIdToken("128879657860-0skgvreu35ilodekimh7l3mbia49o0nu.apps.googleusercontent.com")
                .build();

        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        findViewById(R.id.btn_google_sign_in).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onGoogleSignInClicked();
            }
        });
        // silent sign in.
        OptionalPendingResult<GoogleSignInResult> pendingResult =
                Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        if (pendingResult.isDone()) {
            // There's immediate result available.
            handleSignInResult(pendingResult.get());
        } else {
            findViewById(R.id.btn_new_post).setVisibility(View.GONE);
            // There's no immediate result ready, displays some progress indicator and waits for the
            // async callback.
            showProgressDialog();
            pendingResult.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(@NonNull GoogleSignInResult result) {
                    handleSignInResult(result);
                    hideProgressDialog();
                }
            });
        }
    }

    public void onGoogleSignInClicked() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, REQUEST_CODE_SIGN_IN);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == REQUEST_CODE_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            App app = ((App) getApplicationContext());
            app.getDataController().googleAuth(acct.getIdToken(), new Callback<Boolean>() {
                @Override
                public void call(Boolean result, Exception e) {
                    if (e != null) {
                        Log.d("auth", result.toString());
                    } else {
                        Log.d("auth", e.toString());
                    }
                }
            });
            // TODO get token and send to firebase
            Log.d(TAG, "handleSignInResult: " + acct.getIdToken());

            findViewById(R.id.btn_google_sign_in).setVisibility(View.GONE);
            findViewById(R.id.btn_new_post).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.btn_google_sign_in).setVisibility(View.VISIBLE);
            findViewById(R.id.btn_new_post).setVisibility(View.GONE);
            // Signed out, show unauthenticated UI.
        }
    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
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
                            visualContentManager.addStaticContentToBeRenderedOnScreen(new VisualContent(c));
                        }
                        mTangoManager.setVisualContentManager(visualContentManager);
                    }
                }).start();
            }
        });
    }

    private void saveActiveContent(Content content, Pose pose, double scale) {
        TangoData tangoData = new TangoData(pose);
        tangoData.setScale(scale);
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
        // Synchronize against discoxnnecting while the service is being used in the OpenGL thread or
        // in the UI thread.
        // TODO check if here shoud be synchronized or not
        if (Utils.hasNoADFPermissions(getBaseContext())) {
            Log.i(TAG, "onResume: Didn't have ADF permission returning.");
            return;

        }
        mTangoManager.onResume();
        if (mContentFitter != null) {
            if (mContentFitter.isCancelled()) {
                mContentFitter = new ContentFitter(mContentFitter.getContent(), mTangoManager);
            }
            mContentFitter.setFittingStatusListener(this);
            mContentFitter.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    public void onNewContentClick(View v) {
        NewContentDialogFragment.newInstance().show(getSupportFragmentManager(), "NewContentDialogFragment");
    }

    public void onBtnMapClick(View v) {
        Intent mapIntent = new Intent(getBaseContext(), MapsActivity.class);
        startActivity(mapIntent);
    }

    @Override
    public void onContentCreated(Content content, boolean isEditMode) {
        if (mContentFitter != null) {
            Log.e(TAG, "onContentCreated: called when content was already fitting");
            return;
        }
        if (isEditMode) {
            // remove content and start new fitting.
            mTangoManager.removeContent(content);
        }
        mContentFitter = new ContentFitter(content, mTangoManager);
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
        saveActiveContent(mContentFitter.getContent(), ScenePoseCalculator.toOpenGLPose(mContentFitter.getPose()), mContentFitter.getScale());
        mContentFitter.finishFitting();
        changeFittingMode(false);
        mContentFitter = null;
    }

    private void changeFittingMode(boolean startFittingMode) {
        mLayoutFitting.setVisibility(startFittingMode ? View.VISIBLE : View.GONE);
        for (View v : mNonFittingModeViews) {
            v.setVisibility(startFittingMode ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public void onContentSelected(final Content c) {
        Log.d(TAG, "onContentSelected() called with: " + "c = [" + c + "]");
        runOnUiThread(new Runnable() {
            @SuppressWarnings("ConstantConditions")
            @Override
            public void run() {
                mLastSelectTime = System.currentTimeMillis();

                mSelectedContent = c;
                final View root = findViewById(R.id.layout_content_select);
                root.setVisibility(mSelectedContent == null ? View.GONE : View.VISIBLE);

                // hide after 3 secs
                root.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // Hide iff user didn't click after
                        if (mLastSelectTime + 3000 <= System.currentTimeMillis()) {
                            root.setVisibility(View.GONE);
                            mSelectedContent = null;
                        }
                    }
                }, 3000);
            }
        });
    }

    public void editSelectedContent(View view) {
        Log.d(TAG, "editSelectedContent() called with: " + "view = [" + view + "]");
        if (mSelectedContent == null) {
            Log.e(TAG, "editSelectedContent: when mSelectedContent is NULL");
            return;
        }
        NewContentDialogFragment.newInstance(mSelectedContent).show(getSupportFragmentManager(), "edit_content");
    }

    public void deleteSelectedContent(View view) {
        Log.d(TAG, "deleteSelectedContent() called with: " + "view = [" + view + "]");
        if (mSelectedContent == null) {
            Log.e(TAG, "deleteSelectedContent: when mSelectedContent is NULL");
            return;
        }
        ((App) getApplicationContext()).getDataController().delete(mSelectedContent);
        mTangoManager.removeContent(mSelectedContent);
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
            outState.putSerializable("mSelectedContent", mSelectedContent);
        }
    }

}
