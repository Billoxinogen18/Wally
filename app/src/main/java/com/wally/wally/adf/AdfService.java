package com.wally.wally.adf;

import com.wally.wally.datacontroller.utils.SerializableLatLng;

import java.util.List;

public interface AdfService {
    void upload(AdfInfo info);

    void download(AdfInfo info, AdfDownloadListener listener);

    void searchNearLocation(SerializableLatLng location, SearchResultListener listener);

    interface SearchResultListener {
        void onSearchResult(List<AdfInfo> infoList);
    }

    interface AdfDownloadListener {
        void onSuccess();
        void onFail(Exception e);
    }
}