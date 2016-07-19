package com.wally.wally.components;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.wally.wally.R;

/**
 * Created by Meravici on 6/23/2016.
 */
public class MapWindowAdapter implements MapboxMap.InfoWindowAdapter {
    private Context context = null;

    public MapWindowAdapter(Context context) {
        this.context = context;
    }

    // Hack to prevent info window from displaying: use a 0dp/0dp frame
    @Override
    public View getInfoWindow(Marker marker) {
        return LayoutInflater.from(context).inflate(R.layout.no_info_window, null);
    }
}
