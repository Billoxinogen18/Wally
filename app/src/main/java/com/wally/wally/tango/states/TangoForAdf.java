package com.wally.wally.tango.states;

import android.util.Log;

import com.projecttango.tangosupport.TangoPointCloudManager;
import com.wally.wally.adf.AdfInfo;
import com.wally.wally.renderer.WallyRenderer;
import com.wally.wally.tango.TangoFactory;
import com.wally.wally.tango.TangoUpdater;

import java.util.Map;

/**
 * Created by shota on 8/10/16.
 *
 */
public abstract class TangoForAdf extends TangoState {
    private static final String TAG = TangoForAdf.class.getSimpleName();

    protected AdfInfo mAdfInfo;
    private Thread mLocalizationWatchdog;
    private long mLocalizationTimeout = 20000;

    public TangoForAdf(Executor executor,
                       TangoUpdater tangoUpdater,
                       TangoFactory tangoFactory,
                       WallyRenderer wallyRenderer,
                       Map<Class, TangoState> tangoStatePool,
                       TangoPointCloudManager pointCloudManager){
        super(executor, tangoUpdater, tangoFactory, wallyRenderer, tangoStatePool, pointCloudManager);
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
        Log.d(TAG, "changeToReadyState");
        TangoState nextTango = ((TangoForReadyState)mTangoStatePool.get(TangoForReadyState.class)).withTangoAndAdf(mTango, mAdfInfo);
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
                    Log.d(TAG, "startLocalizationWatchDog failed");
                    pause();
                    Log.d(TAG, "changeToCloudAdfState");
                    TangoState nextTango = mTangoStatePool.get(TangoForCloudAdfs.class);
                    changeState(nextTango);
                }
            }
        });
        mLocalizationWatchdog.start();
    }

    protected abstract void fireLocalizationFinish();
}
