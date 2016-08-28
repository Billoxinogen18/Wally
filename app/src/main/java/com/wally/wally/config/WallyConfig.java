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

public class WallyConfig implements Config, LearningEvaluatorConstants, TangoManagerConstants, CameraTangoActivityConstants {
    private static final String TAG = "WallyConfig";
    private static Config instance;
    private FirebaseRemoteConfig config;

    private WallyConfig() {
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

    public static Config getInstance() {
        if (instance == null) {
            instance = new WallyConfig();
        }
        return instance;
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

        // timeouts
        params.put(LOCALIZATION_TIMEOUT, 15000);

        // UX constants
        params.put(LOCALIZED, "Yay!");
        params.put(NEW_ROOM_LEARNED, "New room learned");
        params.put(LEARNING_AREA, "Learning new area, Walk around");
        params.put(LOCALIZING_IN_NEW_AREA, "Verifying new area, Walk around");
        params.put(LOCALIZING_IN_KNOWN_AREA, "Identifying area, Walk around");
        params.put(LOCALIZATION_LOST_IN_LEARNING, "I'm lost. Restarting learning...");
        params.put(LOCALIZATION_LOST, "I'm lost. Walk Around");

        params.put(ADF_EXPORT_EXPLAIN_MSG, "If you don't give permission, you won't be able to see created content again, Would you like to give permission?");
        params.put(ADF_EXPORT_EXPLAIN_PST_BTN, "Give Permission");
        params.put(ADF_EXPORT_EXPLAIN_NGT_BTN, "Deny");
        config.setDefaults(params);
    }

    private void fetchServerParams() {
        Log.d(TAG, "Attach listener");
        config.fetch(0).addOnCompleteListener(
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "fetch success");
                            Log.d(TAG, "activate " + config.activateFetched());
                            Log.d(TAG, MIN_TIME_S + ": " + getString(MIN_TIME_S));
                            Log.d(TAG, LOCALIZED + ": " + getString(LOCALIZED));
                        } else {
                            Log.d(TAG, "fetch failed");
                        }
                    }
                }
        );
    }

    @Override
    public String getString(String key) {
        return config.getString(key);
    }

    @Override
    public int getInt(String key) {
        return (int) config.getLong(key);
    }
}
