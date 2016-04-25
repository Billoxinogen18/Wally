package com.wally.wally.datacontroller.dal;

public interface Callback<T> {
    void call(T result, Exception e);
}
