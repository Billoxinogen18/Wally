package com.wally.wally.tango.states;

import android.util.Log;

import com.google.atap.tangoservice.Tango;
import com.projecttango.tangosupport.TangoPointCloudManager;
import com.wally.wally.adf.AdfInfo;
import com.wally.wally.renderer.WallyRenderer;
import com.wally.wally.tango.EventListener;
import com.wally.wally.tango.TangoFactory;
import com.wally.wally.tango.TangoUpdater;

import java.util.Map;

/**
 * Created by shota on 8/9/16.
 * Manages Tango for Ready State
 */
public class TangoForReadyState extends TangoState {
    private static final String TAG = TangoForReadyState.class.getSimpleName();

    private AdfInfo mAdfInfo;

    public TangoForReadyState(TangoUpdater tangoUpdater,
                              TangoFactory tangoFactory,
                              WallyRenderer wallyRenderer,
                              Map<Class, TangoState> tangoStatePool,
                              TangoPointCloudManager pointCloudManager){
        super(tangoUpdater, tangoFactory, wallyRenderer, tangoStatePool, pointCloudManager);
    }

    public TangoForReadyState withTangoAndAdf(Tango tango, AdfInfo adf) {
        Log.d(TAG, "withTangoAndAdf() called with: " + "tango = [" + tango + "], adf = [" + adf + "]");
        super.mTango = tango;
        this.mAdfInfo = adf;
        return this;
    }

    @Override
    public synchronized void pause() {
        Log.d(TAG, "changeToSavedAdfState Thread = " + Thread.currentThread());
        TangoState nextTango = ((TangoForSavedAdf)mTangoStatePool.get(TangoForSavedAdf.class)).withAdf(mAdfInfo);
        changeState(nextTango);
    }

    @Override
    public void resume() {
        Log.d(TAG, "resume Thread = " + Thread.currentThread());
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
