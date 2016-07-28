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

public class Config implements LEConstants {
    public static final String TAG = "WallyConfig";
    private FirebaseRemoteConfig config;

    public Config() {
        config = FirebaseRemoteConfig.getInstance();
        setSettings();
        registerDefaultParams();
        // http://stackoverflow.com/a/37376342
        // Shitty walk around for the "silent completion" problem
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                fetchServerParams();
            }
        }, 1000);
    }

    private void setSettings() {
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(true)
                .build();
        config.setConfigSettings(configSettings);
    }

    private void registerDefaultParams() {
        Map<String, Object> params = new HashMap<>();

        // LearningEvaluator constants
        params.put(MAX_TIME_S, 25);
        params.put(MIN_TIME_S, 20);
        params.put(MIN_CELL_COUNT, 4);
        params.put(MIN_ANGLE_COUNT, 10);
        params.put(ANGLE_RESOLUTION, 8);
        config.setDefaults(params);
    }

    public void fetchServerParams() {
        Log.d(TAG, "Attach listener");
        config.fetch(0).addOnCompleteListener(
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Log.d(TAG, "fetch success");
                            Log.d(TAG, "activate " + config.activateFetched());
                            Log.d(TAG, MIN_TIME_S + getString(MIN_TIME_S));
                            Log.d(TAG, MAX_TIME_S + getString(MAX_TIME_S));
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

    public int getInt(String key) {
        return (int) config.getLong(key);
    }

    private static Config instance;
    public static Config getInstance() {
        if (instance == null) {
            instance = new Config();
        }
        return instance;
    }
}
