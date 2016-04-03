package com.wally.wally.dal;

import android.support.annotation.Nullable;

public interface Callback<T> {

    void call(T result, @Nullable Exception e);

}
