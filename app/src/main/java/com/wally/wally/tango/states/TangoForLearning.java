package com.wally.wally.tango.states;

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
    protected void pauseHook() { }

    @Override
    protected void resumeHook() {
        mLearningEvaluator.addLearningEvaluatorListener(getLearningEvaluatorListener());
        mTango = mTangoFactory.getTangoForLearning(getTangoInitializer());
        mTangoUpdater.addValidPoseListener(this);
        fireEvent(WallyEvent.LEARNING_START_EVENT);
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
        mAdfInfo = new AdfInfo()
                .withUuid(mTango.saveAreaDescription());
        fireEvent(WallyEvent.LEARNING_FINISH_EVENT);
        mSuccessStateConnector.toNextState();
    }

    @Override
    public AdfInfo getAdf() {
        return mAdfInfo;
    }

    @Override
    public void onValidPose(TangoPoseData pose) {
        mLearningEvaluator.onValidPose(pose.translation, pose.rotation);
    }
}
