package com.wally.wally;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.wally.wally.activities.LoginActivity;

/**
 * Created by shota on 5/20/16.
 */
public class LoginManagerFactory {

    public static LoginManager getLoginManager(LoginActivity loginActivity){
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(Plus.SCOPE_PLUS_LOGIN, Plus.SCOPE_PLUS_PROFILE)
                .requestIdToken(loginActivity.getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(loginActivity)
                .enableAutoManage(loginActivity /* FragmentActivity */, loginActivity /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .addApi(Plus.API)
                .build();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(loginActivity);
        return new LoginManager(mGoogleApiClient, preferences);
    }

}
