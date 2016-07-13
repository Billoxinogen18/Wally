package com.wally.wally.adfCreator;

import com.google.android.gms.maps.model.LatLng;
import com.wally.wally.Utils;
import com.wally.wally.datacontroller.adf.ADFService;
import com.wally.wally.datacontroller.callbacks.Callback;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Lazy retrieval of ADF files from the cloud.
 * Created by shota on 7/11/16.
 */
public class AdfManager {
    private CountDownLatch latch;
    private List<String> uuids;
    private ADFService adfService;
    private AdfInfo current;

    public AdfManager(ADFService adfService) {
        this.adfService = adfService;
        this.uuids = new LinkedList<>();
    }

    public AdfManager withUuids(List<String> uuids) {
        this.uuids.addAll(uuids);
        downloadNext();
        return this;
    }

    public boolean hasAdf(){
        return uuids.size() > 0;
    }

    public boolean isAdfReady(){
        return latch.getCount() == 0;
    }

    public AdfInfo getAdf(){
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        AdfInfo result = new AdfInfo(current);
        downloadNext();
        return result;
    }

    private void downloadNext() {
        latch = new CountDownLatch(1);
        final String uuid = uuids.get(0);
        final String path = Utils.getAdfFilePath(uuid);
        adfService.download(path, uuid, new Callback<Void>() {
            @Override
            public void onResult(Void result) {
                current = new AdfInfo().withUuid(uuid).withPath(path);
                latch.countDown();
            }

            @Override
            public void onError(Exception e) {
                // TODO leave empty for now
                // Warning: if we somehow get here
                // No more adf will be downloaded anymore
            }
        });
    }

    public void startWithLocation(LatLng location) {
    }
}
