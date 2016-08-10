package com.wally.wally.tango.states;

import android.util.Log;

import com.projecttango.tangosupport.TangoPointCloudManager;
import com.wally.wally.config.Config;
import com.wally.wally.renderer.WallyRenderer;
import com.wally.wally.tango.EventListener;
import com.wally.wally.tango.LocalizationAnalytics;
import com.wally.wally.tango.TangoFactory;
import com.wally.wally.tango.TangoUpdater;

import java.util.Map;

/**
 * Created by shota on 8/9/16.
 * Manages Tango for learned Adf
 */
public class TangoForLearnedAdf extends TangoForAdf {
    private static final String TAG = TangoForLearnedAdf.class.getSimpleName();


    public TangoForLearnedAdf(Config config,
                              TangoUpdater tangoUpdater,
                              TangoFactory tangoFactory,
                              WallyRenderer wallyRenderer,
                              LocalizationAnalytics analytics,
                              Map<Class, TangoBase> tangoStatePool,
                              TangoPointCloudManager pointCloudManager) {

        super(config, tangoUpdater, tangoFactory, wallyRenderer, analytics, tangoStatePool, pointCloudManager);
    }

    @Override
    public synchronized void resume() {
        Log.d(TAG, "Localize With Learned adf = [" + mAdfInfo + "]");
        mLocalizationAnalytics.startAdfLocalizationStopWatch();
        mTango = mTangoFactory.getTangoWithUuid(getTangoInitializer(), mAdfInfo.getUuid());
        mLocalizationAnalytics.setLocalizationState(LocalizationAnalytics.LocalizationState.AFTER_LEARNING);
        startLocalizationWatchDog();
        fireLocalizationStartAfterLearning();
    }

    @Override
    protected void fireLocalizationFinish() {
        fireLocalizationFinishAfterLearning();
    }

    private void fireLocalizationStartAfterLearning() {
        for (EventListener listener : mEventListeners) {
            listener.onLocalizationStartAfterLearning();
        }
    }

    private void fireLocalizationFinishAfterLearning() {
        for (EventListener listener : mEventListeners) {
            listener.onLocalizationFinishAfterLearning();
        }
    }
}
