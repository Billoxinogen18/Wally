package com.wally.wally;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.wally.wally.components.PersistentDialogFragment;
import com.wally.wally.components.PersistentDialogFragment.PersistentDialogListener;

/**
 * Abstract activity that helps to manage things used in activities.
 * Created by ioane5 on 8/4/16.
 */
public abstract class BaseActivity extends AppCompatActivity implements
        PersistentDialogListener {

    private static final String TAG = BaseActivity.class.getSimpleName();
    private static final int RC_LOCATION_PERMISSION = 199;

    private int mLocationRequestCode = -1;
    private boolean mShowLocationPermissionExplanation;
    private boolean mStartedAppSettingsScreen;

    /**
     * Called after location permission is granted.
     *
     * @param locationRequestCode request code that was passed when {@link #requestLocationPermission(int)}
     */
    public abstract void onLocationPermissionGranted(int locationRequestCode);

    private void onLocationPermissionGranted() {
        if (mLocationRequestCode > 0) {
            Log.e(TAG, "onLocationPermissionGranted: called when mLocationRequestCode was -1");
            return;
        }
        onLocationPermissionGranted(mLocationRequestCode);
        mLocationRequestCode = -1;
    }

    /**
     * Method that gains location permission, and if user denies permission
     * it tries to explain than gain location.
     *
     * @param locationRequestCode called  location permission is finally granted.
     */
    protected void requestLocationPermission(int locationRequestCode) {
        mLocationRequestCode = locationRequestCode;
        if (Utils.checkLocationPermission(this)) {
            onLocationPermissionGranted();
            return;
        }
        requestLocationPermission();
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                RC_LOCATION_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RC_LOCATION_PERMISSION) {
            if (Utils.checkLocationPermission(this)) {
                onLocationPermissionGranted();
            } else {
                // Note that because fragment/Dialog transactions can't happen here
                // we need to wait for onResume
                mShowLocationPermissionExplanation = true;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Check if location permission was granted in settings screen.
        if (mStartedAppSettingsScreen) {
            mStartedAppSettingsScreen = false;
            if (Utils.checkLocationPermission(this)) {
                onLocationPermissionGranted();
            } else {
                // If still not granted show Explanation again.
                mShowLocationPermissionExplanation = true;
            }
        }

        if (mShowLocationPermissionExplanation) {
            mShowLocationPermissionExplanation = false;

            PersistentDialogFragment.newInstance(
                    this,
                    RC_LOCATION_PERMISSION,
                    R.string.explain_location_permission,
                    canExplainLocationPermission() ? R.string.got_it : R.string.go_to_settings)
                    .show(getSupportFragmentManager(), PersistentDialogFragment.TAG);

        }
    }

    private boolean canExplainLocationPermission() {
        return ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION);
    }

    @Override
    public void onDialogNegativeClicked(int requestCode) {
        // We don't have negative button so ignore it.
    }

    @Override
    public void onDialogPositiveClicked(int requestCode) {
        if (requestCode == RC_LOCATION_PERMISSION) {
            if (canExplainLocationPermission()) {
                requestLocationPermission();
            } else {
                // Start settings screen.
                startInstalledAppDetailsActivity();
            }
        }
    }

    public void startInstalledAppDetailsActivity() {
        mStartedAppSettingsScreen = true;

        Intent i = new Intent();
        i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        i.addCategory(Intent.CATEGORY_DEFAULT);
        i.setData(Uri.parse("package:" + getPackageName()));
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        startActivity(i);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("mLocationRequestCode", mLocationRequestCode);
        outState.putBoolean("mShowLocationPermissionExplanation", mShowLocationPermissionExplanation);
        outState.putBoolean("mStartedAppSettingsScreen", mStartedAppSettingsScreen);

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mLocationRequestCode = savedInstanceState.getInt("mLocationRequestCode");
        mShowLocationPermissionExplanation = savedInstanceState.getBoolean("mShowLocationPermissionExplanation");
        mStartedAppSettingsScreen = savedInstanceState.getBoolean("mStartedAppSettingsScreen");
    }
}
