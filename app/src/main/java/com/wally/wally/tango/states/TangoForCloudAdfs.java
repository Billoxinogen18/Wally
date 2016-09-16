package com.wally.wally.tango.states;

import com.projecttango.tangosupport.TangoPointCloudManager;
import com.wally.wally.adf.AdfInfo;
import com.wally.wally.adf.AdfScheduler;
import com.wally.wally.events.WallyEvent;
import com.wally.wally.renderer.WallyRenderer;
import com.wally.wally.tango.TangoFactory;
import com.wally.wally.tango.TangoUpdater;

/**
 * Created by shota on 8/9/16.
 * Manages Tango which downloads Adfs and tries to localize
 */
public class TangoForCloudAdfs extends TangoState {

    private long mLocalizationTimeout = 20000;
    private AdfScheduler mAdfScheduler;
    private AdfInfo mAdfInfo;

    public TangoForCloudAdfs(TangoUpdater tangoUpdater,
                             TangoFactory tangoFactory,
                             WallyRenderer wallyRenderer,
                             TangoPointCloudManager pointCloudManager,
                             AdfScheduler adfScheduler){
        super(tangoUpdater, tangoFactory, wallyRenderer, pointCloudManager);
        mAdfScheduler = adfScheduler;
    }

    public TangoForCloudAdfs withLocalizationTimeout(long timeout){
        mLocalizationTimeout = timeout;
        return this;
    }

    @Override
    protected void pauseHook() {
        mAdfScheduler.finish();
    }

    @Override
    protected void resumeHook() {
        mAdfScheduler = mAdfScheduler
                .withTimeout(mLocalizationTimeout)
                .addListener(createAdfSchedulerListener());
        mAdfScheduler.start();
    }

    private AdfScheduler.AdfSchedulerListener createAdfSchedulerListener() {
        return new AdfScheduler.AdfSchedulerListener() {
            @Override
            public void onNewAdfSchedule(AdfInfo info) {
                if (mIsLocalized) { return; }
                mAdfInfo = info;
                String path = mAdfInfo.getPath();
                if (mTango == null) {
                    mTango = mTangoFactory.getTangoForCloudAdf(getTangoInitializer(), path);
                } else {
                    mTango.experimentalLoadAreaDescriptionFromFile(path);
                }
                fireLocalizationStart();
            }

            @Override
            public void onScheduleFinish() {
                if (mIsLocalized) { return; }
                mFailStateConnector.toNextState();
            }
        };
    }

    @Override
    public void onLocalization(boolean localization) {
        super.onLocalization(localization);
        if (localization) {
            mAdfScheduler.finish();
            fireLocalizationFinish();
            mSuccessStateConnector.toNextState();
        }
    }

    @Override
    public AdfInfo getAdf() {
        return mAdfInfo;
    }

    private void fireLocalizationFinish() {
        fireEvent(WallyEvent.createEventWithId(WallyEvent.LOCALIZATION_FINISH_AFTER_CLOUD_ADF));
    }

    private void fireLocalizationStart() {
        fireEvent(WallyEvent.createEventWithId(WallyEvent.LOCALIZATION_START_ON_CLOUD_ADF));
    }
}
