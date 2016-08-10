package com.wally.wally.tango.states;

import android.util.Log;

import com.google.atap.tangoservice.Tango;
import com.projecttango.tangosupport.TangoPointCloudManager;
import com.wally.wally.adf.AdfInfo;
import com.wally.wally.config.Config;
import com.wally.wally.renderer.WallyRenderer;
import com.wally.wally.tango.EventListener;
import com.wally.wally.tango.LocalizationAnalytics;
import com.wally.wally.tango.TangoFactory;
import com.wally.wally.tango.TangoUpdater;
import com.wally.wally.tango.TangoUtils;

import java.util.Map;

/**
 * Created by shota on 8/10/16.
 * Manages Tango after resume with previously saved Adf
 */
public class TangoForSavedAdf extends TangoForAdf {
    private static final String TAG = TangoForSavedAdf.class.getSimpleName();

    public TangoForSavedAdf(Config config,
                            TangoUpdater tangoUpdater,
                            TangoFactory tangoFactory,
                            WallyRenderer wallyRenderer,
                            LocalizationAnalytics analytics,
                            Map<Class, TangoBase> tangoStatePool,
                            TangoPointCloudManager pointCloudManager){
        super(config, tangoUpdater, tangoFactory, wallyRenderer, analytics, tangoStatePool, pointCloudManager);
    }

    public TangoForAdf withAdf(AdfInfo adf){
        mAdfInfo = adf;
        return this;
    }

    @Override
    public void resume() {
        startLocalizing();
        mLocalizationAnalytics.setLocalizationState(LocalizationAnalytics.LocalizationState.AFTER_ON_RESUME);
        startLocalizationWatchDog();
    }

    protected void startLocalizing(){
        Log.d(TAG, "startLocalizing with: adf = [" + mAdfInfo + "]");
        mLocalizationAnalytics.startAdfLocalizationStopWatch();
        final TangoFactory.RunnableWithError r = getTangoInitializer();
        mTango = mTangoFactory.getTango(new TangoFactory.RunnableWithError() {
            @Override
            public void run() {
                r.run();
                loadAdf(mTango, mAdfInfo);
            }

            @Override
            public void onError(Exception e) {
                r.onError(e);
            }
        });
        fireLocalizationStart();
    }

    private void loadAdf(Tango tango, AdfInfo adf) {
        if (TangoUtils.isAdfImported(tango, adf.getUuid())) {
            tango.experimentalLoadAreaDescription(adf.getUuid());
        } else {
            tango.experimentalLoadAreaDescriptionFromFile(adf.getPath());
        }
    }

    private void fireLocalizationStart() {
        for (EventListener listener : mEventListeners) {
            listener.onLocalizationStart();
        }

    }

    @Override
    protected void fireLocalizationFinish() {
        fireLocalizationFinishAfterSavedAdf();
    }

    private void fireLocalizationFinishAfterSavedAdf() {
        for (EventListener listener : mEventListeners) {
            listener.onLocalizationFinishAfterSavedAdf();
        }
    }
}