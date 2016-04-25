package com.wally.wally.datacontroller;

import java.util.Collection;

public interface DataAccessLayer<T> {

    /**
     * Save (update if exists) given object.
     * Even if connection fails, save it next time network is available.
     */
    void save(T data);

    /**
     * Save (update if exists) given object.
     * Call statusCallback with status of the operation and/or error reason
     */
    void save(T data, Callback<Boolean> statusCallback);

    /**
     * Delete given object.
     * Even if connection fails, delete it next time network is available.
     */
    void delete(T data);

    /**
     * Delete given object.
     * Call statusCallback with status of the operation and/or error reason
     */
    void delete(T data, Callback<Boolean> statusCallback);

    /**
     * Fetch objects satisfying given query
     * Call resultCallback with result of the operation and/or error reason
     */
    void fetch(Query query, Callback<Collection<T>> resultCallback);
}