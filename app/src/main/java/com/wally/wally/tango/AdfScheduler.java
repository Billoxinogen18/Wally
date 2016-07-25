package com.wally.wally.tango;

import android.util.Log;

import com.wally.wally.adfCreator.AdfInfo;
import com.wally.wally.adfCreator.AdfManager;
import com.wally.wally.datacontroller.callbacks.Callback;

import java.util.ArrayList;
import java.util.List;

public class AdfScheduler extends Thread {
    public static final String TAG = AdfScheduler.class.getSimpleName();
    public static final int DEFAULT_TIMEOUT = 1000;

    private boolean done;
    private int timeout;
    private AdfManager mAdfManager;
    private List<Callback<AdfInfo>> callbackList;

    public AdfScheduler(AdfManager adfManager) {
        done = false;
        timeout = DEFAULT_TIMEOUT;
        this.mAdfManager = adfManager;
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
        interrupt();
    }

    private void fireSuccess(AdfInfo info) {
        if (info != null) {
            Log.d(TAG, info.getUuid());
        } else {
            Log.d(TAG, "end");
        }
        for (Callback<AdfInfo> c : callbackList) {
            c.onResult(info);
        }
    }

    private void fireError(Exception e){
        for (Callback<AdfInfo> c : callbackList) {
            c.onError(e);
        }
    }

    @Override
    public void run() {
        while (!done && !isInterrupted()) {
            mAdfManager.getAdf(new Callback<AdfInfo>() {
                @Override
                public void onResult(AdfInfo result) {
                    if (done || isInterrupted()) return;
                    fireSuccess(result);
                }
                @Override
                public void onError(Exception e) {
                    fireError(e);
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