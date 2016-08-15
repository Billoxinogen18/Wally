package com.wally.wally.tango.states;

import android.util.Log;

import com.google.atap.tangoservice.Tango;
import com.projecttango.tangosupport.TangoPointCloudManager;
import com.wally.wally.adf.AdfInfo;
import com.wally.wally.events.WallyEvent;
import com.wally.wally.renderer.WallyRenderer;
import com.wally.wally.tango.TangoFactory;
import com.wally.wally.tango.TangoUpdater;
import com.wally.wally.tango.TangoUtils;

/**
 * Created by shota on 8/10/16.
 * Manages Tango after resume with previously saved Adf
 */
public class TangoForSavedAdf extends TangoForAdf {
    private static final String TAG = TangoForSavedAdf.class.getSimpleName();

    public TangoForSavedAdf(Executor executor,
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

    @Override
    protected void pauseHook() {
        Log.d(TAG, "pause Thread = " + Thread.currentThread());
    }

    @Override
    protected void resumeHook() {
        Log.d(TAG, "resume Thread = " + Thread.currentThread());
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
        Log.d(TAG, "startLocalizing() mTango = " + mTango);
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
        fireEvent(WallyEvent.createEventWithId(WallyEvent.LOCALIZATION_START));
    }

    @Override
    protected void fireLocalizationFinish() {
        fireLocalizationFinishAfterSavedAdf();
    }

    private void fireLocalizationFinishAfterSavedAdf() {
        fireEvent(WallyEvent.createEventWithId(WallyEvent.LOCALIZATION_FINISH_AFTER_SAVED_ADF));
    }
}
