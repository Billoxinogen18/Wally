package com.wally.wally.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.common.SignInButton;
import com.wally.wally.LoginManager;
import com.wally.wally.R;

/**
 * This activity shows user an authentication UI.
 * <br/>
 * User can auth using G+ or Continue as Anonymous Guest user.
 */
public class AuthActivity extends AppCompatActivity implements LoginManager.AuthListener {

    @SuppressWarnings("unused")
    private static final String TAG = AuthActivity.class.getSimpleName();
    private LoginManager mManager;

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // create before setting content view
        // (On Silent login, views won't be inflated and we will directly go to home screen)
        mManager = new LoginManager(this);
        mManager.setAuthListener(this);

        setContentView(R.layout.activity_login);
        SignInButton plusSignIn = (SignInButton) findViewById(R.id.btn_google_sign_in);
        mManager.setUpGoogleButton(plusSignIn);
        Button signInAsGuest = (Button) findViewById(R.id.btn_sign_in_as_guest);
        signInAsGuest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mManager.guestSignIn();
            }
        });
        plusSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mManager.onGoogleSignInClicked();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onAuth(boolean isSuccess) {
        Log.d(TAG, "onAuth() called with: " + "isSuccess = [" + isSuccess + "]");
        if (isSuccess) {
            Intent intent = new Intent(this, ADFChooser.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finishActivity(0);
        }
    }
}
