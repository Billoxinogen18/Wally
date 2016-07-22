package com.wally.wally.tango;


import com.wally.wally.datacontroller.callbacks.Callback;

public class LearningEvaluator {

    public void addCallback(final LearningEvaluatorListener listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(20000);
                } catch (InterruptedException e) {}
                listener.onLearningFinish();
            }
        }).start();
    }

    interface LearningEvaluatorListener{
        void onLearningFinish();
        void onLearningFailed();
    }
}