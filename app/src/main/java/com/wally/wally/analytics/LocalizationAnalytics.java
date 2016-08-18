package com.wally.wally.analytics;


/**
 * Created by shota on 8/9/16.
 * Class is responsible for logging Localization data in Analytics
 */
@SuppressWarnings("ALL")
@Deprecated
public class LocalizationAnalytics {
    private WallyAnalytics mAnalytics;
    private LocalizationState mLocalizationState = LocalizationState.NONE;
    private int mAdfCounter = 0;
    private long mTimeForAdfLocalization;


    public LocalizationAnalytics(WallyAnalytics analytics) {
//        mAnalytics = analytics;
    }


    public void resetAdfCounter() {
//        mAdfCounter = 0;
    }

    public void incrementAdfCounter() {
//        mAdfCounter++;
    }

    public void startAdfLocalizationStopWatch() {
//        mTimeForAdfLocalization = System.currentTimeMillis();
    }

    public void logLocalization(boolean success) {
//        if (mLocalizationState == LocalizationState.AFTER_LEARNING){
//            mAnalytics.onLocalizeOnNewAdf(success);
//            mLocalizationState = LocalizationState.NONE;
//        } else if (mLocalizationState == LocalizationState.AFTER_ON_RESUME){
//            mAnalytics.onLocalizeAfterResume(success);
//            mLocalizationState = LocalizationState.NONE;
//        } else if (mLocalizationState == LocalizationState.AFTER_DOWNLOAD){
//            mAnalytics.onLocalizeOnDownloadedAdf(success);
//            mLocalizationState = LocalizationState.NONE;
//            if (success){
//                mAnalytics.logAdfNumberBeforeLocalization(mAdfCounter);
//            }
//        }
//        if (success && mLocalizationState != LocalizationState.AFTER_LEARNING){
//            mAnalytics.logLocalizationTimeForAdf("NAN", System.currentTimeMillis() - mTimeForAdfLocalization);
//        }
    }

    public void setLocalizationState(LocalizationState state) {
        mLocalizationState = state;
    }

    public void logAdfNumberBeforeLearning() {
//        mAnalytics.logAdfNumberBeforeLearning(mAdfCounter);
    }

    public void onAdfCreate() {
//        mAnalytics.onAdfCreate();
    }

    public enum LocalizationState {
        NONE, AFTER_LEARNING, AFTER_ON_RESUME, AFTER_DOWNLOAD
    }
}
