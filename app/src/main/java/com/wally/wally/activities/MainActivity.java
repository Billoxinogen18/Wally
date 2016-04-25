/*
 * Copyright 2016 Wally ltd. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wally.wally.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.google.atap.tango.ux.TangoUxLayout;
import com.google.atap.tangoservice.Tango;
import com.wally.wally.R;
import com.wally.wally.datacontroller.Content;
import com.wally.wally.Utils;
import com.wally.wally.fragments.NewContentDialogFragment;
import com.wally.wally.fragments.PreviewContentDialogFragment;
import com.wally.wally.tango.TangoManager;

import org.rajawali3d.surface.RajawaliSurfaceView;


public class MainActivity extends AppCompatActivity implements NewContentDialogFragment.NewContentDialogListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String ARG_ADF_UUID = "ARG_ADF_UUID";

    private TangoManager mTangoManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RajawaliSurfaceView mSurfaceView = (RajawaliSurfaceView) findViewById(R.id.rajawali_surface);
        TangoUxLayout mTangoUxLayout = (TangoUxLayout) findViewById(R.id.layout_tango_ux);
        String adfUuid = getIntent().getStringExtra(ARG_ADF_UUID);
        mTangoManager = new TangoManager(this, mSurfaceView, mTangoUxLayout, adfUuid);


        if (Utils.hasNoADFPermissions(getBaseContext())) {
            Log.i(TAG, "onCreate: Didn't had ADF permission, requesting permission");
            requestADFPermission();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mTangoManager.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Synchronize against disconnecting while the service is being used in the OpenGL thread or
        // in the UI thread.
        // TODO check if here shoud be synchronized or not
        if (Utils.hasNoADFPermissions(getBaseContext())) {
            Log.i(TAG, "onResume: Didn't have ADF permission returning.");
            return;

        }
        mTangoManager.onResume();
    }

    public void onNewContentClick(View v) {
        NewContentDialogFragment dialog = new NewContentDialogFragment();
        dialog.show(getSupportFragmentManager(), "NewContentDialogFragment");
    }

    public void onBtnMapClick(View v) {
        Intent mapIntent = new Intent(getBaseContext(), MapsActivity.class);
        startActivity(mapIntent);
    }

    @Override
    public void onContentCreated(Content content) {
        Log.d(TAG, "onContentCreated() called with: " + "content = [" + content + "]");
        mTangoManager.startContentFitting(content);
    }


    private void requestADFPermission() {
        startActivityForResult(
                Tango.getRequestPermissionIntent(Tango.PERMISSIONTYPE_ADF_LOAD_SAVE),
                Tango.TANGO_INTENT_ACTIVITYCODE);
    }


    /**
     * Returns intent to start main activity.
     *
     * @param uuid can be null, if we want to start with learning mode.
     */
    public static Intent newIntent(Context context, @Nullable String uuid) {
        Intent i = new Intent(context, MainActivity.class);
        i.putExtra(ARG_ADF_UUID, uuid);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        return i;
    }
}
