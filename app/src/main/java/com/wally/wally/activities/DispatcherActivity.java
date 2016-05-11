package com.wally.wally.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.wally.wally.LoginManager;

/**
 * This activity makes decision which activity to start.
 * <br/>
 * Thus it has no UI and finishes it's job very fast.
 * <br/>
 * If user was previously authenticated we start {@link MainActivity}. <br/>
 * Else it starts {@link AuthActivity}.
 */
public class DispatcherActivity extends AppCompatActivity implements LoginManager.AuthListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LoginManager manager = new LoginManager(this);
        manager.setAuthListener(this);
        manager.trySilentAuth();
    }

    @Override
    public void onAuth(boolean isSuccess) {
        Intent intent;
        if (isSuccess) {
            intent = new Intent(this, ADFChooser.class);
        } else {
            intent = new Intent(this, AuthActivity.class);
        }
        startActivity(intent);
        finishActivity(0);
    }
}
