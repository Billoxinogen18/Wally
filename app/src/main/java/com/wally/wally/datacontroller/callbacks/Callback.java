package com.wally.wally.datacontroller.callbacks;

@Deprecated
public interface Callback<T> {

    void onResult(T result);

    void onError(Exception e);
}