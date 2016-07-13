package com.wally.wally.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.model.LatLng;
import com.wally.wally.R;
import com.wally.wally.datacontroller.content.Content;

/**
 * Created by shota on 5/21/16.
 */
public class CameraARStandardActivity extends CameraARActivity {

    public static Intent newIntent(Context context) {
        return new Intent(context, CameraARStandardActivity.class);
    }

    @Override
    public void onDeleteContent(Content selectedContent) {

    }

    @Override
    public void onSaveContent(Content selectedContent) {

    }

    @Override
    public void onContentCreated(Content contentCreated, boolean isEditMode) {
        saveActiveContent(contentCreated);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View mSurfaceView = findViewById(R.id.rajawali_surface);
        ((ViewGroup) mSurfaceView.getParent()).removeView(mSurfaceView); //TODO should not delete rajawali
    }

    @Override
    protected void onLocationAvailable(LatLng location) {

    }
}
