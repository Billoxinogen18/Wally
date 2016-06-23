package com.wally.wally.datacontroller.adf;

import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.wally.wally.datacontroller.callbacks.Callback;

/**
 * Adf Cloud service interface
 * Created by shota on 6/20/16.
 */
public interface ADFCloudService {
    /**
     * Makes estimation and returns most suitable ADF URI
     * </br>
     *
     * @param location user location
     * @param callback returns ADF path on disk
     */
    void downloadADf(@NonNull LatLng location, final Callback<String> callback);

    /**
     * User creates and uploads ADF on server
     * </br>
     *
     * @param adfFilePath ADF file path on disk
     * @param location    ADF location
     */
    void uploadADF(@NonNull String adfFilePath, @NonNull LatLng location);
}
