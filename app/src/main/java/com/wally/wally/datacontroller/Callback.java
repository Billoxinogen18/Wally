package com.wally.wally.datacontroller;

@Deprecated
public interface Callback<T> {
    void call(T result, Exception e);
}
