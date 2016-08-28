package com.wally.wally.tango.states;

import android.util.Log;

import com.google.atap.tangoservice.TangoPoseData;
import com.projecttango.tangosupport.TangoPointCloudManager;
import com.wally.wally.adf.AdfInfo;
import com.wally.wally.events.WallyEvent;
import com.wally.wally.renderer.WallyRenderer;
import com.wally.wally.progressUtils.LearningEvaluator;
import com.wally.wally.tango.TangoFactory;
import com.wally.wally.tango.TangoUpdater;

/**
 * Created by shota on 8/9/16.
 * Tango state for learning
 */
public class TangoForLearning extends TangoState implements TangoUpdater.ValidPoseListener {
    private static final String TAG = TangoForLearning.class.getSimpleName();

    private AdfInfo mAdfInfo;
    private LearningEvaluator mLearningEvaluator;

    public TangoForLearning(TangoUpdater tangoUpdater,
                            TangoFactory tangoFactory,
                            WallyRenderer wallyRenderer,
                            LearningEvaluator evaluator,
                            TangoPointCloudManager pointCloudManager) {
        super(tangoUpdater, tangoFactory, wallyRenderer, pointCloudManager);
        mLearningEvaluator = evaluator;
    }

    @Override
    protected void pauseHook() {
        Log.d(TAG, "pause Thread = " + Thread.currentThread());
    }

    @Override
    protected void resumeHook() {
        Log.d(TAG, "startLearning Thread = " + Thread.currentThread());
        mLearningEvaluator.addLearningEvaluatorListener(getLearningEvaluatorListener());
        mTangoUpdater.addValidPoseListener(this);
        mTango = mTangoFactory.getTangoForLearning(getTangoInitializer());
        Log.d(TAG, "resume() mTango = " + mTango);
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
                    mTangoUpdater.removeValidPoseListener(TangoForLearning.this);
                    finishLearning();
                } else {
                    onLearningFailed();
                }
            }

            @Override
            public void onLearningFailed() {
                mTangoUpdater.removeValidPoseListener(TangoForLearning.this);
                mFailStateConnector.toNextState();
            }
        };
    }

    private synchronized void finishLearning() {
        Log.d(TAG, "finishLearning");
        mAdfInfo = saveAdf();
        fireFinishLearning();
        mSuccessStateConnector.toNextState();
    }

    @Override
    public AdfInfo getAdf() {
        return mAdfInfo;
    }

    private AdfInfo saveAdf() {
        Log.d(TAG, "saveAdf");
        String uuid = mTango.saveAreaDescription();
        return new AdfInfo().withUuid(uuid);
    }

    private void fireStartLearning(){
        fireEvent(WallyEvent.createEventWithId(WallyEvent.LEARNING_START));
    }

    private void fireFinishLearning(){
        fireEvent(WallyEvent.createEventWithId(WallyEvent.LEARNING_FINISH));
    }

    @Override
    public void onValidPose(TangoPoseData pose) {
        mLearningEvaluator.onValidPose(pose.translation, pose.rotation);
    }
}
