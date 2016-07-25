package com.wally.wally.endlessScroll;

import android.content.Context;

import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.MarkerView;
import com.mapbox.mapboxsdk.annotations.MarkerViewOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Meravici on 6/23/2016. yea
 */
public class MarkerManager {
    private static final int LIMIT = 15;
    private List<MarkerNameVisibility> mMarkerList;

    private MarkerIconGenerator mMarkerIconGenerator;
    private MapboxMap mMap;

    private int mSelectedMarker = 0;

    public MarkerManager(Context context, MapboxMap map) {
        mMarkerIconGenerator = new MarkerIconGenerator(context);
        mMap = map;
        mMarkerList = new ArrayList<>();
    }

    public void reset() {
        mSelectedMarker = 0;
        for (MarkerNameVisibility markerNameVisibility : mMarkerList) {
            mMap.removeMarker(markerNameVisibility.marker);
        }
        mMarkerList.clear();
    }


    public void selectMarkerAt(final int position) {
        if (position < 0 || position >= mMarkerList.size()) return;

        final MarkerNameVisibility oldSelect = mMarkerList.get(mSelectedMarker);
        final MarkerNameVisibility newSelect = mMarkerList.get(position);


        mMarkerIconGenerator.getDefaultMarkerIcon(oldSelect.visibility,
                oldSelect.isAnonymous,
                oldSelect.isNoPreviewVisible,
                new MarkerIconGenerator.MarkerIconGenerateListener() {
            @Override
            public void onMarkerIconGenerate(Icon icon) {
                oldSelect.marker.setIcon(icon);
                oldSelect.markerOptions.icon(icon);

//                MarkerOptions markerOptions = new MarkerOptions()
//                        .position(oldSelect.marker.getPosition())
//                        .icon(iconFactory.fromBitmap(icon));
////                        .anchor(0.5f, 1f);
//
//                Marker marker = mMap.addMarker(markerOptions);
//
//                mMap.removeMarker(oldSelect.marker);
//                oldSelect.markerOptions = markerOptions;
//                oldSelect.marker = marker;
            }
        });

        mMarkerIconGenerator.getEnumeratedMarkerIcon(newSelect.name, newSelect.visibility, new MarkerIconGenerator.MarkerIconGenerateListener() {
            @Override
            public void onMarkerIconGenerate(Icon icon) {

                newSelect.marker.setIcon(icon);
                newSelect.markerOptions.icon(icon);

//                MarkerOptions markerOptions = new MarkerOptions()
//                        .position(newSelect.marker.getPosition())
//                        .icon(iconFactory.fromBitmap(icon));
////                        .anchor(0.5f, 1f);
//
//                Marker marker = mMap.addMarker(markerOptions);
//                mMap.removeMarker(newSelect.marker);
//                newSelect.markerOptions = markerOptions;
//                newSelect.marker = marker;
                mSelectedMarker = position;

                mMap.getMarkerViewManager().select(newSelect.marker);

                hideObsoleteMarkers(position);
            }
        });
    }

    private void hideObsoleteMarkers(int position) {
        for (int i = position - LIMIT; i >= 0; i--) {
            mMap.removeMarker(mMarkerList.get(i).marker);
            mMarkerList.get(i).isAdded = false;
        }

        for (int i = position + LIMIT; i < mMarkerList.size(); i++) {
            mMap.removeMarker(mMarkerList.get(i).marker);
            mMarkerList.get(i).isAdded = false;
        }

        for (int i = position - LIMIT; i < position + LIMIT; i++) {
            if (i >= 0 && i < mMarkerList.size()) {
                if(!mMarkerList.get(i).isAdded) {
                    mMarkerList.get(i).marker = mMap.addMarker(mMarkerList.get(i).markerOptions);
                    mMarkerList.get(i).isAdded = true;
                }
            }
        }
    }


    public void addMarker(final String name, final int visibility, final boolean isAnonymous, final boolean isNoPreviewVisible, final LatLng location) {
        mMarkerIconGenerator.getDefaultMarkerIcon(visibility,
                isAnonymous,
                isNoPreviewVisible,
                new MarkerIconGenerator.MarkerIconGenerateListener() {
            @Override
            public void onMarkerIconGenerate(Icon icon) {
                MarkerViewOptions markerOptions = new MarkerViewOptions()
                        .position(location)
                        .icon(icon);
                // TODO .anchor(1, 0.5f);

                MarkerView marker = mMap.addMarker(markerOptions);

                MarkerNameVisibility markerNameVisibility = new MarkerNameVisibility();
                markerNameVisibility.markerOptions = markerOptions;
                markerNameVisibility.marker = marker;
                markerNameVisibility.name = name;
                markerNameVisibility.visibility = visibility;
                markerNameVisibility.isAnonymous = isAnonymous;
                markerNameVisibility.isNoPreviewVisible = isNoPreviewVisible;

                markerNameVisibility.isAdded = true;
                mMarkerList.add(markerNameVisibility);

                if (mMarkerList.size() == 1) {
                    selectMarkerAt(0);
                }
            }
        });
    }

    public List<MarkerView> getVisibleMarkers() {
        List<MarkerView> res = new ArrayList<>();
        for (MarkerNameVisibility markerNameVisibility : mMarkerList) {
            res.add(markerNameVisibility.marker);
        }
        return res;

    }

    private class MarkerNameVisibility {
        public boolean isAdded;
        public MarkerView marker;
        public MarkerViewOptions markerOptions;
        public int visibility;
        public String name;
        public boolean isAnonymous;
        public boolean isNoPreviewVisible;
    }

}
