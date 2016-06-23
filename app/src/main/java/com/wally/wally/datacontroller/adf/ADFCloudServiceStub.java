package com.wally.wally.datacontroller.adf;

import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.wally.wally.datacontroller.callbacks.Callback;

/**
 * This class is just for testing, should be deleted later
 *
 * Created by shota on 6/20/16.
 */
public class ADFCloudServiceStub implements ADFCloudService {

    @Override
    public void downloadADf(@NonNull LatLng location, final Callback<String> callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                callback.onResult("dfd");
            }
        }).start();
    }

    @Override
    public void uploadADF(@NonNull String adfFilePath, @NonNull LatLng location) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
