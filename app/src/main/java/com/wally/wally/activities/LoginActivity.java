package com.wally.wally.activities;

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
import com.google.android.gms.common.api.Scope;
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
import com.wally.wally.datacontroller.user.User;
import com.wally.wally.userManager.SocialUser;
import com.wally.wally.userManager.UserManager;

public class LoginActivity extends GoogleApiClientActivity implements
        UserManager.UserLoadListener,
        GoogleApiClient.ConnectionCallbacks {

    @SuppressWarnings("unused")
    private static final String TAG = LoginActivity.class.getSimpleName();
    private static final int RC_SIGN_IN = 100;
    /**
     * Tango
     */
    private GoogleApiClient mGoogleApiClient;
    private View mLoadingView;

    private boolean mFlagSignIn = true;

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
        continueToNextActivity();
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
                .build();
        return mGoogleApiClient;
    }
}