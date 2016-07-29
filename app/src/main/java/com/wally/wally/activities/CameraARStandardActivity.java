package com.wally.wally.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.wally.wally.R;
import com.wally.wally.datacontroller.content.Content;

public class CameraARStandardActivity extends CameraARActivity {

    private static final String TAG = CameraARStandardActivity.class.getSimpleName();

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
        Log.d(TAG, "onContentCreated() called with: " + "contentCreated = [" + contentCreated + "], isEditMode = [" + isEditMode + "]");
        saveActiveContent(contentCreated);
    }

    @Override
    public boolean isNewContentFabVisible() {
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View mSurfaceView = findViewById(R.id.rajawali_surface);
        ((ViewGroup) mSurfaceView.getParent()).removeView(mSurfaceView); //TODO should not delete rajawali
    }

}
