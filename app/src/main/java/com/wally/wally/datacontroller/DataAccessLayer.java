package com.wally.wally.datacontroller;

import java.util.Collection;

public interface DataAccessLayer<DataType, QueryType> {

    /**
     * Save (update if exists) given object.
     * Even if connection fails, save it next time network is available.
     */
    void save(DataType data);

    /**
     * Save (update if exists) given object.
     * Call statusCallback with status of the operation and/or error reason
     */
    void save(DataType data, Callback<Boolean> statusCallback);

    /**
     * Delete given object.
     * Even if connection fails, delete it next time network is available.
     */
    void delete(DataType data);

    /**
     * Delete given object.
     * Call statusCallback with status of the operation and/or error reason
     */
    void delete(DataType data, Callback<Boolean> statusCallback);

    /**
     * Fetch objects satisfying given query
     * Call resultCallback with result of the operation and/or error reason
     */
    void fetch(QueryType query, Callback<Collection<DataType>> resultCallback);
}