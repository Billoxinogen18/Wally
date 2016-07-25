package com.wally.wally.adfCreator;

import android.util.Log;

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
    private static final String TAG = AdfManager.class.getSimpleName();

    //private List<String> uuids;
    private List<AdfMetaData> metadatas;
    private ADFService adfService;
    private LinkedList<AdfInfo> cache;
    private Callback<AdfInfo> callback;

    public AdfManager(ADFService adfService) {
        this.adfService = adfService;
      //  this.uuids = new LinkedList<>();
        this.cache = new LinkedList<>();
        this.metadatas = new LinkedList<>();
    }

//    public void addUuid(String uuid) {
//        uuids.add(uuid);
//    }
    private void addMetadata(AdfMetaData d) {metadatas.add(d);}

    public void getAdf(Callback<AdfInfo> callback){
        Log.d(TAG, "getAdf() called with: " + metadatas.size());
        if (metadatas.size() < 1) {
            callback.onResult(null);
            return;
        }

        if (this.callback != null) {
            this.callback.onError(new Exception("getAdf called twice"));
        }

        if (cache.size() > 0) {
            callback.onResult(cache.remove());
            downloadNext();
        } else {
            this.callback = callback;
        }
    }

    private void downloadNext() {
        if (metadatas.size() < 1 || cache.size() > 1) return;
        final AdfMetaData metadata = metadatas.get(0);
        final String uuid = metadata.getUuid();
        final String path = Utils.getAdfFilePath(uuid);
        adfService.download(path, uuid, new Callback<Void>() {
            @Override
            public void onResult(Void result) {
                cache.add(new AdfInfo().withUuid(uuid).withPath(path).withUploaded(true).withMetaData(metadata));
                metadatas.remove(0);
                if (callback != null) {
                    callback.onResult(cache.remove());
                    callback = null;
                }
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
                callback = null;
            }
        });
    }

    public static void createWithLocation(LatLng location, final ADFService adfService, final Callback<AdfManager> callback){
        adfService.searchADfMetaDataNearLocation(location, new Callback<List<AdfMetaData>>() {
            @Override
            public void onResult(List<AdfMetaData> result) {
                AdfManager adfManager = new AdfManager(adfService);
                for (AdfMetaData d : result) {
                    //adfManager.addUuid(d.getUuid());
                    adfManager.addMetadata(d);
                }
                adfManager.downloadNext();
                callback.onResult(adfManager);
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }


}
