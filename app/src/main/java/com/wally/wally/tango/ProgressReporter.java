package com.wally.wally.tango;

/**
 * Created by shota on 8/8/16.
 */
public interface ProgressReporter {
//    void onProgressUpdate(ProgressReporter reporter, double progress);
    void addProgressListener(ProgressListener listener);
}
