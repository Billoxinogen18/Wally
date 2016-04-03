package com.wally.wally.dal;

import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public interface Callback<T> {

    @MainThread
    void call(@NonNull T result, @Nullable Exception e);

}
