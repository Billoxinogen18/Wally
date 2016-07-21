package com.wally.wally.tango;

import android.util.Log;

import com.wally.wally.adfCreator.AdfInfo;
import com.wally.wally.adfCreator.AdfManager;
import com.wally.wally.datacontroller.callbacks.Callback;

import java.util.ArrayList;
import java.util.List;

public class AdfScheduler extends Thread {
    public static final String TAG = AdfScheduler.class.getSimpleName();
    public static final int DEFAULT_TIMEOUT = 15000;

    private AdfManager mAdfManager;
    private boolean done;
    private int timeout;
    private List<Callback<AdfInfo>> callbackList;

    public AdfScheduler(AdfManager adfManager) {
        done = false;
        this.mAdfManager = adfManager;
        timeout = DEFAULT_TIMEOUT;
        callbackList = new ArrayList<>();
    }

    public AdfScheduler withTimeout(int timeoutMs) {
        this.timeout = timeoutMs;
        return this;
    }

    public AdfScheduler addCallback(Callback<AdfInfo> callback) {
        callbackList.add(callback);
        return this;
    }

    public void finish() {
        this.done = true;
    }

    @Override
    public void run() {
        while (!isInterrupted() && !done) {
            Log.d(TAG, "Localizer step");
            mAdfManager.getAdf(new Callback<AdfInfo>() {
                @Override
                public void onResult(AdfInfo result) {
                    Log.d(TAG, "onResult: " + result);
                    for (Callback<AdfInfo> c : callbackList) {
                        c.onResult(result);
                    }
                }

                @Override
                public void onError(Exception e) {
                    for (Callback<AdfInfo> c : callbackList) {
                        c.onError(e);
                    }
                }
            });
            try {
                Thread.sleep(timeout);
            } catch (InterruptedException e) {
                break;
            }
        }
    }
}