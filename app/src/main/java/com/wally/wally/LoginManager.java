package com.wally.wally;

import android.accounts.Account;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.wally.wally.datacontroller.Callback;
import com.wally.wally.datacontroller.DataController;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


/**
 * Class that manages login flow
 * <p/>
 * Created by ioane5 on 5/10/16.
 */
public class LoginManager implements GoogleApiClient.OnConnectionFailedListener {

    public static final int AUTH_TYPE_GOOGLE = 0;
    public static final int AUTH_TYPE_GUEST = 1;
    @SuppressWarnings("unused")
    private static final String TAG = LoginManager.class.getSimpleName();
    private static final int REQUEST_CODE_SIGN_IN = 129;
    private FragmentActivity mContext;
    private GoogleApiClient mGoogleApiClient;
    private LoginListener mLoginListener;
    private AuthListener mAuthListener;

    /**
     * We need fragmentActivity because it gives us more flexibility using GoogleClientApi
     *
     * @param context to access GPlus services
     */
    public LoginManager(FragmentActivity context) {
        mContext = context;

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestId()
                .requestProfile()
                .requestScopes(new Scope(Scopes.PLUS_LOGIN))
                .build();

        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                .enableAutoManage(mContext /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
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

    public void onGoogleSignInClicked() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        mContext.startActivityForResult(signInIntent, REQUEST_CODE_SIGN_IN);
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
            // Signed in successfully, show authenticated UI.
            final GoogleSignInAccount acct = result.getSignInAccount();
            // To get token we need network connection. Thats is why we need to use asyncTask.
            new AsyncTask<Void, Void, String>() {
                @Override
                protected String doInBackground(Void... params) {
                    try {
                        Account account = new Account(acct.getEmail(), "com.google");
                        return GoogleAuthUtil.getToken(mContext, account, "oauth2:profile email");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(String token) {
                    super.onPostExecute(token);
                    saveToken(token);
                    Log.d(TAG, "onPostExecute() called with: " + "token = [" + token + "]");
                    mAuthListener.onAuth(true);
                }
            }.execute();
        } else {
            saveToken(null);
            mAuthListener.onAuth(false);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public void tryLogin() {
        String token = getToken();
        if (TextUtils.isEmpty(token)) {
            throw new IllegalStateException("You can call tryLogin only when token is present!");
        }
        DataController dc = App.getInstance().getDataController();
        // TODO get and update object in application
        dc.googleAuth(getToken(), new Callback<Boolean>() {
            @Override
            public void call(Boolean result, Exception e) {
                if (e == null) {
                    mLoginListener.onLogin(result.toString());
                } else {
                    mLoginListener.onLogin(null);
                }
            }
        });
    }

    private String getToken() {
        return PreferenceManager.getDefaultSharedPreferences(mContext).getString("AUTH_TOKEN", null);
    }

    private void saveToken(String token) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(mContext).edit();
        editor.putString("AUTH_TOKEN", token);
        editor.putInt("AUTH_TYPE", AUTH_TYPE_GOOGLE);
        editor.apply();
    }

    /**
     * Called when user wants to continue as guest.
     */
    public void guestSignIn() {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(mContext).edit();
        editor.putInt("AUTH_TYPE", AUTH_TYPE_GUEST);
        editor.apply();
        mAuthListener.onAuth(true);
    }

    public boolean isUserAGuest() {
        return PreferenceManager
                .getDefaultSharedPreferences(mContext).getInt("AUTH_TYPE", -1) == AUTH_TYPE_GUEST;
    }

    public boolean isLoggedIn() {
        return App.getInstance().getUserInfo() != null || isUserAGuest();
    }

    /**
     * We have several types of authentication.
     * Google Plus auth, and guest mode.
     * <p/>
     * Later we may add Facebook auth support.
     */
    @IntDef({AUTH_TYPE_GOOGLE, AUTH_TYPE_GUEST})
    @Retention(RetentionPolicy.SOURCE)

    public @interface AuthType {
    }

    /**
     * Interface callbacks for authentication listening.
     */
    public interface AuthListener {
        void onAuth(boolean isSuccess);
    }

    public interface LoginListener {
        void onLogin(String userName);
    }
}
