package com.wally.wally.endlessScroll;

import android.content.Context;
import android.graphics.Bitmap;

import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
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
    private IconFactory iconFactory;

    private int mSelectedMarker = 0;

    public MarkerManager(Context context, MapboxMap map) {
        mMarkerIconGenerator = new MarkerIconGenerator(context);
        iconFactory = IconFactory.getInstance(context);
        mMap = map;
        mMarkerList = new ArrayList<>();
    }

    public void reset() {
        mSelectedMarker = 0;
        for (MarkerNameVisibility markerNameVisibility : mMarkerList) {
            markerNameVisibility.markerOptions.getMarker().remove();
        }
        mMarkerList.clear();
    }


    public void selectMarkerAt(final int position) {
        if (position < 0 || position >= mMarkerList.size()) return;

        final MarkerNameVisibility oldSelect = mMarkerList.get(mSelectedMarker);
        final MarkerNameVisibility newSelect = mMarkerList.get(position);


        mMarkerIconGenerator.getDefaultMarkerIcon(oldSelect.visibility, new MarkerIconGenerator.MarkerIconGenerateListener() {
            @Override
            public void onMarkerIconGenerate(Bitmap icon) {
                MarkerOptions markerOptions = new MarkerOptions()
                        .position(oldSelect.markerOptions.getMarker().getPosition())
                        .icon(iconFactory.fromBitmap(icon));
//                        .anchor(0.5f, 1f);

                mMap.addMarker(markerOptions);

                oldSelect.markerOptions.getMarker().remove();
                oldSelect.markerOptions = markerOptions;
            }
        });

        mMarkerIconGenerator.getEnumeratedMarkerIcon(newSelect.name, newSelect.visibility, new MarkerIconGenerator.MarkerIconGenerateListener() {
            @Override
            public void onMarkerIconGenerate(Bitmap icon) {
                MarkerOptions markerOptions = new MarkerOptions()
                        .position(newSelect.markerOptions.getMarker().getPosition())
                        .icon(iconFactory.fromBitmap(icon));
//                        .anchor(0.5f, 1f);

                mMap.addMarker(markerOptions);
                newSelect.markerOptions.getMarker().remove();
                newSelect.markerOptions = markerOptions;
                mSelectedMarker = position;

                hideObsoleteMarkers(position);
            }
        });
    }

    private void hideObsoleteMarkers(int position) {
        for (int i = position - LIMIT; i >= 0; i--) {
            mMap.removeMarker(mMarkerList.get(i).markerOptions.getMarker());
        }

        for (int i = position + LIMIT; i < mMarkerList.size(); i++) {
            mMap.removeMarker(mMarkerList.get(i).markerOptions.getMarker());
        }

        for (int i = position - LIMIT; i < position + LIMIT; i++) {
            if (i >= 0 && i < mMarkerList.size())
                mMap.addMarker(mMarkerList.get(i).markerOptions);
        }
    }


    public void addMarker(final String name, final int visibility, final LatLng location) {
        mMarkerIconGenerator.getDefaultMarkerIcon(visibility, new MarkerIconGenerator.MarkerIconGenerateListener() {
            @Override
            public void onMarkerIconGenerate(Bitmap icon) {
                MarkerOptions markerOptions = new MarkerOptions()
                        .position(location)
                        .icon(iconFactory.fromBitmap(icon));
//                        .anchor(0.5f, 1);

                mMap.addMarker(markerOptions);

                MarkerNameVisibility markerNameVisibility = new MarkerNameVisibility();
                markerNameVisibility.markerOptions = markerOptions;
                markerNameVisibility.name = name;
                markerNameVisibility.visibility = visibility;
                mMarkerList.add(markerNameVisibility);

                if (mMarkerList.size() == 1) {
                    selectMarkerAt(0);
                }
            }
        });
    }

    public List<Marker> getVisibleMarkers() {
        return mMap.getMarkers();
    }

    private class MarkerNameVisibility {
        //        public Marker marker;
        public MarkerOptions markerOptions;
        public int visibility;
        public String name;
    }

}
