package com.wally.wally.adfCreator;

import com.google.android.gms.maps.model.LatLng;
import com.wally.wally.Utils;
import com.wally.wally.datacontroller.adf.ADFService;
import com.wally.wally.datacontroller.adf.AdfMetaData;
import com.wally.wally.datacontroller.callbacks.Callback;

import java.util.LinkedList;
import java.util.List;

public class AdfManager {
    private ADFService adfService;
    private List<AdfMetaData> metadatas;

    public AdfManager(ADFService adfService) {
        this.adfService = adfService;
        this.metadatas = new LinkedList<>();
    }

    private void addMetadata(AdfMetaData d) {
        metadatas.add(d);
    }

    public void getAdf(final Callback<AdfInfo> callback){
        if (metadatas.size() < 1) {
            callback.onResult(null);
            return;
        }
        final AdfMetaData metadata = metadatas.remove(0);
        final String uuid = metadata.getUuid();
        final String path = Utils.getAdfFilePath(uuid);
        adfService.download(path, uuid, new Callback<Void>() {
            @Override
            public void onResult(Void result) {
                AdfInfo info = new AdfInfo()
                        .withUuid(uuid)
                        .withPath(path)
                        .withUploaded(true)
                        .withMetaData(metadata);
                callback.onResult(info);
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }

    public static void createWithLocation(LatLng location, final ADFService adfService,
                                          final Callback<AdfManager> callback){
        adfService.searchADfMetaDataNearLocation(location, new Callback<List<AdfMetaData>>() {
            @Override
            public void onResult(List<AdfMetaData> result) {
                AdfManager adfManager = new AdfManager(adfService);
                for (AdfMetaData d : result) {
                    adfManager.addMetadata(d);
                }
                callback.onResult(adfManager);
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }
}