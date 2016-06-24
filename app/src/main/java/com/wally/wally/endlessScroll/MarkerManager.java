package com.wally.wally.endlessScroll;

import android.content.Context;
import android.graphics.Bitmap;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Meravici on 6/23/2016. yea
 */
public class MarkerManager {
    private static final int LIMIT = 15;
    private List<MarkerNameVisibility> mMarkerList;

    private MarkerIconGenerator mMarkerIconGenerator;
    private GoogleMap mMap;

    private int mSelectedMarker = 0;

    public MarkerManager(Context context, GoogleMap map) {
        mMarkerIconGenerator = new MarkerIconGenerator(context);
        mMap = map;
        mMarkerList = new ArrayList<>();
    }

    public void reset() {
        mSelectedMarker = 0;
        for (MarkerNameVisibility markerNameVisibility : mMarkerList) {
            markerNameVisibility.marker.remove();
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
                Marker marker = mMap.addMarker(
                        new MarkerOptions()
                                .position(oldSelect.marker.getPosition())
                                .icon(BitmapDescriptorFactory.fromBitmap(icon))
                                .anchor(0.5f, 1f));
                oldSelect.marker.remove();
                oldSelect.marker = marker;
            }
        });

        mMarkerIconGenerator.getEnumeratedMarkerIcon(newSelect.name, newSelect.visibility, new MarkerIconGenerator.MarkerIconGenerateListener() {
            @Override
            public void onMarkerIconGenerate(Bitmap icon) {
                Marker marker = mMap.addMarker(
                        new MarkerOptions()
                                .position(newSelect.marker.getPosition())
                                .icon(BitmapDescriptorFactory.fromBitmap(icon))
                                .anchor(0.5f, 1f));
                newSelect.marker.remove();
                newSelect.marker = marker;
                mSelectedMarker = position;
                marker.showInfoWindow();

                hideObsoleteMarkers(position);
            }
        });
    }

    private void hideObsoleteMarkers(int position) {
        for(int i= position-LIMIT; i>= 0; i--){
            mMarkerList.get(i).marker.setVisible(false);
        }

        for(int i= position + LIMIT; i<mMarkerList.size(); i++){
            mMarkerList.get(i).marker.setVisible(false);
        }

        for(int i=position -LIMIT; i< position + LIMIT; i++){
            if(i >= 0 && i<mMarkerList.size())
                mMarkerList.get(i).marker.setVisible(true);
        }
    }


    public void addMarker(final String name, final int visibility, final LatLng location) {
        mMarkerIconGenerator.getDefaultMarkerIcon(visibility, new MarkerIconGenerator.MarkerIconGenerateListener() {
            @Override
            public void onMarkerIconGenerate(Bitmap icon) {
                final Marker marker = mMap.addMarker(
                        new MarkerOptions()
                                .position(location)
                                .anchor(0.5f, 1)
                                .icon(BitmapDescriptorFactory.fromBitmap(icon)));
                MarkerNameVisibility markerNameVisibility = new MarkerNameVisibility();
                markerNameVisibility.marker = marker;
                markerNameVisibility.name = name;
                markerNameVisibility.visibility = visibility;
                mMarkerList.add(markerNameVisibility);

                if (mMarkerList.size() == 1) {
                    selectMarkerAt(0);
                }
            }
        });
    }


    private class MarkerNameVisibility {
        public Marker marker;
        public int visibility;
        public String name;
    }

}
