package com.wally.wally.dal;

import android.support.annotation.NonNull;

import java.util.Collection;

public interface DataAccessLayer<T> {

    /**
     * Save (update if exists) given object.
     * Even if connection fails, save it next time network is available.
     */
    void save(@NonNull T c);

    /**
     * Save (update if exists) given object.
     * Call statusCallback with status of the operation and/or error reason
     */
    void save(@NonNull T c, @NonNull Callback<Boolean> statusCallback);

    /**
     * Delete given object.
     * Even if connection fails, delete it next time network is available.
     */
    void delete(@NonNull T c);

    /**
     * Delete given object.
     * Call statusCallback with status of the operation and/or error reason
     */
    void delete(@NonNull T c, @NonNull Callback<Boolean> statusCallback);

    /**
     * Fetch objects satisfying given query
     * Call resultCallback with result of the operation and/or error reason
     */
    void fetch(@NonNull Query query, @NonNull Callback<Collection<T>> resultCallback);
}