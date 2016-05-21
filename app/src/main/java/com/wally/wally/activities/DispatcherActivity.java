package com.wally.wally.activities;

import android.os.Bundle;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.wally.wally.App;
import com.wally.wally.datacontroller.user.User;
import com.wally.wally.userManager.SocialUser;
import com.wally.wally.userManager.UserManager;

/**
 * This activity makes decision which activity to start.
 * <br/>
 * Thus it has no UI and finishes it's job very fast.
 * <br/>
 * If user was previously authenticated we start {@link MainActivity}. <br/>
 * Else it starts {@link MainActivity}.
 */
public class DispatcherActivity extends LoginActivity implements UserManager.UserLoadListener {

//    private static final int RC_SIGN_IN = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        User user = App.getInstance().getDataController().getCurrentUser();

        if (user != null) {
            // already signed in
            saveUserInContext(user);
        } else {
            continueToNextActivity();
        }
    }

    private void continueToNextActivity(){
        startActivity(ADFChooser.newIntent(this));
        finish();
    }

    private void saveUserInContext(User user) {

     GoogleApiClient googleApiClient = new GoogleApiClient.Builder(this)
             .enableAutoManage(this, this).addApi(Plus.API)
             .addScope(Plus.SCOPE_PLUS_LOGIN)
             .addScope(Plus.SCOPE_PLUS_PROFILE)
             .build();
        App.getInstance().getUserManager().loadUser(user, googleApiClient, this);
    }

    @Override
    public void onUserLoad(SocialUser user) {
        continueToNextActivity();
    }
}
