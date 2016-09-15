package com.wally.wally.controllers.map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.annotation.DrawableRes;
import android.support.v4.content.ContextCompat;

import com.google.maps.android.ui.IconGenerator;
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
    private final static int ANONYMOUS_DRAWABLE = 3;
    private final static int NO_PREVIEW_DRAWABLE = 4;
    private final int[] DRAWABLES;
    private Context context;
    private AsyncTask mMarkerGeneratorTask;
    private Map<String, Icon> cache;
    private Map<Integer, Icon> defaultCache;

    public MarkerIconGenerator(Context context) {
        this.context = context;

        DRAWABLES = new int[]{
                R.drawable.public_content_marker,
                R.drawable.private_content_marker,
                R.drawable.friends_content_marker,
                R.drawable.anonymous_content_marker,
                R.drawable.no_preview_content_marker
        };

        cache = new HashMap<>();
        defaultCache = new HashMap<>();
    }

    public synchronized void getEnumeratedMarkerIcon(final String name, final int color, final MarkerIconGenerateListener markerIconGenerateListener) {
        if (cache.containsKey(name)) {
            markerIconGenerateListener.onMarkerIconGenerate(cache.get(name));
        } else {
//            if (mMarkerGeneratorTask != null) {
//                mMarkerGeneratorTask.cancel(true);
//            }

            mMarkerGeneratorTask = new AsyncTask<Void, Void, Icon>() {

                @Override
                protected Icon doInBackground(Void... voids) {
                    IconGenerator iconGenerator = new IconGenerator(context);
                    iconGenerator.setTextAppearance(Utils.isColorDark(color) ? R.style.Bubble_TextAppearance_Light : R.style.Bubble_TextAppearance_Dark);

                    if (isCancelled()) {
                        return null;
                    }
                    iconGenerator.setColor(color);


                    Bitmap bitmap = iconGenerator.makeIcon(name);
                    Icon icon = IconFactory.getInstance(context).fromBitmap(bitmap);
                    cache.put(name, icon);

                    return icon;
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


    public void getDefaultMarkerIcon(int visibility, boolean isAnonymous,
                                     boolean isNoPreviewVisible,
                                     MarkerIconGenerateListener markerIconGenerateListener) {
        visibility = isAnonymous ? ANONYMOUS_DRAWABLE : isNoPreviewVisible ? NO_PREVIEW_DRAWABLE : visibility;
        if (defaultCache.containsKey(visibility)) {
            markerIconGenerateListener.onMarkerIconGenerate(defaultCache.get(visibility));
        } else {
            Drawable drawable = ContextCompat.getDrawable(context, DRAWABLES[visibility]);
            Icon icon = IconFactory.getInstance(context).fromDrawable(drawable);
            defaultCache.put(visibility, icon);

            markerIconGenerateListener.onMarkerIconGenerate(icon);
        }
    }

    public void getSolvedPuzzleMarker(String solvedMarkerURL, final String name, final MarkerIconGenerateListener markerCallback) {
        getPuzzleMarker(solvedMarkerURL, R.drawable.ic_marker_puzzle_solved, name, markerCallback);
    }

    public void getUnsolvedPuzzleMarker(String unsolvedMarkerURL, String name, MarkerIconGenerateListener markerCallback) {
        getPuzzleMarker(unsolvedMarkerURL, R.drawable.ic_marker_puzzle_unsolved, name, markerCallback);
    }

    private void getPuzzleMarker(final String resURL, final @DrawableRes int defaultResource, final String name, final MarkerIconGenerateListener markerCallback) {
        new AsyncTask<Void, Void, Icon>() {

            @Override
            protected Icon doInBackground(Void... voids) {
                // TODO add url
                Drawable drawable = ContextCompat.getDrawable(context, defaultResource);

                IconGenerator iconGenerator = new IconGenerator(context);
                iconGenerator.setBackground(drawable);
                iconGenerator.setTextAppearance(R.style.Bubble_TextAppearance_Dark);
                int padding = Utils.dpToPx(context, 20);
                iconGenerator.setContentPadding(padding, padding, padding, padding);
                Bitmap bitmap = iconGenerator.makeIcon(name);
                Icon icon = IconFactory.getInstance(context).fromBitmap(bitmap);
                cache.put(name, icon);
                return icon;
            }

            @Override
            protected void onPostExecute(Icon icon) {
                super.onPostExecute(icon);
                if (icon == null) {
                    return;
                }
                markerCallback.onMarkerIconGenerate(icon);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public interface MarkerIconGenerateListener {
        void onMarkerIconGenerate(Icon icon);
    }
}
