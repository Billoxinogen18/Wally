package com.wally.wally.components;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.wally.wally.R;

/**
 * Created by Meravici on 6/23/2016.
 */
public class MapWindowAdapter implements GoogleMap.InfoWindowAdapter {
    private Context context = null;

    public MapWindowAdapter(Context context) {
        this.context = context;
    }

    // Hack to prevent info window from displaying: use a 0dp/0dp frame
    @Override
    public View getInfoWindow(Marker marker) {
        return LayoutInflater.from(context).inflate(R.layout.no_info_window, null);
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }
}
