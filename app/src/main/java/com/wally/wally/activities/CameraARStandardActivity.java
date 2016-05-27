package com.wally.wally.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;

import com.wally.wally.R;
import com.wally.wally.datacontroller.content.Content;

import org.rajawali3d.surface.RajawaliSurfaceView;

/**
 * Created by shota on 5/21/16.
 */
public class CameraARStandardActivity extends CameraARActivity {

    public static Intent newIntent(Context context) {
        Intent i = new Intent(context, CameraARStandardActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        return i;
    }

    @Override
    public void onDeleteContent(Content selectedContent) {

    }

    @Override
    public void onSaveContent(Content selectedContent) {

    }

    @Override
    public void onContentCreated(Content contentCreated, boolean isEditMode) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RajawaliSurfaceView mSurfaceView = (RajawaliSurfaceView) findViewById(R.id.rajawali_surface);
        ((ViewGroup) mSurfaceView.getParent()).removeView(mSurfaceView); //TODO should not delete rajawali
    }
}
