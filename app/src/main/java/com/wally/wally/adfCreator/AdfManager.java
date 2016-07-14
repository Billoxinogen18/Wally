package com.wally.wally.adfCreator;

import com.google.android.gms.maps.model.LatLng;
import com.wally.wally.Utils;
import com.wally.wally.datacontroller.adf.ADFService;
import com.wally.wally.datacontroller.adf.AdfMetaData;
import com.wally.wally.datacontroller.callbacks.Callback;

import java.util.LinkedList;
import java.util.List;

/**
 * Lazy retrieval of ADF files from the cloud.
 */
public class AdfManager {
    private List<String> uuids;
    private ADFService adfService;
    private AdfInfo current;

    public AdfManager(ADFService adfService) {
        this.adfService = adfService;
        this.uuids = new LinkedList<>();
    }

    public void addUuid(String uuid) {
        uuids.add(uuid);
    }

    public boolean hasAdf(){
        return isAdfReady() || uuids.size() > 0;
    }

    public boolean isAdfReady(){
        return current != null;
    }

    public AdfInfo getAdf(){
        AdfInfo result = current;
        current = null;
        downloadNext();
        return result;
    }

    private void downloadNext() {
        if (uuids.size() < 1) return;
        final String uuid = uuids.get(0);
        final String path = Utils.getAdfFilePath(uuid);
        adfService.download(path, uuid, new Callback<Void>() {
            @Override
            public void onResult(Void result) {
                current = new AdfInfo().withUuid(uuid).withPath(path);
                uuids.remove(0);
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
        adfService.searchADfMetaDataNearLocation(location, new Callback<List<AdfMetaData>>() {
            @Override
            public void onResult(List<AdfMetaData> result) {
                for (AdfMetaData d : result) {
                    addUuid(d.getUuid());
                }
                downloadNext();
            }

            @Override
            public void onError(Exception e) {

            }
        });
    }
}
