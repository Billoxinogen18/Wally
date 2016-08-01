package com.wally.wally.adf;

import com.wally.wally.datacontroller.callbacks.Callback;
import com.wally.wally.datacontroller.utils.SerializableLatLng;

import java.util.List;

public interface AdfService {
    void upload(AdfInfo info);

    void download(AdfInfo info, Callback<Void> callback);

    void searchNearLocation(SerializableLatLng location, Callback<List<AdfInfo>> callback);
}