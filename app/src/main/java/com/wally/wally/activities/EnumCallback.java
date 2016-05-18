package com.wally.wally.activities;

import com.wally.wally.datacontroller.callbacks.FetchResultCallback;

public abstract class EnumCallback implements FetchResultCallback {
    private long id;

    public EnumCallback(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }
}
