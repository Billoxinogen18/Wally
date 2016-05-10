package com.wally.wally.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.common.SignInButton;
import com.wally.wally.LoginManager;
import com.wally.wally.R;

public class LoginActivity extends AppCompatActivity implements LoginManager.LoginListener {

    private static final String TAG = LoginActivity.class.getSimpleName();

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // create before setting content view
        // (On Silent login, views won't be inflated and we will directly go to home screen)
        final LoginManager manager = new LoginManager(this, this);

        setContentView(R.layout.activity_login);
        SignInButton plusSignIn = (SignInButton) findViewById(R.id.btn_google_sign_in);
        manager.setUpGoogleButton(plusSignIn);
        Button signInAsGuest = (Button) findViewById(R.id.btn_sign_in_as_guest);

        signInAsGuest.setWidth(plusSignIn.getWidth());
        plusSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                manager.onGoogleSignInClicked();
            }
        });
    }

    /**
     * User was already logged in
     */
    @Override
    public void onLogin(String userName) {
        Log.d(TAG, "onLogin() called with: " + "userName = [" + userName + "]");
        if (!TextUtils.isEmpty(userName)) {
            // success
            Intent intent = new Intent(this, ADFChooser.class);
            startActivity(intent);
            finishActivity(0);
        } else {
            // login failed show something to user
        }
    }
}
