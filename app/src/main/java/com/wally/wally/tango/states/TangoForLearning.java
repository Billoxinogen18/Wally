package com.wally.wally.tango.states;

import android.util.Log;

import com.projecttango.tangosupport.TangoPointCloudManager;
import com.wally.wally.adf.AdfInfo;
import com.wally.wally.renderer.WallyRenderer;
import com.wally.wally.tango.EventListener;
import com.wally.wally.tango.LearningEvaluator;
import com.wally.wally.tango.LocalizationAnalytics;
import com.wally.wally.tango.TangoFactory;
import com.wally.wally.tango.TangoUpdater;

import java.util.Map;

/**
 * Created by shota on 8/9/16.
 * Tango state for learning
 */
public class TangoForLearning extends TangoBase {
    private static final String TAG = TangoForLearning.class.getSimpleName();
    private LearningEvaluator mLearningEvaluator;


    public TangoForLearning(TangoUpdater tangoUpdater,
                            TangoFactory tangoFactory,
                            WallyRenderer wallyRenderer,
                            LearningEvaluator evaluator,
                            LocalizationAnalytics analytics,
                            Map<Class, TangoBase> tangoStatePool,
                            TangoPointCloudManager pointCloudManager) {
        super(tangoUpdater, tangoFactory, wallyRenderer, analytics, tangoStatePool, pointCloudManager);
        mLearningEvaluator = evaluator;
    }

    @Override
    public synchronized void resume(){
        Log.d(TAG, "startLearning");
        mLearningEvaluator.addLearningEvaluatorListener(getLearningEvaluatorListener());
        mTangoUpdater.addValidPoseListener(mLearningEvaluator);
        mTango = mTangoFactory.getTangoForLearning(getTangoInitializer());
        fireStartLearning();
    }

    @Override
    public boolean isLearningState() {
        return true;
    }

    private LearningEvaluator.LearningEvaluatorListener getLearningEvaluatorListener(){
        return new LearningEvaluator.LearningEvaluatorListener() {
            @Override
            public void onLearningFinish() {
                if (mIsLocalized) {
                    mTangoUpdater.removeValidPoseListener(mLearningEvaluator);
                    finishLearning();
                } else {
                    onLearningFailed();
                }
            }

            @Override
            public void onLearningFailed() {
                mTangoUpdater.removeValidPoseListener(mLearningEvaluator);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        pause();
                        resume();
                    }
                }).start();
            }
        };
    }

    private synchronized void finishLearning() {
        Log.d(TAG, "finishLearning");
        AdfInfo info = saveAdf();
        fireFinishLearning();
        mLocalizationAnalytics.onAdfCreate();
        changeToLearnedAdfState(info);
    }

    private void changeToLearnedAdfState(AdfInfo info){
        pause();
        TangoBase nextTango = ((TangoForLearnedAdf)mTangoStatePool.get(TangoForLearnedAdf.class)).withAdf(info);
        changeState(nextTango);
        nextTango.resume();
    }

    private AdfInfo saveAdf() {
        Log.d(TAG, "saveAdf");
        String uuid = mTango.saveAreaDescription();
        return new AdfInfo().withUuid(uuid);
    }

    private void fireStartLearning(){
        for (EventListener eventListener : mEventListeners) {
            eventListener.onLearningStart();
        }
    }

    private void fireFinishLearning(){
        for (EventListener eventListener : mEventListeners) {
            eventListener.onLearningFinish();
        }
    }

}
