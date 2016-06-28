package com.wally.wally.datacontroller.adf;

import com.google.android.gms.maps.model.LatLng;
import com.google.atap.tangoservice.TangoAreaDescriptionMetaData;
import com.wally.wally.LocationConverter;

import java.io.Serializable;
import java.nio.ByteBuffer;

/**
 * Stores information about Adf Sinchronization.
 * If it's local or cloud adf
 * Or is a synchronized
 * Created by ioane5 on 6/25/16.
 */
public class AdfSyncInfo implements Serializable {

    private AdfMetaData adfMetaData;
    private boolean isLocal;
    private boolean isSynchronized;

    public AdfSyncInfo(AdfMetaData adfMetaData, boolean isLocal) {
        this.adfMetaData = adfMetaData;
        this.isLocal = isLocal;
    }

    public static AdfSyncInfo fromMetadata(TangoAreaDescriptionMetaData metadata) {
        String uuid = new String(metadata.get(TangoAreaDescriptionMetaData.KEY_UUID));
        String name = new String(metadata.get(TangoAreaDescriptionMetaData.KEY_NAME));

        byte[] rawLoc = metadata.get(TangoAreaDescriptionMetaData.KEY_TRANSFORMATION);
        double[] ecef = new double[7];
        ByteBuffer wrapped = ByteBuffer.wrap(rawLoc);
        for (int i = 0; i < 7; i++) {
            ecef[i] = wrapped.getDouble(i);
        }
        double[] lla = LocationConverter.ecefToLla(ecef);
        LatLng loc = new LatLng(lla[0], lla[1]);

        return new AdfSyncInfo(new AdfMetaData(name, uuid, loc), true);
    }

    public boolean isLocal() {
        return isLocal;
    }

    public void setIsLocal(boolean isLocal) {
        this.isLocal = isLocal;
    }

    public boolean isSynchronized() {
        return isSynchronized;
    }

    public AdfMetaData getAdfMetaData() {
        return adfMetaData;
    }

    /**
     * @param isSynchronized true if is on cloud and local storage together
     */
    public void setIsSynchronized(boolean isSynchronized) {
        this.isSynchronized = isSynchronized;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AdfSyncInfo syncInfo = (AdfSyncInfo) o;

        return adfMetaData.equals(syncInfo.adfMetaData);
    }

    @Override
    public int hashCode() {
        return adfMetaData.hashCode();
    }

    @Override
    public String toString() {
        return "AdfSyncInfo{" +
                "adfMetaData=" + adfMetaData +
                ", isLocal=" + isLocal +
                ", isSynchronized=" + isSynchronized +
                '}';
    }
}
