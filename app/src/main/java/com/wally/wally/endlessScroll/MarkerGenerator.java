package com.wally.wally.endlessScroll;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;

import com.google.maps.android.ui.IconGenerator;
import com.wally.wally.R;
import com.wally.wally.datacontroller.content.Content;
import com.wally.wally.datacontroller.content.Visibility;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Xato on 6/20/2016.
 */
public abstract class MarkerGenerator extends AsyncTask<Void, Void, List<Bitmap>> {
    private final int startIndex;
    private Context context;
    private List<Content> contents;

    public MarkerGenerator(Context context, List<Content> contents, int startIndex) {
        this.contents = contents;
        this.context = context;
        this.startIndex = startIndex;
    }

    @Override
    protected List<Bitmap> doInBackground(Void... params) {

        List<Bitmap> icons = new ArrayList<>(contents.size());
        IconGenerator iconGenerator = new IconGenerator(context);
        iconGenerator.setTextAppearance(R.style.Bubble_TextAppearance_Light);

        int[] colors = new int[3];
        colors[Visibility.SocialVisibility.PRIVATE] = ContextCompat.getColor(context, R.color.private_content_marker_color);
        colors[Visibility.SocialVisibility.PUBLIC] = ContextCompat.getColor(context, R.color.public_content_marker_color);
        colors[Visibility.SocialVisibility.PEOPLE] = ContextCompat.getColor(context, R.color.people_content_marker_color);
        for (int i = 0; i < contents.size(); i++) {
            if (isCancelled()) {
                return null;
            }
            Visibility visibility = contents.get(i).getVisibility();
            int color = colors[visibility.getSocialVisibility().getMode()];
            iconGenerator.setColor(color);
            icons.add(iconGenerator.makeIcon("" + (startIndex + i)));
        }
        return icons;
    }

    @Override
    protected abstract void onPostExecute(List<Bitmap> markerIcons);
}
