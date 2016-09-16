package com.wally.wally.tango.states;

import android.util.Log;

import com.projecttango.tangosupport.TangoPointCloudManager;
import com.wally.wally.adf.AdfInfo;
import com.wally.wally.events.WallyEvent;
import com.wally.wally.renderer.WallyRenderer;
import com.wally.wally.tango.TangoFactory;
import com.wally.wally.tango.TangoUpdater;
import com.wally.wally.tango.WatchDog;

/**
 * Created by shota on 8/10/16.
 * Manages Tango after resume with previously saved Adf
 */
public class TangoForSavedAdf extends TangoState {
    private static final String TAG = TangoForSavedAdf.class.getSimpleName();

    private AdfInfo mAdfInfo;
    private WatchDog mLocalizationWatchdog;

    public TangoForSavedAdf(TangoUpdater tangoUpdater,
                            TangoFactory tangoFactory,
                            WallyRenderer wallyRenderer,
                            TangoPointCloudManager pointCloudManager){
        super(tangoUpdater, tangoFactory, wallyRenderer, pointCloudManager);
        mLocalizationWatchdog = new WatchDog() {
            @Override
            protected void onTimeout() {
                // TODO delete bad ADF from cloud
                mFailStateConnector.toNextState();
            }

            @Override
            protected boolean conditionFailed() {
                return !mIsLocalized;
            }
        };
    }

    public TangoForSavedAdf withAdf(AdfInfo adf){
        mAdfInfo = adf;
        return this;
    }

    @Override
    protected void pauseHook() {
        mLocalizationWatchdog.disarm();
    }

    @Override
    protected void resumeHook() {
        startLocalizing();
        fireLocalizationStart();
        mLocalizationWatchdog.arm();
    }

    public TangoForSavedAdf withLocalizationTimeout(long timeout){
        mLocalizationWatchdog = mLocalizationWatchdog.withTimeout(timeout);
        return this;
    }

    protected void startLocalizing(){
        Log.d(TAG, "startLocalizing with: adf = [" + mAdfInfo + "]");
        final TangoFactory.RunnableWithError r = getTangoInitializer();
        mTango = mTangoFactory.getTangoForLocalAdf(r, mAdfInfo.getUuid());
        Log.d(TAG, "startLocalizing() mTango = " + mTango);
    }

    @Override
    public void onLocalization(boolean localization) {
        super.onLocalization(localization);
        if (localization){
            mLocalizationWatchdog.disarm();
            fireLocalizationFinish();
            mSuccessStateConnector.toNextState();
        }
    }

    @Override
    public AdfInfo getAdf() {
        return mAdfInfo;
    }

    private void fireLocalizationStart() {
        fireEvent(WallyEvent.createEventWithId(WallyEvent.LOCALIZATION_START));
    }

    private void fireLocalizationFinish() {
        fireEvent(WallyEvent.createEventWithId(WallyEvent.LOCALIZATION_FINISH_AFTER_SAVED_ADF));
    }
}
