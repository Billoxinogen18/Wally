package com.wally.wally.dal;

import android.support.annotation.NonNull;

import java.util.Collection;

public interface DataAccessLayer {

    /**
     * Save or update object if possible and return status with this callback.
     * If object with this Id is in database already, the method will update that object.
     * Otherwise if Id is null create new entry.
     * If Id is not null and isn't in database, undefined behaviour.
     */
    void save(@NonNull Content c, @NonNull Callback<Boolean> statusCallback);

    /**
     * Save Object even if connection fails.
     * This means that object will be saved eventually when
     * network is available.
     * Other than that, behaviour is the same as save method.
     *
     * @see {#save(Content c, StatusCallback callback)}
     */
    void saveEventually(@NonNull Content c);

    /**
     * Deletes object with this Id.
     */
    void delete(@NonNull Content c, @NonNull Callback<Boolean> statusCallback);

    /**
     * Same as saveEventually. @see{#saveEventually(Content c)}
     * Other that that @see {#delete(Content c, StatusCallback callback)}
     */
    void deleteEventually(@NonNull Content c);

    /**
     * Fetch Objects by ...
     */
    void fetch(@NonNull Query query, @NonNull Callback<Collection<Content>> resultCallback);
}