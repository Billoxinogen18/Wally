package com.wally.wally.datacontroller;

import com.google.android.gms.maps.model.LatLng;
import com.wally.wally.datacontroller.callbacks.Callback;

public interface ADFService {

    void download(String path, String uuid, Callback<Void> callback);

    void searchUuidNearLocation(LatLng location, Callback<String> callback);

    void upload(String path, String uuid, LatLng location, Callback<Void> callback);
}