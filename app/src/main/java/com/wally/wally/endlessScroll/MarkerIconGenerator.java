package com.wally.wally.endlessScroll;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;

import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.wally.wally.R;
import com.wally.wally.Utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Meravici on 6/20/2016. yea
 */
public class MarkerIconGenerator {
    private Context context;

    private AsyncTask mMarkerGeneratorTask;
    private Map<String, Icon> cache;
    private Map<Integer, Icon> defaultCache;
    private final int[] COLORS;

    public MarkerIconGenerator(Context context) {
        this.context = context;

        COLORS = new int[]{
                ContextCompat.getColor(context, R.color.public_content_marker_color),
                ContextCompat.getColor(context, R.color.private_content_marker_color),
                ContextCompat.getColor(context, R.color.people_content_marker_color)
        };

        cache = new HashMap<>();
        defaultCache = new HashMap<>();
    }

    public synchronized void getEnumeratedMarkerIcon(final String name, final int visibility, final MarkerIconGenerateListener markerIconGenerateListener) {
        if (cache.containsKey(name)) {
            markerIconGenerateListener.onMarkerIconGenerate(cache.get(name));
        } else {
            if (mMarkerGeneratorTask != null) {
                mMarkerGeneratorTask.cancel(true);
            }

            mMarkerGeneratorTask = new AsyncTask<Void, Void, Icon>() {

                @Override
                protected Icon doInBackground(Void... voids) {
//                    IconGenerator iconGenerator = new IconGenerator(context);
//                    iconGenerator.setTextAppearance(R.style.Bubble_TextAppearance_Light);
//
//                    if (isCancelled()) {
//                        return null;
//                    }
//                    int color = COLORS[visibility];
//                    iconGenerator.setColor(color);
//
//                    Bitmap icon = iconGenerator.makeIcon(name);
//                    cache.put(name, icon);

                    return IconFactory.getInstance(context).fromDrawable(ContextCompat.getDrawable(context, R.drawable.map_marker));
                }

                @Override
                protected void onPostExecute(Icon icon) {
                    super.onPostExecute(icon);
                    if (icon == null) {
                        return;
                    }
                    markerIconGenerateListener.onMarkerIconGenerate(icon);
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }


    public void getDefaultMarkerIcon(int visibility, MarkerIconGenerateListener markerIconGenerateListener) {
        if(defaultCache.containsKey(visibility)){
            markerIconGenerateListener.onMarkerIconGenerate(defaultCache.get(visibility));
        }else {
            Drawable drawable = ContextCompat.getDrawable(context, R.drawable.ic_marker_dot);
            drawable = Utils.tintDrawable(drawable, COLORS[visibility]);
            Icon icon = IconFactory.getInstance(context).fromDrawable(drawable);
            defaultCache.put(visibility, icon);

            markerIconGenerateListener.onMarkerIconGenerate(icon);
        }
    }

    public interface MarkerIconGenerateListener {
        void onMarkerIconGenerate(Icon icon);
    }
}
