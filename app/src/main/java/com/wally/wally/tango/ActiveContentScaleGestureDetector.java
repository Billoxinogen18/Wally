package com.wally.wally.tango;

import android.util.Log;
import android.view.ScaleGestureDetector;

/**
 * Created by shota on 5/27/16.
 */

//TODO it might be better to implement in ActiveContent class. let's see later...
public class ActiveContentScaleGestureDetector implements ScaleGestureDetector.OnScaleGestureListener {
    private static final String TAG = ActiveContentScaleGestureDetector.class.getSimpleName();

    private VisualContentManager mVisualContentManager;


    public ActiveContentScaleGestureDetector(VisualContentManager visualContentManager){
        mVisualContentManager = visualContentManager;
    }


    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        float scale = detector.getScaleFactor() != 0 ? detector.getScaleFactor() : 1f;
        if (mVisualContentManager.getActiveContent() != null) {
            mVisualContentManager.getActiveContent().scaleContent(scale);
        } else {
            Log.e(TAG, "onScale() was called but active content is not on screen");
        }
        return true;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {

    }
}