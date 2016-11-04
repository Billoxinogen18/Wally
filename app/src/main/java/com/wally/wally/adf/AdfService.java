package com.wally.wally.adf;

import java.util.List;

public interface AdfService {
    void upload(AdfInfo info);

    void download(AdfInfo info, AdfDownloadListener listener);

    void delete(AdfInfo info);

    void searchNearLocation(double lat, double lng, SearchResultListener listener);

    interface SearchResultListener {
        void onSearchResult(List<AdfInfo> infoList);
    }

    interface AdfDownloadListener {
        void onSuccess();
        void onFail(Exception e);
    }
}