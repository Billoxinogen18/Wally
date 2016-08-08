package com.wally.wally.tango;

public interface ProgressReporter {
    void forceReport();
    void addProgressListener(ProgressListener listener);
}