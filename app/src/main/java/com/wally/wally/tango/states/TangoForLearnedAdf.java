package com.wally.wally.tango.states;

import android.util.Log;

import com.projecttango.tangosupport.TangoPointCloudManager;
import com.wally.wally.renderer.WallyRenderer;
import com.wally.wally.tango.EventListener;
import com.wally.wally.tango.TangoFactory;
import com.wally.wally.tango.TangoUpdater;

import java.util.Map;

/**
 * Created by shota on 8/9/16.
 * Manages Tango for learned Adf
 */
public class TangoForLearnedAdf extends TangoForAdf {
    private static final String TAG = TangoForLearnedAdf.class.getSimpleName();


    public TangoForLearnedAdf(TangoUpdater tangoUpdater,
                              TangoFactory tangoFactory,
                              WallyRenderer wallyRenderer,
                              Map<Class, TangoState> tangoStatePool,
                              TangoPointCloudManager pointCloudManager) {

        super(tangoUpdater, tangoFactory, wallyRenderer, tangoStatePool, pointCloudManager);
    }

    @Override
    public synchronized void pause() {
        Log.d(TAG, "pause Thread = " + Thread.currentThread());
        super.pause();
    }

    @Override
    public synchronized void resume() {
        Log.d(TAG, "Localize With Learned adf = [" + mAdfInfo + "]. Thread = " + Thread.currentThread());
        mTango = mTangoFactory.getTangoWithUuid(getTangoInitializer(), mAdfInfo.getUuid());
        Log.d(TAG, "resume() mTango = " + mTango);
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
