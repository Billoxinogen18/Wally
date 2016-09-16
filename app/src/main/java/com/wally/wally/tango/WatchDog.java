package com.wally.wally.tango;

/**
 * Created by Viper on 9/16/2016.
 *
 */
public abstract class WatchDog {
    private long timeoutMs;
    private Thread watchDog;

    public WatchDog(long timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    public WatchDog() {
        this(15000);
    }

    public WatchDog withTimeout(long timeoutMs) {
        this.timeoutMs = timeoutMs;
        return this;
    }

    public final void arm() {
        initWatchDog();
        watchDog.start();
    }

    public final void disarm() {
        if (watchDog != null) {
            watchDog.interrupt();
        }
    }

    private void initWatchDog() {
        watchDog = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(timeoutMs);
                } catch (InterruptedException e) {
                    return;
                }
                if (conditionFailed()) { onTimeout(); }
            }
        });
    }

    protected abstract void onTimeout();

    protected abstract boolean conditionFailed();
}
