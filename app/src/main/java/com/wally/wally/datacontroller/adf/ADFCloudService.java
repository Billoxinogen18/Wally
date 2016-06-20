package com.wally.wally.datacontroller.adf;

import com.google.android.gms.maps.model.LatLng;
import com.wally.wally.datacontroller.callbacks.Callback;

/**
 * Created by shota on 6/20/16.
 */
public interface ADFCloudService {
    public void downloadADf(LatLng location, final Callback<String> callback);
    public void uploadADF(String adfFilePath, LatLng location);
}
