package com.wally.wally.datacontroller.adf;

import com.google.android.gms.maps.model.LatLng;
import com.wally.wally.datacontroller.callbacks.Callback;

/**
 * Created by shota on 6/20/16.
 */
public class ADFCloudServiceStub implements ADFCloudService {

    @Override
    public void downloadADf(LatLng location, Callback<String> callback) {
        callback.onResult("dfd");
    }

    @Override
    public void uploadADF(String adfFilePath, LatLng location) {

    }
}
