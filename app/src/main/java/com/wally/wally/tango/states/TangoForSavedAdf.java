package com.wally.wally.tango.states;

import android.util.Log;

import com.google.atap.tangoservice.Tango;
import com.projecttango.tangosupport.TangoPointCloudManager;
import com.wally.wally.adf.AdfInfo;
import com.wally.wally.renderer.WallyRenderer;
import com.wally.wally.tango.EventListener;
import com.wally.wally.tango.TangoFactory;
import com.wally.wally.tango.TangoUpdater;
import com.wally.wally.tango.TangoUtils;

import java.util.Map;

/**
 * Created by shota on 8/10/16.
 * Manages Tango after resume with previously saved Adf
 */
public class TangoForSavedAdf extends TangoForAdf {
    private static final String TAG = TangoForSavedAdf.class.getSimpleName();

    public TangoForSavedAdf(TangoUpdater tangoUpdater,
                            TangoFactory tangoFactory,
                            WallyRenderer wallyRenderer,
                            Map<Class, TangoBase> tangoStatePool,
                            TangoPointCloudManager pointCloudManager){
        super(tangoUpdater, tangoFactory, wallyRenderer, tangoStatePool, pointCloudManager);
    }

    public TangoForAdf withAdf(AdfInfo adf){
        mAdfInfo = adf;
        return this;
    }

    @Override
    public void resume() {
        startLocalizing();
        startLocalizationWatchDog();
    }

    protected void startLocalizing(){
        Log.d(TAG, "startLocalizing with: adf = [" + mAdfInfo + "]");
        final TangoFactory.RunnableWithError r = getTangoInitializer();
        mTango = mTangoFactory.getTango(new TangoFactory.RunnableWithError() {
            @Override
            public void run() {
                r.run();
                loadAdf(mTango, mAdfInfo);
            }

            @Override
            public void onError(Exception e) {
                r.onError(e);
            }
        });
        fireLocalizationStart();
    }

    private void loadAdf(Tango tango, AdfInfo adf) {
        if (TangoUtils.isAdfImported(tango, adf.getUuid())) {
            tango.experimentalLoadAreaDescription(adf.getUuid());
        } else {
            tango.experimentalLoadAreaDescriptionFromFile(adf.getPath());
        }
    }

    private void fireLocalizationStart() {
        for (EventListener listener : mEventListeners) {
            listener.onLocalizationStart();
        }

    }

    @Override
    protected void fireLocalizationFinish() {
        fireLocalizationFinishAfterSavedAdf();
    }

    private void fireLocalizationFinishAfterSavedAdf() {
        for (EventListener listener : mEventListeners) {
            listener.onLocalizationFinishAfterSavedAdf();
        }
    }
}
