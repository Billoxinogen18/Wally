package com.wally.wally.activities;

import android.Manifest;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.atap.tangoservice.Tango;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.wally.wally.App;
import com.wally.wally.R;
import com.wally.wally.Utils;
import com.wally.wally.datacontroller.user.User;
import com.wally.wally.fragments.PersistentDialogFragment;
import com.wally.wally.userManager.SocialUser;
import com.wally.wally.userManager.UserManager;

public class DispatcherActivity extends LoginActivity implements
        UserManager.UserLoadListener,
        GoogleApiClient.ConnectionCallbacks,
        PersistentDialogFragment.PersistentDialogListener {

    public static final int RC_EXPLAIN_LOCATION = 12;
    public static final int RC_EXPLAIN_ADF_IMPORT = 90;
    @SuppressWarnings("unused")
    private static final String TAG = DispatcherActivity.class.getSimpleName();
    private static final int RC_SIGN_IN = 100;
    private static final int RC_CODE_MY_LOCATION = 22;
    /**
     * Tango
     */
    private static final String INTENT_CLASS_PACKAGE = "com.projecttango.tango";
    private static final String INTENT_IMPORT_EXPORT_CLASSNAME = "com.google.atap.tango.RequestImportExportActivity";
    private static final String EXTRA_KEY_SOURCE_FILE = "SOURCE_FILE";

    private GoogleApiClient mGoogleApiClient;
    private View mLoadingView;

    private LatLng mCachedLocation;
    // Flags
    private boolean mFlagSignIn = true;
    private boolean mFlagLoadAdf = false;
    private boolean mFlagShowLocationExplanation = false;
    private boolean mFlagShowAdfImportExplanation = false;

    private String mAdfPathOnDisk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dispacher_activity);
        mLoadingView = findViewById(R.id.loading_view);
    }

    @Override
    protected void onStart() {
        getGoogleApiClient().connect();
        super.onStart();
    }

    protected void onStop() {
        getGoogleApiClient().disconnect();
        super.onStop();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (mFlagSignIn) {
            mFlagSignIn = false;
            trySignIn();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mFlagLoadAdf) {
            mFlagLoadAdf = false;
            loadADF();
        } else if (mFlagShowLocationExplanation) {
            mFlagShowLocationExplanation = false;
            PersistentDialogFragment.newInstance(
                    getBaseContext(),
                    RC_EXPLAIN_LOCATION,
                    R.string.explain_location_permission,
                    R.string.open_settings,
                    R.string.close_application)
                    .show(getSupportFragmentManager(), PersistentDialogFragment.TAG);
        } else if (mFlagShowAdfImportExplanation) {
            mFlagShowAdfImportExplanation = false;
            PersistentDialogFragment.newInstance(
                    getBaseContext(),
                    RC_EXPLAIN_ADF_IMPORT,
                    R.string.explain_adf_import_permission,
                    R.string.give_permission,
                    R.string.close_application)
                    .show(getSupportFragmentManager(), PersistentDialogFragment.TAG);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                firebaseAuthWithGoogle(result.getSignInAccount());
            } else {
                requestSignIn();
            }
        } else if (requestCode == Tango.TANGO_INTENT_ACTIVITYCODE) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(DispatcherActivity.this, "ADF UUID + [" + data.getDataString() + "]", Toast.LENGTH_SHORT).show();
                continueToNextActivity(data.getDataString());
            } else {
                mFlagShowAdfImportExplanation = true;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RC_CODE_MY_LOCATION) {
            if (Utils.checkLocationPermission(this)) {
                mFlagLoadAdf = true;
            } else {
                mFlagShowLocationExplanation = true;
            }
        }
    }

    @Override
    public void onDialogPositiveClicked(int requestCode) {
        // Load Adf continues correct flow.
        switch (requestCode) {
            case RC_EXPLAIN_LOCATION:
                // When activity returns try loading adf again.
                mFlagLoadAdf = true;
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
                break;
            case RC_EXPLAIN_ADF_IMPORT:
                loadADF();
                break;
        }
    }

    @Override
    public void onDialogNegativeClicked(int requestCode) {
        finish();
        System.exit(0);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Login Flow
    ///////////////////////////////////////////////////////////////////////////

    /**
     * At first it tries to silently sign in, if not it calls google sign in window
     */
    private void trySignIn() {
        Log.d(TAG, "trySignIn() called with: " + "");

        User user = App.getInstance().getDataController().getCurrentUser();
        if (user != null) {
            // already signed in
            saveUserInContext(user);
        } else {
            requestSignIn();
        }
    }

    public void requestSignIn() {
        Log.d(TAG, "requestSignIn() called with: " + "");
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(getGoogleApiClient());
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        trySignIn();
                    }
                });
    }

    @Override
    public void onUserLoad(SocialUser user) {
        loadADF();
    }

    @Override
    public void onUserLoadFailed() {
        //TODO implementation missing, show error dialog
    }

    private void saveUserInContext(User user) {
        App.getInstance().getUserManager().loadLoggedInUser(user, getGoogleApiClient(), this);
    }

    ///////////////////////////////////////////////////////////////////////////
    // ADF Flow
    ///////////////////////////////////////////////////////////////////////////

    private void loadADF() {
        Log.d(TAG, "loadADF()");

        if (!Utils.checkLocationPermission(this)) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    RC_CODE_MY_LOCATION);
            return;
        }
        if (mCachedLocation != null) {
            loadADF(mCachedLocation);
            return;
        }

        // Acquire a reference to the system Location Manager
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                mCachedLocation = Utils.extractLatLng(location);
                loadADF(mCachedLocation);
                LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            }
        };

        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(0);
        locationRequest.setFastestInterval(0);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationServices.FusedLocationApi.
                requestLocationUpdates(mGoogleApiClient, locationRequest, locationListener);
    }

    private void loadADF(@NonNull LatLng location) {
        Log.d(TAG, "loadADF: " + location);

        if (!Utils.isTangoDevice(this)) {
            continueToNextActivity();
            return;
        }
        // If downloaded no need to download again.
        if (!TextUtils.isEmpty(mAdfPathOnDisk)) {
            onAdfLoadedInFileSystem(mAdfPathOnDisk);
            return;
        }
        mLoadingView.setVisibility(View.VISIBLE);
        // TODO move with real ADF service
//        ADFService s = App.getInstance().getDataController().getADFService();
//        s.download(Utils.getAdfFilePath("0f43439c-1fda-419d-bb1a-dbc15a3bcb20"), "0f43439c-1fda-419d-bb1a-dbc15a3bcb20", new Callback<Void>() {
//            @Override
//            public void onResult(Void result) {
//                Log.d(TAG, "onResult() called with: " + "result = [" + result + "]");
//                Toast.makeText(DispatcherActivity.this, "THIS SHIT WORKS", Toast.LENGTH_SHORT).show();
//                onAdfLoadedInFileSystem(Utils.getAdfFilePath("0f43439c-1fda-419d-bb1a-dbc15a3bcb20"));
//            }
//
//            @Override
//            public void onError(Exception e) {
//                Log.d(TAG, "onError() called with: " + "e = [" + e + "]");
//                onAdfLoadedInFileSystem(null);
//            }
//        });
    }

    private void onAdfLoadedInFileSystem(String uri) {
        Log.d(TAG, "onAdfLoadedInFileSystem() called with: " + "uri = [" + uri + "]");
        mAdfPathOnDisk = uri;

        // If file couldn't be found for this location, let's move on
        if (TextUtils.isEmpty(uri)) {
            continueToNextActivity();
            return;
        }
        mLoadingView.setVisibility(View.GONE);
        Toast.makeText(DispatcherActivity.this, String.format("Downloaded ADF : [%s]", uri), Toast.LENGTH_SHORT).show();

        Intent importIntent = new Intent();
        importIntent.setClassName(INTENT_CLASS_PACKAGE, INTENT_IMPORT_EXPORT_CLASSNAME);
        importIntent.putExtra(EXTRA_KEY_SOURCE_FILE, uri);
        startActivityForResult(importIntent, Tango.TANGO_INTENT_ACTIVITYCODE);
    }


    private void continueToNextActivity() {
        continueToNextActivity(null);
    }

    private void continueToNextActivity(String uuid) {
        Intent intent;
        if (!Utils.isTangoDevice(this)) {
            // Start standard activity
            intent = CameraARStandardActivity.newIntent(getBaseContext());
        } else if (TextUtils.isEmpty(uuid)) {
            intent = ADFChooser.newIntent(this);
        } else {
            intent = CameraARTangoActivity.newIntent(getBaseContext(), uuid);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private GoogleApiClient getGoogleApiClient() {
        if (mGoogleApiClient != null) {
            return mGoogleApiClient;
        }
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .requestProfile()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addConnectionCallbacks(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .addApi(Plus.API)
                .addApi(LocationServices.API)
                .build();
        return mGoogleApiClient;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("mFlagSignIn", mFlagSignIn);
        outState.putBoolean("mFlagLoadAdf", mFlagLoadAdf);
        outState.putBoolean("mFlagShowLocationExplanation", mFlagShowLocationExplanation);
        outState.putBoolean("mFlagShowAdfImportExplanation", mFlagShowAdfImportExplanation);
        outState.putParcelable("mCachedLocation", mCachedLocation);
        outState.putString("mAdfPathOnDisk", mAdfPathOnDisk);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            mFlagSignIn = savedInstanceState.getBoolean("mFlagSignIn");
            mFlagLoadAdf = savedInstanceState.getBoolean("mFlagLoadAdf");
            mFlagShowLocationExplanation = savedInstanceState.getBoolean("mFlagShowLocationExplanation");
            mFlagShowAdfImportExplanation = savedInstanceState.getBoolean("mFlagShowAdfImportExplanation");
            mCachedLocation = savedInstanceState.getParcelable("mCachedLocation");
            mAdfPathOnDisk = savedInstanceState.getString("mAdfPathOnDisk");
        }
    }
}
