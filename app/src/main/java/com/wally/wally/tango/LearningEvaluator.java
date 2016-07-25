package com.wally.wally.tango;


import android.util.Log;

import com.google.atap.tangoservice.TangoPoseData;

public class LearningEvaluator implements TangoUpdater.ValidPoseListener {
    public static final String TAG = LearningEvaluator.class.getSimpleName();
    public static final int TIMEOUT_S = 20;
    public void addCallback(final LearningEvaluatorListener listener) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(TIMEOUT_S * 1000);
                } catch (InterruptedException e) {
                    listener.onLearningFailed();
                }
                listener.onLearningFinish();
            }
        }).start();
    }

    @Override
    public void onValidPose(TangoPoseData pose) {
        Log.d(TAG, "" + (pose.statusCode == TangoPoseData.POSE_VALID));
    }

    interface LearningEvaluatorListener{
        void onLearningFinish();
        void onLearningFailed();
    }
}