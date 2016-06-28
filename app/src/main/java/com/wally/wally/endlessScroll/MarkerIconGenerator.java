package com.wally.wally.endlessScroll;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;

import com.google.maps.android.ui.IconGenerator;
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
    private Map<String, Bitmap> cache;
    private Map<Integer, Bitmap> defaultCache;
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

            mMarkerGeneratorTask = new AsyncTask<Void, Void, Bitmap>() {

                @Override
                protected Bitmap doInBackground(Void... voids) {
                    IconGenerator iconGenerator = new IconGenerator(context);
                    iconGenerator.setTextAppearance(R.style.Bubble_TextAppearance_Light);

                    if (isCancelled()) {
                        return null;
                    }
                    int color = COLORS[visibility];
                    iconGenerator.setColor(color);

                    Bitmap icon = iconGenerator.makeIcon(name);
                    cache.put(name, icon);

                    return icon;
                }

                @Override
                protected void onPostExecute(Bitmap icon) {
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
            Bitmap bitmap;

            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);

            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);

            defaultCache.put(visibility, bitmap);

            markerIconGenerateListener.onMarkerIconGenerate(bitmap);
        }
    }

    public interface MarkerIconGenerateListener {
        void onMarkerIconGenerate(Bitmap icon);
    }
}
