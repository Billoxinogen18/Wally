package com.wally.wally.tango.states;

import android.util.Log;

import com.projecttango.tangosupport.TangoPointCloudManager;
import com.wally.wally.adf.AdfInfo;
import com.wally.wally.config.Config;
import com.wally.wally.renderer.WallyRenderer;
import com.wally.wally.tango.LocalizationAnalytics;
import com.wally.wally.tango.TangoFactory;
import com.wally.wally.tango.TangoUpdater;

import java.util.Map;

/**
 * Created by shota on 8/10/16.
 */
public abstract class TangoForAdf extends TangoBase {
    private static final String TAG = TangoForAdf.class.getSimpleName();

    protected AdfInfo mAdfInfo;
    private Thread mLocalizationWatchdog;
    private long mLocalizationTimeout = 20000;

    public TangoForAdf(Config config,
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

    public TangoForAdf withLocalizationTimeout(long timeout){
        mLocalizationTimeout = timeout;
        return this;
    }


    @Override
    public void onLocalization(boolean localization) {
        Log.d(TAG, "onLocalization() called with: " + "localization = [" + localization + "]");
        super.onLocalization(localization);
        if (mIsLocalized){
            if (mLocalizationWatchdog != null) {
                mLocalizationWatchdog.interrupt();
            }
            changeToReadyState();
            fireLocalizationFinish();
        }
    }

    protected void changeToReadyState(){
        TangoBase nextTango = ((TangoForReadyState)mTangoStatePool.get(TangoForReadyState.class)).withTangoAndAdf(mTango, mAdfInfo);
        changeState(nextTango);
    }

    protected void startLocalizationWatchDog() {
        mLocalizationWatchdog = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(mLocalizationTimeout);
                } catch (InterruptedException e) {
                    return;
                }
                if (!mIsLocalized) {
                    mLocalizationAnalytics.logLocalization(false);
                    pause();
                    TangoBase nextTango = mTangoStatePool.get(TangoForCloudAdfs.class);
                    changeState(nextTango);
                }
            }
        });
        mLocalizationWatchdog.start();
    }

    protected abstract void fireLocalizationFinish();
}
