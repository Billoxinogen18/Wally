package com.wally.wally.adf;

import android.util.Log;

import com.wally.wally.progressUtils.ProgressListener;
import com.wally.wally.progressUtils.ProgressReporter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class AdfScheduler implements ProgressReporter {
    private static final String TAG = AdfScheduler.class.getSimpleName();
    private static final int DEFAULT_TIMEOUT = 1000;

    private boolean done;
    private long timeout;
    private AdfManager mAdfManager;
    private List<AdfSchedulerListener> callbackList;

    private ProgressListener listener;
    private int adfsSoFar;
    private Thread scheduler;

    public AdfScheduler(AdfManager adfManager) {
        //initial();
        done = false;
        callbackList = new ArrayList<>();
        adfsSoFar = 0;
        timeout = DEFAULT_TIMEOUT;
        this.mAdfManager = adfManager;
    }

    public AdfScheduler withTimeout(long timeoutMs) {
        this.timeout = timeoutMs;
        return this;
    }

    public synchronized AdfScheduler addListener(AdfSchedulerListener listener) {
        callbackList.add(listener);
        return this;
    }

    public synchronized void finish() {
        this.done = true;
        if (!scheduler.isInterrupted()) {
            scheduler.interrupt();
        }
        callbackList.clear();
        listener.onProgressUpdate(this, 1);
    }

    private synchronized void fireSuccess(AdfInfo info) {
        for (AdfSchedulerListener c : callbackList) {
            if (info != null) {
                Log.d(TAG, info.getUuid());
                c.onNewAdfSchedule(info);
            } else {
                Log.d(TAG, "end");
                c.onScheduleFinish();
            }
        }
    }

    public void start() {
        done = false;
        scheduler = new Thread(new Runnable() {
            @Override
            public void run() {
                schedulingLoop();
            }
        });
        scheduler.start();
    }


    private void schedulingLoop() {
        while (!done && !scheduler.isInterrupted()) {
            Log.d(TAG, "schedulingLoop() called with: " + "");
            final CountDownLatch latch = new CountDownLatch(1);
            mAdfManager.getAdf(new AdfManager.AdfManagerStateListener() {
                @Override
                public void onAdfReady(AdfInfo info) {
                    if (done || scheduler.isInterrupted()) return;
                    fireSuccess(info);
                    fireProgress();
                    latch.countDown();
                }

                @Override
                public void onNoMoreAdfs() {
                    fireSuccess(null);
                    listener.onProgressUpdate(AdfScheduler.this, 1);
                }
            });

            try {
                latch.await();
                Thread.sleep(timeout);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    private void fireProgress() {
        adfsSoFar++;
        double progress = (double) adfsSoFar / mAdfManager.getAdfTotalCount();
        listener.onProgressUpdate(this, progress);
    }

    @Override
    public void addProgressListener(ProgressListener listener) {
        this.listener = listener;
    }

    public interface AdfSchedulerListener {
        void onNewAdfSchedule(AdfInfo info);

        void onScheduleFinish();
    }
}