package com.wally.wally.activities;

import android.Manifest;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.plus.Plus;
import com.wally.wally.App;
import com.wally.wally.R;
import com.wally.wally.Utils;
import com.wally.wally.components.SelectedMenuView;
import com.wally.wally.components.UserInfoView;
import com.wally.wally.datacontroller.DataController;
import com.wally.wally.datacontroller.content.Content;
import com.wally.wally.fragments.NewContentDialogFragment;
import com.wally.wally.tango.OnVisualContentSelectedListener;
import com.wally.wally.tango.VisualContent;
import com.wally.wally.userManager.SocialUser;
import com.wally.wally.userManager.UserManager;

import java.util.Date;


/**
 * Created by shota on 5/21/16.
 */
public abstract class CameraARActivity extends GoogleApiClientActivity implements OnVisualContentSelectedListener, NewContentDialogFragment.NewContentDialogListener, SelectedMenuView.OnSelectedMenuActionListener {
    private static final String TAG = CameraARActivity.class.getSimpleName();
    private static final int REQUEST_CODE_MY_LOCATION = 22;
    protected DataController mDataController;
    protected GoogleApiClient mGoogleApiClient;
    private UserManager mUserManager;
    private SelectedMenuView mSelectedMenuView;
    private long mLastSelectTime;
    private Content mSelectedContent; //TODO may be needed to remove
    private Content mContentToSave;

    public abstract void onDeleteContent(Content selectedContent);

    public abstract void onSaveContent(Content selectedContent);

    // Called When content object is created by user
    @Override
    public abstract void onContentCreated(Content content, boolean isEditMode);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSelectedMenuView = (SelectedMenuView) findViewById(R.id.selected_menu_view);
        mSelectedMenuView.setOnSelectedMenuActionListener(this);
        // Initialize managers
        mUserManager = ((App) getApplicationContext()).getUserManager(); //TODO get LoginManager from the Factory!
        mDataController = ((App) getApplicationContext()).getDataController();


        mGoogleApiClient = new GoogleApiClient.Builder(this)
//                .enableAutoManage(this, this)
                .addOnConnectionFailedListener(this)
                .addScope(Plus.SCOPE_PLUS_LOGIN)
                .addScope(Plus.SCOPE_PLUS_PROFILE)
                .addApi(Plus.API)
                .addApi(LocationServices.API)
                .build();
    }

    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }


    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mUserManager.isLoggedIn()) {
            displayProfileBar(mUserManager.getUser());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_MY_LOCATION) {
            if (Utils.checkLocationPermission(this)) {
                saveActiveContent(mContentToSave);
            } else {
                // TODO show error that user can't add content without location permission
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("mSelectedContent", mSelectedContent);
        outState.putSerializable("mContentToSave", mContentToSave);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mSelectedContent = (Content) savedInstanceState.getSerializable("mSelectedContent");
        mContentToSave = (Content) savedInstanceState.getSerializable("mContentToSave");
        onContentSelected(mSelectedContent);
    }

    public void onContentSelected(final Content content) {
        mSelectedContent = content;
        if (App.getInstance().getUserManager().isLoggedIn()) {
            runOnUiThread(new Runnable() {
                @SuppressWarnings("ConstantConditions")
                @Override
                public void run() {
                    mSelectedMenuView.setVisibility(content == null ? View.GONE : View.VISIBLE);
                    mSelectedMenuView.setContent(content, mGoogleApiClient);
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
    }

    @Override
    public void onVisualContentSelected(VisualContent visualContent) {
        Content content = null;
        if (visualContent != null) {
            content = visualContent.getContent();
        }
        onContentSelected(content);
    }


    public void onNewContentClick(View v) {
        NewContentDialogFragment.newInstance()
                .show(getSupportFragmentManager(), NewContentDialogFragment.TAG);

    }

    public void onBtnMapClick(View v) {
        Intent mapIntent = new Intent(getBaseContext(), MapsActivity.class);
        startActivity(mapIntent);
    }

    public void onShowProfileClick(View v) {
        onProfileClick(mUserManager.getUser());
    }


    public void onEditSelectedContentClick(Content content) {
        if (content == null) {
            Log.e(TAG, "editSelectedContent: when mSelectedContent is NULL");
            return;
        }
        Log.d(TAG, "onEditSelectedContentClick() called with: " + "content = [" + content + "]");
        NewContentDialogFragment.newInstance(content)
                .show(getSupportFragmentManager(), NewContentDialogFragment.TAG);
    }

    public void onDeleteSelectedContentClick(Content content) {
        if (content == null) {
            Log.e(TAG, "deleteSelectedContent: when mSelectedContent is NULL");
            return;
        }
        //delete content on the server
        mDataController.delete(content);
        onDeleteContent(content);
    }

    public void onProfileClick(SocialUser user) {
        startActivity(MapsActivity.newIntent(this, user));
    }

    protected void saveActiveContent(Content content) {
        mContentToSave = content;
        // Check and set location to content
        if (Utils.checkLocationPermission(this)) {
            Location myLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

            if (myLocation == null) {
                Toast.makeText(this, "Couldn't get user location", Toast.LENGTH_SHORT).show();
                // TODO show error or something
                Log.e(TAG, "saveActiveContent: Cannot get user location");
                return;
            } else {
                content.withLocation(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()));
            }

            if(content.getCreationDate() == null) {
                content.withCreationDate(new Date(System.currentTimeMillis()));
            }
            onSaveContent(content);
            Log.wtf(TAG, "saveActiveContent: " + content);
            mDataController.save(content);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_CODE_MY_LOCATION);
        }
    }


    @SuppressWarnings("ConstantConditions")
    private void displayProfileBar(SocialUser user) {
        UserInfoView infoView = (UserInfoView) findViewById(R.id.profile_bar);
        infoView.setVisibility(View.VISIBLE);
        infoView.setUser(user);
    }
}
