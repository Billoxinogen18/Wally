package com.wally.wally.analytics;

import android.content.Context;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.wally.wally.events.WallyEvent;
import com.wally.wally.events.WallyEventListener;

public class WallyAnalytics implements WallyEventListener {
    public static final String TAG = WallyAnalytics.class.getSimpleName();
    private static WallyAnalytics instance;
    private final FirebaseAnalytics analytics;

    public WallyAnalytics(FirebaseAnalytics analytics) {
        this.analytics = analytics;
    }

    public void onButtonClick(String buttonId) {
        Bundle bundle = getDefaultBundle();
        bundle.putString("Button_Id", buttonId);
        analytics.logEvent("Button_Click", bundle);
    }

    @SuppressWarnings("unused")
    public void onContentCreate() {
        analytics.logEvent("Content_Create", getDefaultBundle());
    }

    public void onContentDelete() {
        analytics.logEvent("Content_Delete", getDefaultBundle());
    }

    @SuppressWarnings("unused")
    public void onContentEdit() {
        analytics.logEvent("Content_Edit", getDefaultBundle());
    }

    @SuppressWarnings("unused")
    public void onContentSelect() {
        analytics.logEvent("Content_Select", getDefaultBundle());
    }

    private Bundle getDefaultBundle() {
        Bundle bundle = new Bundle();
        bundle.putInt("Count", 1);
        return  bundle;
    }

    @Override
    public void onWallyEvent(WallyEvent event) {
        analytics.logEvent(event.getId(), getDefaultBundle());
    }

    public static WallyAnalytics getInstance(Context context) {
//        if (instance == null) {
//            FirebaseAnalytics analytics = FirebaseAnalytics.getInstance(context);
//            instance = new WallyAnalytics(analytics);
//        }
        return instance;
    }
}
