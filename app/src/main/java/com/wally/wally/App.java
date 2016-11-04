package com.wally.wally;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.multidex.MultiDex;
import android.util.Log;

import com.wally.wally.adf.AdfManager;
import com.wally.wally.adf.AdfService;
import com.wally.wally.datacontroller.DBController;
import com.wally.wally.datacontroller.DataControllerFactory;
import com.wally.wally.objects.content.Content;
import com.wally.wally.userManager.SocialUserFactory;
import com.wally.wally.userManager.SocialUserManager;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Application class for Application wide feature initializations.
 */
public class App extends Application {
    private static final String TAG = App.class.getSimpleName();

    private static final List<Long> sPuzzlePenalties = Arrays.asList(
            60000L,     // 1 min
            300000L,    // 5 min
            900000L,    // 15 min
            3600000L,   // 1 hour
            7200000L);  // 2 hour

    private static App sInstance = null;

    private SocialUserManager socialUserManager;
    private AdfManager adfManager;

    public static App getInstance() {
        return sInstance;
    }

    public static Context getContext() {
        return sInstance.getBaseContext();
    }

    @Override
    public void onCreate() {
        MultiDex.install(getApplicationContext());
        super.onCreate();
        socialUserManager = new SocialUserManager(new SocialUserFactory());
        sInstance = this;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }

    public DBController getDataController() {
        return DataControllerFactory.getDbController();
    }

    public SocialUserManager getSocialUserManager() {
        return socialUserManager;
    }

    public void setAdfManager(AdfManager adfManager) {
        this.adfManager = adfManager;
    }

    public AdfManager getAdfManager() {
        return adfManager;
    }

    public AdfService getAdfService() {
        return DataControllerFactory.getAdfServiceInstance();
    }

    /**
     * This method helps to find penalty time for next puzzle answer request.
     *
     * @param content Puzzle content to find penalty for.
     * @return Returns when penalty will expire or null if there is no penalty(Or already expired)
     */
    public Date penaltyForPuzzle(Content content) {
        if (!content.isPuzzle()) {
            throw new IllegalArgumentException("Content must be a puzzle");
        }

        SharedPreferences prefs = getSharedPreferences("Puzzle", MODE_PRIVATE);
        String data = prefs.getString(content.getId(), null);
        if (data == null) {
            return null;
        }
        try {
            String[] dataParts = data.split("_");
            int tryCount = Integer.parseInt(dataParts[0]);
            tryCount = Math.min(sPuzzlePenalties.size() - 1, tryCount);
            long penaltyTime = sPuzzlePenalties.get(tryCount);
            long lastTry = Long.parseLong(dataParts[1]);

            long penaltyUntil = lastTry + penaltyTime;
            // If penalty already expired
            if (penaltyUntil <= System.currentTimeMillis()) {
                return null;
            }
            return new Date(penaltyUntil);
        } catch (Exception e) {
            Log.e(TAG, "penaltyForPuzzle: ", e);
            // If some error, maybe invalid formatting, so clear the prefs
            prefs.edit().clear().apply();
        }
        return null;
    }

    /**
     * Saves that user tried to answer the puzzle
     *
     * @param content puzzle to answer
     */
    @SuppressLint("DefaultLocale")
    public void incorrectPenaltyTrial(Content content) {
        SharedPreferences prefs = getSharedPreferences("Puzzle", MODE_PRIVATE);
        String data = prefs.getString(content.getId(), null);
        int tryCount = 0;
        if (data != null) {
            String[] dataParts = data.split("_");
            int OldTryCount = Integer.parseInt(dataParts[0]);
            tryCount += OldTryCount + 1;
        }
        tryCount = Math.min(sPuzzlePenalties.size() - 1, tryCount);
        long tryDate = System.currentTimeMillis();
        prefs.edit()
                .putString(content.getId(), String.format("%d_%d", tryCount, tryDate))
                .apply();
    }
}
