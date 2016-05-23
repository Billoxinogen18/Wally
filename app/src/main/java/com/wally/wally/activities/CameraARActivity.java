package com.wally.wally.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.wally.wally.App;
import com.wally.wally.OnContentSelectedListener;
import com.wally.wally.R;
import com.wally.wally.components.CircleTransform;
import com.wally.wally.datacontroller.DataController;
import com.wally.wally.datacontroller.content.Content;
import com.wally.wally.fragments.NewContentDialogFragment;
import com.wally.wally.userManager.SocialUser;
import com.wally.wally.userManager.UserManager;



/**
 * Created by shota on 5/21/16.
 */
public abstract class CameraARActivity extends LoginActivity implements OnContentSelectedListener, NewContentDialogFragment.NewContentDialogListener {
    private static final String TAG = CameraARActivity.class.getSimpleName();
    private static final int RC_SIGN_IN = 100;

    private UserManager mUserManager;
    protected DataController mDataController;

    private View mSelectedMenuView;
    private long mLastSelectTime;
    private Content mSelectedContent;

    private ProgressDialog mProgressDialog;


    public abstract void onDeleteContent(Content selectedContent);
    public abstract void onSaveContent(Content selectedContent);
    public abstract void onCreatedContent(Content contentCreated, boolean isEditMode);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSelectedMenuView = findViewById(R.id.layout_content_select);

        // Initialize managers
        mUserManager = ((App) getApplicationContext()).getUserManager(); //TODO get LoginManager from the Factory!
        mDataController = ((App) getApplicationContext()).getDataController();

    }


    @Override
    protected void onPause() {
        super.onPause();
        hideProgress();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mUserManager.isLoggedIn()) {
            displayProfileBar(mUserManager.getUser());
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("mSelectedContent", mSelectedContent);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mSelectedContent = (Content) savedInstanceState.getSerializable("mSelectedContent");
        onContentSelected(mSelectedContent);
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


    public void onNewContentClick(View v) {
        if (!mUserManager.isLoggedIn()) {
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setProviders(AuthUI.GOOGLE_PROVIDER)
                            .build(),
                    RC_SIGN_IN);
        } else {
            NewContentDialogFragment.newInstance()
                    .show(getSupportFragmentManager(), NewContentDialogFragment.TAG);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                // user is signed in!
                GoogleApiClient googleApiClient = new GoogleApiClient.Builder(this)
                        .enableAutoManage(this, this)
                        .addApi(Plus.API)
                        .addScope(Plus.SCOPE_PLUS_LOGIN)
                        .addScope(Plus.SCOPE_PLUS_PROFILE)
                        .build();
                mUserManager.loadUser(googleApiClient, new UserManager.UserLoadListener() {
                    @Override
                    public void onUserLoad(SocialUser user) {
                        displayProfileBar(user);
                        NewContentDialogFragment.newInstance()
                                .show(getSupportFragmentManager(), NewContentDialogFragment.TAG);
                    }
                });

            } else {
                // user is not signed in. Maybe just wait for the user to press
                // "sign in" again, or show a message
                //TODO
                Toast.makeText(this, "User could not login!!!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void onBtnMapClick(View v) {
        Intent mapIntent = new Intent(getBaseContext(), MapsActivity.class);
        startActivity(mapIntent);
    }

    public void onShowProfileClick(View v) {
        Intent profileIntent = ProfileActivity.newIntent(getBaseContext(), mUserManager.getUser());
        startActivity(profileIntent);
    }


    public void onEditSelectedContentClick(View view) {
        Log.d(TAG, "editSelectedContent() called with: " + "view = [" + view + "]");
        if (mSelectedContent == null) {
            Log.e(TAG, "editSelectedContent: when mSelectedContent is NULL");
            return;
        }
        NewContentDialogFragment.newInstance(mSelectedContent)
                .show(getSupportFragmentManager(), NewContentDialogFragment.TAG);
    }

    public void onDeleteSelectedContentClick(View view) {
        Log.d(TAG, "deleteSelectedContent() called with: " + "view = [" + view + "]");
        if (mSelectedContent == null) {
            Log.e(TAG, "deleteSelectedContent: when mSelectedContent is NULL");
            return;
        }
        //delete content on the server
        mDataController.delete(mSelectedContent);
        onDeleteContent(mSelectedContent);
    }

    protected void saveActiveContent(Content content) {
        onSaveContent(content);
        mDataController.save(content);
    }


    @Override
    public void onContentCreated(Content content, boolean isEditMode) {
        onCreatedContent(content, isEditMode);
    }

    @SuppressWarnings("ConstantConditions")
    private void displayProfileBar(SocialUser user) {
        findViewById(R.id.profile_bar).setVisibility(View.VISIBLE);
        Glide.with(getBaseContext())
                .load(user.getAvatarUrl())
                .override(1000, 1000)
                .transform(new CircleTransform(getBaseContext()))
                .into((ImageView) findViewById(R.id.profile_image));

        ((TextView) findViewById(R.id.profile_name)).setText(user.getFirstName());

    }

    private void showProgress() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
        mProgressDialog = ProgressDialog.show(this, null, getString(R.string.loading_message), true);
    }

    private void hideProgress() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }
}
