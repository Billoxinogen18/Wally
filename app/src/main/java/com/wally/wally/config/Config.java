package com.wally.wally.config;


import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.util.HashMap;
import java.util.Map;

public class Config {
    public static final String TAG = "WallyConfig";
    private FirebaseRemoteConfig config;

    public Config() {
        config = FirebaseRemoteConfig.getInstance();
        setSettings();
        setDefaultParams();
        // http://stackoverflow.com/a/37376342
        // Shitty walk around for the "silent completion" problem
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                fetchServerParams();
            }
        });
    }

    private void setSettings() {
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(true)
                .build();
        config.setConfigSettings(configSettings);
    }

    private void setDefaultParams() {
        Map<String, Object> params = new HashMap<>();
        config.setDefaults(params);
    }

    private void fetchServerParams() {
        Log.d(TAG, "Attach listener");
        config.fetch().addOnCompleteListener(
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Log.d(TAG, "fetch success");
                            config.activateFetched();
                        } else {
                            Log.d(TAG, "fetch failed");
                        }
                    }
                }
        );
    }

    public String getString(String key) {
        return config.getString(key);
    }

    private static Config instance;
    public static Config getInstance() {
        if (instance == null) {
            instance = new Config();
        }
        return instance;
    }
}
