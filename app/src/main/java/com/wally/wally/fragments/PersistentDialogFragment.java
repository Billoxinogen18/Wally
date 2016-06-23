package com.wally.wally.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

/**
 * This is general persistent dialog fragment, that is recreated after rotation changes.
 * <p/>
 * Created by ioane5 on 6/22/16.
 */
public class PersistentDialogFragment extends DialogFragment {
    public static final String TAG = PersistentDialogFragment.class.getSimpleName();

    private static final String ARG_REC_CODE = "ARG_REC_CODE";

    private static final String ARG_TITLE = "ARG_TITLE";
    private static final String ARG_MESSAGE = "ARG_MESSAGE";
    private static final String ARG_POSITIVE_TEXT = "ARG_POSITIVE_TEXT";
    private static final String ARG_NEGATIVE_TEXT = "ARG_NEGATIVE_TEXT";
    private static final String ARG_IS_CANCELLABLE = "ARG_IS_CANCELLABLE";

    private PersistentDialogListener mListener;


    public static PersistentDialogFragment newInstance(int requestCode, String title, String msg, String positiveBtnText, String negativeBtnText, boolean isCancellable) {
        Bundle args = new Bundle();
        args.putInt(ARG_REC_CODE, requestCode);

        args.putString(ARG_TITLE, title);
        args.putString(ARG_MESSAGE, msg);
        args.putString(ARG_POSITIVE_TEXT, positiveBtnText);
        args.putString(ARG_NEGATIVE_TEXT, negativeBtnText);
        args.putBoolean(ARG_IS_CANCELLABLE, isCancellable);

        PersistentDialogFragment fragment = new PersistentDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static PersistentDialogFragment newInstance(Context context, int requestCode, @StringRes int msg, @StringRes int positiveBtn, @StringRes int negativeBtn) {
        return newInstance(requestCode,
                null,
                context.getString(msg),
                context.getString(positiveBtn),
                context.getString(negativeBtn),
                false);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        Bundle args = getArguments();
        builder.setTitle(args.getString(ARG_TITLE));
        builder.setMessage(args.getString(ARG_MESSAGE));

        if (args.containsKey(ARG_POSITIVE_TEXT)) {
            builder.setPositiveButton(args.getString(ARG_POSITIVE_TEXT), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    mListener.onDialogPositiveClicked(getArguments().getInt(ARG_REC_CODE));
                }
            });
        }

        if (args.containsKey(ARG_NEGATIVE_TEXT)) {
            builder.setNegativeButton(args.getString(ARG_NEGATIVE_TEXT), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    mListener.onDialogNegativeClicked(getArguments().getInt(ARG_REC_CODE));
                }
            });
        }

        setCancelable(args.getBoolean(ARG_IS_CANCELLABLE, false));
        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (PersistentDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + "Parent activity must implement PersistentDialogListener");
        }
    }

    public interface PersistentDialogListener {
        void onDialogPositiveClicked(int requestCode);

        void onDialogNegativeClicked(int requestCode);
    }
}
