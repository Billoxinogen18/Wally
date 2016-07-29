package com.wally.wally.analytics;

import android.content.Context;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;

public class WallyAnalytics {
    public static final String TAG = WallyAnalytics.class.getSimpleName();
    private static WallyAnalytics instance;
    private final FirebaseAnalytics analytics;

    public WallyAnalytics(FirebaseAnalytics analytics) {
        this.analytics = analytics;

    }

    public void onAdfCreate() {
        Bundle bundle = new Bundle();
        bundle.putInt("Count", 1);
        analytics.logEvent("ADF_created", bundle);
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

    public void onLocalizeOnDownloadedAdf(boolean status) {
        Bundle bundle = new Bundle();
        bundle.putInt("Count", 1);
        if (status) {
            analytics.logEvent("ADF_dnl_localize_success", bundle);
        } else {
            analytics.logEvent("ADF_dnl_localize_fail", bundle);
        }
    }

    public void onLocalizeAfterExport(boolean status) {
        Bundle bundle = new Bundle();
        bundle.putInt("Count", 1);
        if (status) {
            analytics.logEvent("ADF_exp_localize_success", bundle);
        } else {
            analytics.logEvent("ADF_exp_localize_fail", bundle);
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

    public static WallyAnalytics getInstance(Context context) {
        if (instance == null) {
            FirebaseAnalytics analytics = FirebaseAnalytics.getInstance(context);
            instance = new WallyAnalytics(analytics);
        }
        return instance;
    }
}
