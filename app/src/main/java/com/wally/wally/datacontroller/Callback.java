package com.wally.wally.datacontroller;

public interface Callback<T> {
    void call(T result, Exception e);
}
