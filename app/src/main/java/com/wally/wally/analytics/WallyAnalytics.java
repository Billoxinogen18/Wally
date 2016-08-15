package com.wally.wally.analytics;

import android.content.Context;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.wally.wally.events.EventListener;

public class WallyAnalytics implements EventListener {
    public static final String TAG = WallyAnalytics.class.getSimpleName();
    private static WallyAnalytics instance;
    private final FirebaseAnalytics analytics;

    public WallyAnalytics(FirebaseAnalytics analytics) {
        this.analytics = analytics;

    }

    public void onLocalizeOnDownloadedAdf(boolean status) {
        Bundle bundle = new Bundle();
        bundle.putInt("Count", 1);
        if (status) {
            analytics.logEvent("ADF_dnl_localize_success", bundle);
        } else {
            analytics.logEvent("ADF_dnl_localize_fail", bundle);
        }
    }

    public void onLocalizeAfterResume(boolean status) {
        Bundle bundle = new Bundle();
        bundle.putInt("Count", 1);
        if (status) {
            analytics.logEvent("ADF_resume_localize_success", bundle);
        } else {
            analytics.logEvent("ADF_resume_localize_fail", bundle);
        }
    }

    public void logAdfNumberBeforeLocalization(int n) {
        Bundle bundle = new Bundle();
        bundle.putInt("Number", n);
        analytics.logEvent("ADF_count_before_localization", bundle);
    }

    public void logAdfNumberBeforeLearning(int n) {
        Bundle bundle = new Bundle();
        bundle.putInt("Number", n);
        analytics.logEvent("ADF_count_before_learning", bundle);
    }

    public void onButtonClick(String buttonId) {
        Bundle bundle = new Bundle();
        bundle.putInt("Count", 1);
        bundle.putString("Button_Id", buttonId);
        analytics.logEvent("Button_Click", bundle);
    }

    public void onContentCreate() {
        Bundle bundle = new Bundle();
        bundle.putInt("Count", 1);
        analytics.logEvent("Content_Create", bundle);
    }

    public void logContentType(String type) {
        Bundle bundle = new Bundle();
        bundle.putInt("Count", 1);
        bundle.putString("Type", type);
        analytics.logEvent("Content_Type", bundle);
    }

    public void onContentDelete() {
        Bundle bundle = new Bundle();
        bundle.putInt("Count", 1);
        analytics.logEvent("Content_Delete", bundle);
    }

    public void onContentEdit() {
        Bundle bundle = new Bundle();
        bundle.putInt("Count", 1);
        analytics.logEvent("Content_Edit", bundle);
    }

    public void onContentSelect() {
        Bundle bundle = new Bundle();
        bundle.putInt("Count", 1);
        analytics.logEvent("Content_Select", bundle);
    }

    public void logLocalizationTimeForAdf(String uuid, long timeMs) {
        Bundle bundle = new Bundle();
        bundle.putString("uuid", uuid);
        bundle.putLong("time", timeMs);
        analytics.logEvent("ADF_TIME_ON_LOCALIZATION", bundle);
    }

    public void onLocalizeOnNewAdf(boolean status) {
        Bundle bundle = new Bundle();
        bundle.putInt("Count", 1);
        if (status) {
            analytics.logEvent("ADF_new_localize_success", bundle);
        } else {
            analytics.logEvent("ADF_new_localize_fail", bundle);
        }
    }

    public static WallyAnalytics getInstance(Context context) {
        if (instance == null) {
            FirebaseAnalytics analytics = FirebaseAnalytics.getInstance(context);
            instance = new WallyAnalytics(analytics);
        }
        return instance;
    }

    @Override
    public void onTangoReady() {

    }

    @Override
    public void onLearningStart() {

    }

    @Override
    public void onTangoOutOfDate() {

    }

    @Override
    public void onLearningFinish() {
        analytics.logEvent("ADF_created", getDefaultBundle());
    }

    @Override
    public void onLocalizationStart() {

    }

    @Override
    public void onLocalizationStartAfterLearning() {

    }

    @Override
    public void onLocalizationFinishAfterLearning() {
        analytics.logEvent("ADF_new_localize_success", getDefaultBundle());
    }

    @Override
    public void onLocalizationFinishAfterSavedAdf() {

    }

    private Bundle getDefaultBundle() {
        Bundle bundle = new Bundle();
        bundle.putInt("Count", 1);
        return  bundle;
    }
}
