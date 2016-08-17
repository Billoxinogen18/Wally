package com.wally.wally.tango.states;

import android.util.Log;

import com.projecttango.tangosupport.TangoPointCloudManager;
import com.wally.wally.adf.AdfInfo;
import com.wally.wally.renderer.WallyRenderer;
import com.wally.wally.tango.TangoFactory;
import com.wally.wally.tango.TangoUpdater;

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
                       TangoPointCloudManager pointCloudManager){
        super(executor, tangoUpdater, tangoFactory, wallyRenderer, pointCloudManager);
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
            mSuccessStateConnector.toNextState();
            fireLocalizationFinish();
        }
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
                    mFailStateConnector.toNextState();
                }
            }
        });
        mLocalizationWatchdog.start();
    }

    @Override
    public AdfInfo getAdf() {
        return mAdfInfo;
    }

    protected abstract void fireLocalizationFinish();
}
