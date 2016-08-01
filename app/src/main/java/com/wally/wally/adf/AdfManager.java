package com.wally.wally.adf;

import com.wally.wally.Utils;
import com.wally.wally.datacontroller.callbacks.Callback;
import com.wally.wally.datacontroller.utils.SerializableLatLng;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AdfManager {
    private AdfService adfService;
    private List<AdfInfo> metadatas;
    private Iterator<AdfInfo> queue;

    public AdfManager(AdfService adfService) {
        this.adfService = adfService;
        this.metadatas = new ArrayList<>();
        this.queue = metadatas.iterator();
    }

    private void addAdfInfo(AdfInfo info) {
        metadatas.add(info);
        queue = metadatas.iterator();
    }

    public void getAdf(final Callback<AdfInfo> callback){
        if (!queue.hasNext()) {
            callback.onResult(null);
            queue = metadatas.iterator();
            return;
        }
        final AdfInfo info = queue.next();
        final String path = Utils.getAdfFilePath(info.getUuid());
        info.withPath(path).withUploadedStatus(true);
        adfService.download(info, new Callback<Void>() {
            @Override
            public void onResult(Void result) {
                callback.onResult(info);
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }

    public static void createWithLocation(SerializableLatLng location, final AdfService adfService,
                                          final Callback<AdfManager> callback){
        adfService.searchNearLocation(location, new Callback<List<AdfInfo>>() {
            @Override
            public void onResult(List<AdfInfo> result) {
                AdfManager adfManager = new AdfManager(adfService);
                for (AdfInfo i : result) {
                    adfManager.addAdfInfo(i);
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