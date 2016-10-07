package com.wally.wally;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.atap.tangoservice.Tango;
import com.wally.wally.components.PersistentDialogFragment;
import com.wally.wally.components.PersistentDialogFragment.PersistentDialogListener;
import com.wally.wally.controllers.map.BaseFragment;

import java.util.List;

/**
 * Abstract activity that helps to manage things used in activities.
 * Created by ioane5 on 8/4/16.
 */
public abstract class BaseActivity extends AppCompatActivity implements
        PersistentDialogListener {

    private static final String TAG = BaseActivity.class.getSimpleName();
    private static final int RC_PERMISSIONS = 199;
    // Permission Denied explain codes
    private static final int RC_EXPLAIN_ADF = 14;
    private static final int RC_REQ_AREA_LEARNING = Tango.TANGO_INTENT_ACTIVITYCODE;

    private int mPermissionRequestCode = -1;
    private boolean mShowPermissionsExplanation;
    private boolean mStartedAppSettingsScreen;
    private boolean mExplainAdfPermission;

    /**
     * Called after location permission is granted.
     *
     * @param permissionCode request code that was passed when {@link #requestPermissions(int)}}
     */
    protected abstract void onPermissionsGranted(int permissionCode);

    /**
     * Called when ADF permissions are granted.
     */
    @CallSuper
    protected void onAdfPermissionsGranted() {}

    private void onPermissionsGranted() {
        if (mPermissionRequestCode < 0) {
            Log.e(TAG, "onLocationPermissionGranted: called when mPermissionRequestCode was -1");
            return;
        }
        onPermissionsGranted(mPermissionRequestCode);
        List<Fragment> fragmentList = getSupportFragmentManager().getFragments();
        if (fragmentList != null) {
            for (Fragment fragment : fragmentList) {
                if (fragment instanceof BaseFragment) {
                    ((BaseFragment) fragment).onPermissionsGranted(mPermissionRequestCode);
                }
            }
        }
        mPermissionRequestCode = -1;
    }

    /**
     * Method that gains location permission, and if user denies permission
     * it tries to explain than gain location.
     *
     * @param permissionRequestCode called  location permission is finally granted.
     */
    public final void requestPermissions(int permissionRequestCode) {
        mPermissionRequestCode = permissionRequestCode;
        if (Utils.checkHasLocationPermission(this) &&
                Utils.checkHasCameraPermission(this) &&
                Utils.checkHasExternalStorageReadWritePermission(this)) {
            onPermissionsGranted();
            return;
        }
        requestPermissions();
    }

    public void requestADFPermission() {
        startActivityForResult(
                Tango.getRequestPermissionIntent(Tango.PERMISSIONTYPE_ADF_LOAD_SAVE), RC_REQ_AREA_LEARNING);
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                },
                RC_PERMISSIONS);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RC_PERMISSIONS) {
            if (Utils.checkHasLocationPermission(this)) {
                onPermissionsGranted();
            } else {
                // Note that because fragment/Dialog transactions can't happen here
                // we need to wait for onResume
                mShowPermissionsExplanation = true;
            }
        }
    }

    @CallSuper
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_REQ_AREA_LEARNING) {
            if (resultCode != RESULT_OK) {
                mExplainAdfPermission = true;
            } else {
                onAdfPermissionsGranted();
            }
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
        // Check if location permission was granted in settings screen.
        if (mStartedAppSettingsScreen) {
            mStartedAppSettingsScreen = false;
            if (Utils.checkHasLocationPermission(this)) {
                onPermissionsGranted();
            } else {
                // If still not granted show Explanation again.
                mShowPermissionsExplanation = true;
            }
        }

        if (mShowPermissionsExplanation) {
            mShowPermissionsExplanation = false;

            PersistentDialogFragment.newInstance(
                    this,
                    RC_PERMISSIONS,
                    R.string.explain_all_permissions,
                    canExplainLocationPermission() ? R.string.got_it : R.string.go_to_settings)
                    .show(getSupportFragmentManager(), PersistentDialogFragment.TAG);

        }
    }

    private boolean canExplainLocationPermission() {
        return ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION) ||
                ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA) ||
                ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    @CallSuper
    @Override
    public void onDialogNegativeClicked(int requestCode) {
        switch (requestCode) {
            case RC_EXPLAIN_ADF:
                finish();
                System.exit(0);
                break;
        }
    }

    @CallSuper
    @Override
    public void onDialogPositiveClicked(int requestCode) {
        switch (requestCode) {
            case RC_EXPLAIN_ADF:
                requestADFPermission();
                break;
            case RC_PERMISSIONS:
                if (canExplainLocationPermission()) {
                    requestPermissions();
                } else {
                    // Start settings screen.
                    startInstalledAppDetailsActivity();
                }
                break;
        }
    }

    private void startInstalledAppDetailsActivity() {
        mStartedAppSettingsScreen = true;
        startActivity(Utils.getAppSettingsIntent(this));
    }

    @CallSuper
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("mPermissionRequestCode", mPermissionRequestCode);
        outState.putBoolean("mShowPermissionsExplanation", mShowPermissionsExplanation);
        outState.putBoolean("mStartedAppSettingsScreen", mStartedAppSettingsScreen);

    }

    @CallSuper
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mPermissionRequestCode = savedInstanceState.getInt("mPermissionRequestCode");
        mShowPermissionsExplanation = savedInstanceState.getBoolean("mShowPermissionsExplanation");
        mStartedAppSettingsScreen = savedInstanceState.getBoolean("mStartedAppSettingsScreen");
    }
}
