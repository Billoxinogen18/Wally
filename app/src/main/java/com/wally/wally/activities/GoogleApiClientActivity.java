package com.wally.wally.activities;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

/**
 * Created by shota on 5/20/16.
 */
//TODO rename!
public abstract class GoogleApiClientActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = GoogleApiClientActivity.class.getSimpleName();


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed() called with: " + "connectionResult = [" + connectionResult + "]");
        switch (connectionResult.getErrorCode()) {
            case ConnectionResult.SIGN_IN_FAILED:
            case ConnectionResult.SIGN_IN_REQUIRED:
                // cleear datacontroller.
                break;
        }
    }
}
