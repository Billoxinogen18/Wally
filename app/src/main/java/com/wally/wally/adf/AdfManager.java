package com.wally.wally.adf;

import com.wally.wally.Utils;
import com.wally.wally.datacontroller.callbacks.Callback;
import com.wally.wally.datacontroller.utils.SerializableLatLng;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AdfManager {
    private ADFService adfService;
    private List<AdfMetaData> metadatas;
    private Iterator<AdfMetaData> queue;

    public AdfManager(ADFService adfService) {
        this.adfService = adfService;
        this.metadatas = new ArrayList<>();
        this.queue = metadatas.iterator();
    }

    private void addMetadata(AdfMetaData d) {
        metadatas.add(d);
        queue = metadatas.iterator();
    }

    public void getAdf(final Callback<AdfInfo> callback){
        if (!queue.hasNext()) {
            callback.onResult(null);
            queue = metadatas.iterator();
            return;
        }
        final AdfMetaData metadata = queue.next();
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

    public static void createWithLocation(SerializableLatLng location, final ADFService adfService,
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