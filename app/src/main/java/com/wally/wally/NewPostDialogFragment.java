package com.wally.wally;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

/**
 * New Post dialog, that manages adding new content.
 * <p/>
 * Created by ioane5 on 4/7/16.
 */
public class NewPostDialogFragment extends AppCompatDialogFragment {

    // Empty constructor required for DialogFragment
    public NewPostDialogFragment() {
    }

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.new_content_dialog, null, false);

        builder.setView(dialogView);
        final Dialog dialog = builder.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        return dialog;
    }
}
