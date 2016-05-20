package com.wally.wally;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.plus.Plus;
import com.wally.wally.datacontroller.DataController;
import com.wally.wally.datacontroller.callbacks.Callback;
import com.wally.wally.datacontroller.user.User;
import com.wally.wally.userManager.SocialUser;
import com.wally.wally.userManager.SocialUserFactory;

/**
 * Class that manages login flow
 * <p/>
 * Created by ioane5 on 5/10/16.
 */
public class LoginManager{
    public static final int AUTH_TYPE_GOOGLE = 0;
    public static final int AUTH_TYPE_GUEST = 1;
    private static final String TAG = LoginManager.class.getSimpleName();
    private static final int REQUEST_CODE_SIGN_IN = 129;

    private GoogleApiClient mGoogleApiClient;
    private LoginListener mLoginListener;
    private AuthListener mAuthListener;
    private SharedPreferences mPreferences;



    public LoginManager(GoogleApiClient api, SharedPreferences preferences){
        mGoogleApiClient = api;
        mPreferences = preferences;
    }


    public void setAuthListener(AuthListener authListener) {
        mAuthListener = authListener;
    }

    public void setLoginListener(LoginListener loginListener) {
        mLoginListener = loginListener;
    }

    public void setUpGoogleButton(SignInButton signInButton) {
        signInButton.setSize(SignInButton.SIZE_WIDE);
        signInButton.setScopes(new Scope[]{new Scope(Scopes.PLUS_LOGIN)});
    }

    /**
     * Try to authenticate silently without showing on UI.
     * If user has authenticated even once, we must be able to silently sign in.
     */
    public void trySilentAuth() {
        OptionalPendingResult<GoogleSignInResult> pendingResult =
                Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        if (pendingResult.isDone()) {
            // There's immediate result available.
            handleSignInResult(pendingResult.get());
        } else {
            // There's no immediate result ready, displays some progress indicator and waits for the
            // async callback.
            pendingResult.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(@NonNull GoogleSignInResult result) {
                    handleSignInResult(result);
                }
            });
        }
    }

    public void onGoogleSignInClicked(Activity activity) {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        activity.startActivityForResult(signInIntent, REQUEST_CODE_SIGN_IN);
    }

    @SuppressWarnings("UnusedParameters")
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
            return true;
        }
        return false;
    }

    @SuppressWarnings("ConstantConditions")
    private void handleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            String token = result.getSignInAccount().getIdToken();
            saveToken(token);
            mAuthListener.onAuth(!TextUtils.isEmpty(token));
        }
    }

    public void tryLogin() {
        String token = getToken();
        if (TextUtils.isEmpty(token)) {
            throw new IllegalStateException("You can call tryLogin only when token is present!");
        }
        DataController dc = App.getInstance().getDataController();
        // TODO get and update object in application
        dc.googleAuth(getToken(), new Callback<User>() {

            @Override
            public void onResult(User result) {
                SocialUserFactory sf = App.getInstance().getSocialUserFactory();
                sf.getSocialUser(result, mGoogleApiClient,
                        new SocialUserFactory.UserLoadListener() {
                            @Override
                            public void onUserLoad(SocialUser user) {
                                mLoginListener.onLogin(user);
                            }
                        });
            }

            @Override
            public void onError(Exception e) {
                mLoginListener.onLogin(null);
            }
        });
    }

    private String getToken() {
        return mPreferences.getString("AUTH_TOKEN", null);
    }

    private void saveToken(String token) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString("AUTH_TOKEN", token);
        editor.putInt("AUTH_TYPE", AUTH_TYPE_GOOGLE);
        editor.apply();
    }

    /**
     * Called when user wants to continue as guest.
     */
    public void guestSignIn() {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt("AUTH_TYPE", AUTH_TYPE_GUEST);
        editor.apply();
        mAuthListener.onAuth(true);
    }

    public boolean isUserAGuest() {
        return mPreferences.getInt("AUTH_TYPE", -1) == AUTH_TYPE_GUEST;
    }

    public boolean isLoggedIn() {
        return App.getInstance().getUser() != null || isUserAGuest();
    }

    /**
     * Interface callbacks for authentication listening.
     */
    public interface AuthListener {
        void onAuth(boolean isSuccess);
    }

    public interface LoginListener {
        void onLogin(SocialUser user);
    }
}
