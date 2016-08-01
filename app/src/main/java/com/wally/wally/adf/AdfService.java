package com.wally.wally.adf;

import com.wally.wally.datacontroller.callbacks.Callback;
import com.wally.wally.datacontroller.utils.SerializableLatLng;

import java.util.List;

public interface AdfService {

    /**
     * Download Adf file using provided UUID
     *
     * @param path     path to write downloaded file
     * @param uuid     filter param
     * @param callback callback with success/error result
     */
    void download(String path, String uuid, Callback<Void> callback);

    /**
     * Searches for adfMetaData near location.
     *
     * @param location filter parameter
     * @param callback callback with result
     */
    void searchNearLocation(SerializableLatLng location, Callback<List<AdfInfo>> callback);


    /**
     *
     * @deprecated use {@link #upload(String, AdfInfo)} instead.
     */
    @Deprecated
    void upload(String path, AdfInfo info, Callback<Void> callback);

    /**
     * Uploads adf with it's meta data to server
     *
     * @param path        path of adf on local disk
     * @param info Adf data that contains uuid, name and location
     */
    void upload(String path, AdfInfo info);
}