package com.wally.wally.tango.states;

import com.projecttango.tangosupport.TangoPointCloudManager;
import com.wally.wally.adf.AdfInfo;
import com.wally.wally.adf.AdfService;
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
    private AdfInfo mAdfInfo;
    private WatchDog mLocalizationWatchdog;

    public TangoForSavedAdf(TangoUpdater tangoUpdater,
                            TangoFactory tangoFactory,
                            WallyRenderer wallyRenderer,
                            final AdfService adfService,
                            TangoPointCloudManager pointCloudManager){
        super(tangoUpdater, tangoFactory, wallyRenderer, pointCloudManager);
        mLocalizationWatchdog = new WatchDog() {
            @Override
            protected void onTimeout() {
                adfService.delete(mAdfInfo);
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
        mTango = mTangoFactory.getTangoForLocalAdf(getTangoInitializer(), mAdfInfo.getUuid());
        fireLocalizationStart();
        mLocalizationWatchdog.arm();
    }

    public TangoForSavedAdf withLocalizationTimeout(long timeout){
        mLocalizationWatchdog = mLocalizationWatchdog.withTimeout(timeout);
        return this;
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
