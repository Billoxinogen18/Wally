package com.wally.wally;

import android.accounts.Account;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;

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


/**
 * Class that manages login flow
 * <p/>
 * Created by ioane5 on 5/10/16.
 */
public class LoginManager implements GoogleApiClient.OnConnectionFailedListener {

    @SuppressWarnings("unused")
    private static final String TAG = LoginManager.class.getSimpleName();
    private static final int REQUEST_CODE_SIGN_IN = 129;

    private ProgressDialog mProgressDialog;
    private FragmentActivity mContext;
    private GoogleApiClient mGoogleApiClient;
    private LoginListener mLoginListener;

    /**
     * We need fragmentActivity because it gives us more flexibility using GoogleClientApi
     *
     * @param context       to access GPlus services
     * @param loginListener listener gets callbacks from login flow
     */
    public LoginManager(FragmentActivity context, LoginListener loginListener) {
        mContext = context;
        mLoginListener = loginListener;

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
        trySilentLogin();
    }

    public void setUpGoogleButton(SignInButton signInButton) {
        signInButton.setSize(SignInButton.SIZE_WIDE);
        signInButton.setScopes(new Scope[]{new Scope(Scopes.PLUS_LOGIN)});
    }

    /**
     * Try to login silently without showing on UI.
     * If user has logged in once, we must be able to silently sign in.
     */
    private void trySilentLogin() {
        OptionalPendingResult<GoogleSignInResult> pendingResult =
                Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        if (pendingResult.isDone()) {
            // There's immediate result available.
            handleSignInResult(pendingResult.get());
        } else {
            // There's no immediate result ready, displays some progress indicator and waits for the
            // async callback.
            showProgressDialog();
            pendingResult.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(@NonNull GoogleSignInResult result) {
                    handleSignInResult(result);
                    hideProgressDialog();
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

                    DataController dc = ((App) mContext.getApplicationContext()).getDataController();
                    dc.googleAuth(token, new Callback<Boolean>() {
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
            }.execute();
        } else {
            mLoginListener.onLogin(null);
        }
    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(mContext);
            mProgressDialog.setMessage(mContext.getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public interface LoginListener {

        // TODO maybe add account here.

        /**
         * User was already logged in
         */
        void onLogin(String userName);

    }
}
