package com.wally.wally.tango.states;

import com.projecttango.tangosupport.TangoPointCloudManager;
import com.wally.wally.adf.AdfInfo;
import com.wally.wally.adf.AdfScheduler;
import com.wally.wally.renderer.WallyRenderer;
import com.wally.wally.tango.TangoFactory;
import com.wally.wally.tango.TangoUpdater;

import java.util.Map;

/**
 * Created by shota on 8/9/16.
 * Manages Tango which downloads Adfs and tries to localize
 */
public class TangoForCloudAdfs extends TangoForSavedAdf{
    private long mLocalizationTimeout = 20000;
    private AdfScheduler mAdfScheduler;

    public TangoForCloudAdfs(TangoUpdater tangoUpdater,
                             TangoFactory tangoFactory,
                             WallyRenderer wallyRenderer,
                             Map<Class, TangoBase> tangoStatePool,
                             TangoPointCloudManager pointCloudManager,
                             AdfScheduler adfScheduler){
        super(tangoUpdater, tangoFactory, wallyRenderer, tangoStatePool, pointCloudManager);
        mAdfScheduler = adfScheduler;
    }

    public TangoForCloudAdfs withLocalizationTimeout(long timeout){
        mLocalizationTimeout = timeout;
        return this;
    }

    @Override
    public synchronized void pause() {
        super.pause();
    }

    @Override
    public void resume() {
        mAdfScheduler = mAdfScheduler
                .withTimeout(mLocalizationTimeout)
                .addListener(createAdfSchedulerListener());
        mAdfScheduler.start();
    }

    private AdfScheduler.AdfSchedulerListener createAdfSchedulerListener() {
        return new AdfScheduler.AdfSchedulerListener() {
            @Override
            public void onNewAdfSchedule(AdfInfo info) {
                if (mIsLocalized) {
                    return;
                }
                if (info == null) {
                    changeToLearningState();
                } else {
                    withAdf(info);
                    startLocalizing();
                }
            }

            @Override
            public void onScheduleFinish() {
                // TODO rethink this shit!
                this.onNewAdfSchedule(null);
            }
        };
    }


    private void changeToLearningState(){
        TangoBase nextTango = mTangoStatePool.get(TangoForLearning.class);
        changeState(nextTango);
        nextTango.resume();
    }


    @Override
    public void onLocalization(boolean localization) {//TODO refactor inheritance
        mIsLocalized = localization;
        if (localization) {
            mAdfScheduler.finish();
            changeToReadyState();
        }
    }
}
