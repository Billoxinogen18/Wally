package com.wally.wally.controllers.map;

import android.content.Context;
import android.graphics.Color;

import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.MarkerView;
import com.mapbox.mapboxsdk.annotations.MarkerViewOptions;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.wally.wally.Utils;
import com.wally.wally.datacontroller.content.Content;

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
            markerNameVisibility.remove();
        }
        mMarkerList.clear();
    }


    public void selectMarkerAt(final int position) {
        if (position < 0 || position >= mMarkerList.size()) return;

//        final MarkerNameVisibility oldSelect = mMarkerList.get(mSelectedMarker);
//        final MarkerNameVisibility newSelect = mMarkerList.get(position);
//
//
//        mMarkerIconGenerator.getDefaultMarkerIcon(oldSelect.visibility,
//                oldSelect.isAnonymous,
//                oldSelect.isNoPreviewVisible,
//                new MarkerIconGenerator.MarkerIconGenerateListener() {
//            @Override
//            public void onMarkerIconGenerate(Icon icon) {
//                oldSelect.markerBase.setIcon(icon);
//            }
//        });

//        mMarkerIconGenerator.getEnumeratedMarkerIcon(newSelect.name, newSelect.visibility, new MarkerIconGenerator.MarkerIconGenerateListener() {
//            @Override
//            public void onMarkerIconGenerate(Icon icon) {
//
//                newSelect.markerBase.setIcon(icon);

        mSelectedMarker = position;

//                mMap.getMarkerViewManager().select(newSelect.markerBase);

        hideObsoleteMarkers(position);
//            }
//        });
    }

    private void hideObsoleteMarkers(int position) {
        for (int i = position - LIMIT; i >= 0; i--) {
            mMarkerList.get(i).hide();
        }

        for (int i = position + LIMIT; i < mMarkerList.size(); i++) {
            mMarkerList.get(i).hide();
        }

        for (int i = position - LIMIT; i < position + LIMIT; i++) {
            if (i >= 0 && i < mMarkerList.size()) {
                mMarkerList.get(i).show();
            }
        }
    }


    public void addMarker(String name, Content content, OnMarkerAddListener callback) {
        MarkerNameVisibility markerNameVisibility = new MarkerNameVisibility(name, content);

        markerNameVisibility.createMarker(callback);
    }

    public List<MarkerView> getVisibleMarkers() {
        List<MarkerView> res = new ArrayList<>();
        for (MarkerNameVisibility markerNameVisibility : mMarkerList) {
            res.add(markerNameVisibility.markerBase);
        }
        return res;

    }

    public interface OnMarkerAddListener {
        void onMarkerAdd();
    }

    private class MarkerNameVisibility {

        public MarkerView markerBase;
        public MarkerView markerNumber;

        public String name;
        public Content content;


        public MarkerNameVisibility(String name, Content content) {
            this.name = name;
            this.content = content;
        }


        public void createMarker(final OnMarkerAddListener callback) {
            mMarkerIconGenerator.getDefaultMarkerIcon(
                    content.getVisibility().getSocialVisibility().getMode(),
                    content.getVisibility().isAuthorAnonymous(),
                    !content.getVisibility().isPreviewVisible(),
                    new MarkerIconGenerator.MarkerIconGenerateListener() {
                        @Override
                        public void onMarkerIconGenerate(final Icon icon) {
                            MarkerViewOptions baseMarkerOptions = new MarkerViewOptions()
                                    .position(Utils.serializableLatLngToLatLng(content.getLocation()))
                                    .icon(icon)
                                    .anchor(0.5f, 0.5f);

                            mMap.getMarkerViewManager().update();

                            markerBase = mMap.addMarker(baseMarkerOptions);

                            int contentColor = content.getColor() == null ? Color.WHITE : content.getColor();
                            mMarkerIconGenerator.getEnumeratedMarkerIcon(name, contentColor,
                                    new MarkerIconGenerator.MarkerIconGenerateListener() {
                                        @Override
                                        public void onMarkerIconGenerate(Icon icon) {
                                            MarkerViewOptions markerOptions = new MarkerViewOptions()
                                                    .position(Utils.serializableLatLngToLatLng(content.getLocation()))
                                                    .icon(icon)
                                                    .anchor(0.5f, 1f);

                                            markerNumber = mMap.addMarker(markerOptions);
                                            mMarkerList.add(MarkerNameVisibility.this);
                                            callback.onMarkerAdd();
                                        }
                                    });
                        }
                    });
        }


        public void hide() {
            if (markerBase != null)
                markerBase.setVisible(false);
            if (markerNumber != null)
                markerNumber.setVisible(false);
        }

        public void show() {
            if (markerBase != null)
                markerBase.setVisible(true);
            if (markerNumber != null)
                markerNumber.setVisible(true);
        }

        public void remove() {
            mMap.removeMarker(markerBase);
            mMap.removeMarker(markerNumber);
        }
    }
}
