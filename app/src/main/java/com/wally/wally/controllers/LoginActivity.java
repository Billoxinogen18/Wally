package com.wally.wally.controllers;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
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
import com.wally.wally.BaseActivity;
import com.wally.wally.R;
import com.wally.wally.Utils;
import com.wally.wally.adf.AdfManager;
import com.wally.wally.adf.AdfService;
import com.wally.wally.controllers.main.CameraARStandardActivity;
import com.wally.wally.controllers.main.CameraARTangoActivity;
import com.wally.wally.datacontroller.DataControllerFactory;
import com.wally.wally.datacontroller.user.User;
import com.wally.wally.datacontroller.utils.SerializableLatLng;
import com.wally.wally.userManager.SocialUser;
import com.wally.wally.userManager.SocialUserManager;

public class LoginActivity extends BaseActivity implements
        SocialUserManager.UserLoadListener,
        GoogleApiClient.ConnectionCallbacks {

    @SuppressWarnings("unused")
    private static final String TAG = LoginActivity.class.getSimpleName();
    private static final int RC_SIGN_IN = 100;
    private static final int RC_CREATE_ADF = 129;
    private static final int RC_CAMERA = 102;
    private static final int RC_EXPLAIN_CAMERA = 111;
    /**
     * Tango
     */
    private GoogleApiClient mGoogleApiClient;
    private View mLoadingView;

    private boolean mFirst = true;
    private boolean mContinueToNextActivity = false;

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
        if (mFirst) {
            mFirst = false;
            silentSignInWithGoogle();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            GoogleSignInAccount account = result.getSignInAccount();
            if (result.isSuccess() && account != null) {
                firebaseAuthWithGoogle(account);
            } else {
                signInWithGoogle();
            }
        }
    }


    @Override
    protected void onPermissionsGranted(int permissionCode) {
        switch (permissionCode) {
            case RC_CREATE_ADF:
                createAdfManager();
                break;
            case RC_CAMERA:
                continueToNextActivity();
                break;
        }
    }

    private void silentSignInWithGoogle() {
        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        if (opr.isDone()) {
            // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
            // and the GoogleSignInResult will be available instantly.
            GoogleSignInResult result = opr.get();
            if (result.getSignInAccount() != null) {
                firebaseAuthWithGoogle(result.getSignInAccount());
            } else {
                // Clean cache from firebase.
                DataControllerFactory.getUserManagerInstance().signOut();
            }
        } else {
            DataControllerFactory.getUserManagerInstance().signOut();
            signInWithGoogle();
        }
    }

    private void signInWithGoogle() {
        Auth.GoogleSignInApi.signOut(getGoogleApiClient());
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(getGoogleApiClient());
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    /**
     * At first it tries to silently sign in, if not it calls google sign in window
     */
    private void trySignIn() {
        User user = DataControllerFactory.getUserManagerInstance().getCurrentUser();
        if (user != null) {
            // already signed in
            saveUserInContext(user);
        } else {
            signInWithGoogle();
        }
    }

    private void firebaseAuthWithGoogle(@NonNull GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mLoadingView.setVisibility(View.VISIBLE);
        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        //noinspection ThrowableResultOfMethodCallIgnored
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
        signInWithGoogle();
    }

    private void saveUserInContext(User user) {
        mLoadingView.setVisibility(View.VISIBLE);
        App.getInstance().getSocialUserManager().loadLoggedInUser(user, getGoogleApiClient(), this);
    }

    private void createAdfManager() {
        mLoadingView.setVisibility(View.VISIBLE);

        if (!Utils.checkHasLocationPermission(this)) {
            requestPermissions(RC_CREATE_ADF);
            return;
        }
        Utils.getNewLocation(mGoogleApiClient, new Utils.Callback<SerializableLatLng>() {
            @Override
            public void onResult(SerializableLatLng result) {
                AdfService as = App.getInstance().getAdfService();
                AdfManager.createWithLocation(result, as, new Utils.Callback<AdfManager>() {
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

    @Override
    protected void onAdfPermissionsGranted() {
        super.onAdfPermissionsGranted();
        continueToNextActivity();
    }

    private void continueToNextActivity() {
        if (!Utils.checkHasCameraPermission(this) && !Utils.checkHasLocationPermission(this)) {
            requestPermissions(RC_CAMERA);
            return;
        }
        if (!Utils.hasADFPermissions(this)) {
            Log.i(TAG, "Request adf permission");
            requestADFPermission();
            return;
        }
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
                .enableAutoManage(this, null)
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
        outState.putBoolean("mFirst", mFirst);
        outState.putBoolean("mContinueToNextActivity", mContinueToNextActivity);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mFirst = savedInstanceState.getBoolean("mFirst", false);
        mContinueToNextActivity = savedInstanceState.getBoolean("mContinueToNextActivity", false);
    }
}