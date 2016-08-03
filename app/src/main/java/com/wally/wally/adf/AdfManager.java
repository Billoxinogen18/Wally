package com.wally.wally.adf;

import android.util.Log;

import com.wally.wally.Utils;
import com.wally.wally.datacontroller.utils.SerializableLatLng;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AdfManager {
    public static final String TAG = AdfManager.class.getSimpleName();
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

    public void getAdf(final AdfManagerStateListener listener){
        if (!queue.hasNext()) {
            listener.onNoMoreAdfs();
            queue = metadatas.iterator();
            return;
        }
        final AdfInfo info = queue.next();
        final String path = Utils.getAdfFilePath(info.getUuid());
        info.withPath(path).withUploadedStatus(true);
        adfService.download(info, new AdfService.AdfDownloadListener() {
            @Override
            public void onSuccess() {
                listener.onAdfReady(info);
            }
            @Override // try download next adf
            public void onFail(Exception e) {
                Log.d(TAG, "Download of adf with uuid " + info.getUuid() + " failed");
                getAdf(listener);
            }
        });
    }

    public static void createWithLocation(SerializableLatLng location, final AdfService adfService,
                                          final Utils.Callback<AdfManager> callback){
        adfService.searchNearLocation(location, new AdfService.SearchResultListener() {
            @Override
            public void onSearchResult(List<AdfInfo> infoList) {
                AdfManager adfManager = new AdfManager(adfService);
                for (AdfInfo i : infoList) {
                    adfManager.addAdfInfo(i);
                }
                callback.onResult(adfManager);
            }
        });
    }

    interface AdfManagerStateListener {
        void onAdfReady(AdfInfo info);
        void onNoMoreAdfs();
    }
}