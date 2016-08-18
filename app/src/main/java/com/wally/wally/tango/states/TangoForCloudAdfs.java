package com.wally.wally.tango.states;

import android.util.Log;

import com.projecttango.tangosupport.TangoPointCloudManager;
import com.wally.wally.adf.AdfInfo;
import com.wally.wally.adf.AdfScheduler;
import com.wally.wally.renderer.WallyRenderer;
import com.wally.wally.tango.TangoFactory;
import com.wally.wally.tango.TangoUpdater;

/**
 * Created by shota on 8/9/16.
 * Manages Tango which downloads Adfs and tries to localize
 */
public class TangoForCloudAdfs extends TangoForSavedAdf{
    private static final String TAG = TangoForCloudAdfs.class.getSimpleName();

    private long mLocalizationTimeout = 20000;
    private AdfScheduler mAdfScheduler;

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
        Log.d(TAG, "pause Thread = " + Thread.currentThread());
        mAdfScheduler.finish();
    }

    @Override
    protected void resumeHook() {
        Log.d(TAG, "resume Thread = " + Thread.currentThread());
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
                    mFailStateConnector.toNextState();
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

    @Override
    public void onLocalization(boolean localization) {//TODO refactor inheritance
        Log.d(TAG, "onLocalization() called with: " + "localization = [" + localization + "]");
        mIsLocalized = localization;
        if (localization) {
            mAdfScheduler.finish();
            mSuccessStateConnector.toNextState();
        }
    }

    @Override
    public AdfInfo getAdf() {
        return mAdfInfo;
    }
}
