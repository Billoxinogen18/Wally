package com.wally.wally.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.wally.wally.R;

/**
 * This dialog just explains user why it's necessary
 * Created by ioane5 on 6/22/16.
 */
public class PermissionExplanationDialogFragment extends DialogFragment {
    public static final String TAG = PermissionExplanationDialogFragment.class.getSimpleName();
    public static final String ARG_OPEN_SETTINGS = "ARG_OPEN_SETTINGS";
    private static final String ARG_REC_CODE = "ARG_REC_CODE";
    private static final String ARG_EXPLANATION = "ARG_EXPLANATION";
    private PermissionExplanationListener mListener;


    public static PermissionExplanationDialogFragment newInstance(String explanation, int requestCode, boolean openSettings) {
        Bundle args = new Bundle();
        args.putString(ARG_EXPLANATION, explanation);
        args.putInt(ARG_REC_CODE, requestCode);
        args.putBoolean(ARG_OPEN_SETTINGS, openSettings);

        PermissionExplanationDialogFragment fragment = new PermissionExplanationDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        builder.setMessage(getArguments().getString(ARG_EXPLANATION));

        boolean openSettings = getArguments().getBoolean(ARG_OPEN_SETTINGS);
        builder.setPositiveButton(openSettings ? R.string.open_settings : R.string.give_permission, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mListener.givePermissionClicked(getArguments().getInt(ARG_REC_CODE));
            }
        });

        builder.setNegativeButton(R.string.close_application, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mListener.closeAppClicked(getArguments().getInt(ARG_EXPLANATION));
            }
        });
        setCancelable(false);
        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (PermissionExplanationListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement PermissionExplanationListener");
        }
    }

    public interface PermissionExplanationListener {
        void givePermissionClicked(int requestCode);

        void closeAppClicked(int requestCode);
    }
}
