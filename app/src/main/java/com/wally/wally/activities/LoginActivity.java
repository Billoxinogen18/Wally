package com.wally.wally.activities;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.wally.wally.App;
import com.wally.wally.R;
import com.wally.wally.Utils;
import com.wally.wally.adf.AdfManager;
import com.wally.wally.adf.AdfService;
import com.wally.wally.datacontroller.callbacks.Callback;
import com.wally.wally.datacontroller.user.User;
import com.wally.wally.datacontroller.utils.SerializableLatLng;
import com.wally.wally.userManager.SocialUser;
import com.wally.wally.userManager.UserManager;

public class LoginActivity extends GoogleApiClientActivity implements
        UserManager.UserLoadListener,
        GoogleApiClient.ConnectionCallbacks {

    @SuppressWarnings("unused")
    private static final String TAG = LoginActivity.class.getSimpleName();
    private static final int RC_SIGN_IN = 100;
    private static final int REQ_CODE_LOCATION = 129;
    /**
     * Tango
     */
    private GoogleApiClient mGoogleApiClient;
    private View mLoadingView;

    private boolean mFlagSignIn = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
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
    public void onConnectionSuspended(int i) {

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                Log.d(TAG, "onActivityResult: " + result.getSignInAccount().getEmail());
                firebaseAuthWithGoogle(result.getSignInAccount());
            } else {
                requestSignIn();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_CODE_LOCATION) {
            if (Utils.checkLocationPermission(this)) {
                createAdfManager();
            } else {
                // TODO show user that program needs Location and exit
            }
        }
    }

    /**
     * At first it tries to silently sign in, if not it calls google sign in window
     */
    private void trySignIn() {
        User user = App.getInstance().getDataController().getCurrentUser();
        if (user != null) {
            // already signed in
            saveUserInContext(user);
        } else {
            requestSignIn();
        }
    }

    public void requestSignIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(getGoogleApiClient());
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mLoadingView.setVisibility(View.VISIBLE);
        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "onComplete: " + task.getException());
                        mLoadingView.setVisibility(View.INVISIBLE);
                        trySignIn();
                    }
                });
    }

    @Override
    public void onUserLoad(SocialUser user) {
        createAdfManager();
    }

    @Override
    public void onUserLoadFailed() {
        trySignIn();
        //TODO implementation missing, show error dialog
    }

    private void saveUserInContext(User user) {
        mLoadingView.setVisibility(View.VISIBLE);
        App.getInstance().getUserManager().loadLoggedInUser(user, getGoogleApiClient(), this);
    }

    private void createAdfManager() {
        mLoadingView.setVisibility(View.VISIBLE);

        if (!Utils.checkLocationPermission(this)) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQ_CODE_LOCATION);
            return;
        }
        Utils.getNewLocation(mGoogleApiClient, new Callback<SerializableLatLng>() {
            @Override
            public void onResult(SerializableLatLng result) {
                AdfService as = App.getInstance().getAdfService();
                AdfManager.createWithLocation(result, as, new Callback<AdfManager>() {
                    @Override
                    public void onResult(AdfManager result) {
                        App.getInstance().setAdfManager(result);
                        continueToNextActivity();
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e(TAG, "onError: couldn't create adf Manager" + e);
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "onError: couldn't get location " + e);
            }
        });
    }


    private void continueToNextActivity() {
        Intent intent;
        if (!Utils.isTangoDevice(this)) {
            // Start standard activity
            intent = CameraARStandardActivity.newIntent(getBaseContext());
        } else {
            intent = CameraARTangoActivity.newIntent(getBaseContext());
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
                .requestScopes(new Scope(Scopes.PLUS_LOGIN))
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
}