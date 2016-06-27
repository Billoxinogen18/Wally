package com.wally.wally.datacontroller.adf;

import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.wally.wally.datacontroller.adf.AdfMetaData;
import com.wally.wally.datacontroller.callbacks.Callback;

import java.util.List;

public interface ADFService {

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
    void searchADfMetaDataNearLocation(@NonNull LatLng location, Callback<List<AdfMetaData>> callback);

    /**
     * Uploads adf with it's meta data to server
     *
     * @param path        path of adf on local disk
     * @param adfMetaData Adf data that contains uuid, name and location
     * @param callback    callback with success/error result
     */
    void upload(String path, AdfMetaData adfMetaData, Callback<Void> callback);
}