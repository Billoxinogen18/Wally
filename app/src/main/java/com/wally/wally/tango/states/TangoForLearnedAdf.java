package com.wally.wally.tango.states;

import android.util.Log;

import com.projecttango.tangosupport.TangoPointCloudManager;
import com.wally.wally.events.WallyEvent;
import com.wally.wally.renderer.WallyRenderer;
import com.wally.wally.tango.TangoFactory;
import com.wally.wally.tango.TangoUpdater;

import java.util.Map;

/**
 * Created by shota on 8/9/16.
 * Manages Tango for learned Adf
 */
public class TangoForLearnedAdf extends TangoForAdf {
    private static final String TAG = TangoForLearnedAdf.class.getSimpleName();


    public TangoForLearnedAdf(Executor executor,
                              TangoUpdater tangoUpdater,
                              TangoFactory tangoFactory,
                              WallyRenderer wallyRenderer,
                              Map<Class, TangoState> tangoStatePool,
                              TangoPointCloudManager pointCloudManager) {

        super(executor, tangoUpdater, tangoFactory, wallyRenderer, tangoStatePool, pointCloudManager);
    }

    @Override
    protected void pauseHook() {
        Log.d(TAG, "pause Thread = " + Thread.currentThread());
    }

    @Override
    protected void resumeHook() {
        Log.d(TAG, "Localize With Learned adf = [" + mAdfInfo + "]. Thread = " + Thread.currentThread());
        mTango = mTangoFactory.getTangoWithUuid(getTangoInitializer(), mAdfInfo.getUuid());
        Log.d(TAG, "resume() mTango = " + mTango + " isTangoConnected " + isConnected());
        startLocalizationWatchDog();
        fireLocalizationStartAfterLearning();
    }

    @Override
    protected void fireLocalizationFinish() {
        fireLocalizationFinishAfterLearning();
    }

    private void fireLocalizationStartAfterLearning() {
        fireEvent(WallyEvent.createEventWithId(WallyEvent.LOCALIZATION_START_AFTER_LEARNING));
    }

    private void fireLocalizationFinishAfterLearning() {
        fireEvent(WallyEvent.createEventWithId(WallyEvent.LOCALIZATION_FINISH_AFTER_LEARNING));
    }
}
