package com.wally.wally.tango.states;

import com.projecttango.tangosupport.TangoPointCloudManager;
import com.wally.wally.adf.AdfInfo;
import com.wally.wally.events.WallyEvent;
import com.wally.wally.renderer.WallyRenderer;
import com.wally.wally.tango.TangoFactory;
import com.wally.wally.tango.TangoUpdater;
import com.wally.wally.tango.WatchDog;

/**
 * Created by shota on 8/9/16.
 * Manages Tango for learned Adf
 */
public class TangoForLearnedAdf extends TangoState {
    private AdfInfo mAdfInfo;
    private WatchDog mLocalizationWatchdog;

    public TangoForLearnedAdf(TangoUpdater tangoUpdater,
                              TangoFactory tangoFactory,
                              WallyRenderer wallyRenderer,
                              TangoPointCloudManager pointCloudManager) {

        super(tangoUpdater, tangoFactory, wallyRenderer, pointCloudManager);
        mLocalizationWatchdog = new WatchDog() {
            @Override
            protected void onTimeout() {
                // TODO delete bad ADF
                mFailStateConnector.toNextState();
            }

            @Override
            protected boolean conditionFailed() {
                return !mIsLocalized;
            }
        };
    }

    public TangoForLearnedAdf withAdf(AdfInfo adf){
        mAdfInfo = adf;
        return this;
    }

    public TangoForLearnedAdf withLocalizationTimeout(long timeout){
        mLocalizationWatchdog = mLocalizationWatchdog.withTimeout(timeout);
        return this;
    }

    @Override
    protected void pauseHook() {
        mLocalizationWatchdog.disarm();
    }

    @Override
    protected void resumeHook() {
        mTango = mTangoFactory.getTangoWithUuid(getTangoInitializer(), mAdfInfo.getUuid());
        mLocalizationWatchdog.arm();
        fireEvent(WallyEvent.LOC_START_EVENT);
    }

    @Override
    public void onLocalization(boolean localization) {
        super.onLocalization(localization);
        if (localization){
            mLocalizationWatchdog.disarm();
            fireEvent(WallyEvent.LOC_FINISH_EVENT);
            mSuccessStateConnector.toNextState();
        }
    }

    @Override
    public AdfInfo getAdf() {
        return mAdfInfo;
    }

}
