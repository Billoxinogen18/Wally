package com.wally.wally.datacontroller.callbacks;

public interface Callback<T> {

    void onResult(T result);

    void onError(Exception e);
}