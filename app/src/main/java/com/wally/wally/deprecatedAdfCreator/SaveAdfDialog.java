package com.wally.wally.deprecatedAdfCreator;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.wally.wally.R;

/**
 * Created by Meravici on 7/8/2016. yea
 */
@Deprecated
public class SaveAdfDialog extends AlertDialog {
    private static final String TAG = SaveAdfDialog.class.getSimpleName();
    private ProgressBar mProgressBar;

    public SaveAdfDialog(Context context) {
        super(context);
    }

    public void setProgress(int progress) {
        if (mProgressBar != null) {
            mProgressBar.setVisibility(View.VISIBLE);
            mProgressBar.setProgress(progress);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.save_adf_dialog);
        setCancelable(false);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        if (mProgressBar == null) {
            Log.e(TAG, "Unable to find view progress_bar.");
        }
    }
}
