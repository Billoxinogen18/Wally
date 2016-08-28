package com.wally.wally.progressUtils;

/**
 * Created by shota on 8/7/16.
 */
public interface ProgressListener {
    void onProgressUpdate(ProgressReporter self, double progress);
}
