package com.wally.wally.tango.states;

import com.google.atap.tangoservice.Tango;
import com.projecttango.tangosupport.TangoPointCloudManager;
import com.wally.wally.adf.AdfInfo;
import com.wally.wally.config.Config;
import com.wally.wally.renderer.WallyRenderer;
import com.wally.wally.tango.EventListener;
import com.wally.wally.tango.LocalizationAnalytics;
import com.wally.wally.tango.TangoFactory;
import com.wally.wally.tango.TangoUpdater;

import java.util.Map;

/**
 * Created by shota on 8/9/16.
 * Manages Tango for Ready State
 */
public class TangoForReadyState extends TangoBase {
    private AdfInfo mAdfInfo;

    public TangoForReadyState(Config config,
                              TangoUpdater tangoUpdater,
                              TangoFactory tangoFactory,
                              WallyRenderer wallyRenderer,
                              LocalizationAnalytics analytics,
                              Map<Class, TangoBase> tangoStatePool,
                              TangoPointCloudManager pointCloudManager){
        super(config, tangoUpdater, tangoFactory, wallyRenderer, analytics, tangoStatePool, pointCloudManager);
    }

    public TangoForReadyState withTangoAndAdf(Tango tango, AdfInfo adf) {
        super.mTango = tango;
        this.mAdfInfo = adf;
        return this;
    }

    @Override
    public synchronized void pause() {
        TangoBase nextTango = ((TangoForSavedAdf)mTangoStatePool.get(TangoForSavedAdf.class)).withAdf(mAdfInfo);
        mStateChangeListener.onStateChange(nextTango);
    }

    @Override
    public void resume() {
        fireOnTangoReady();
    }

    @Override
    public AdfInfo getAdf() {
        return mAdfInfo;
    }

    private void fireOnTangoReady(){
        for (EventListener listener: mEventListeners){
            listener.onTangoReady();
        }
    }
}
