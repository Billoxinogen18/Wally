package com.wally.wally.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.wally.wally.R;
import com.wally.wally.Utils;
import com.wally.wally.adf.AdfInfo;

/**
 * This is dialog that manages ADF import export permission grant and returns callback statuses via
 * {@link ImportExportPermissionListener} interface.
 * This dialog also helps to manage explanation messages.
 * <p/>
 * Note that if your activity doesn't implement {@link ImportExportPermissionListener} than you will get exception.
 * Created by ioane5 on 7/8/16.
 */
public class ImportExportPermissionDialogFragment extends DialogFragment implements View.OnClickListener {

    public static final String TAG = ImportExportPermissionDialogFragment.class.getSimpleName();
    public static final int IMPORT = 0;
    public static final int EXPORT = 1;
    private static final String ARG_ADF_INFO = "ARG_ADF_INFO";
    private static final String INTENT_CLASSPACKAGE = "com.projecttango.tango";
    private static final String INTENT_IMPORTEXPORT_CLASSNAME = "com.google.atap.tango.RequestImportExportActivity";
    private static final String EXTRA_KEY_SOURCEFILE = "SOURCE_FILE";
    private static final String EXTRA_KEY_SOURCEUUID = "SOURCE_UUID";
    private static final String EXTRA_KEY_DESTINATIONFILE = "DESTINATION_FILE";
    private static final String ARG_MODE = "ARG_MODE";
    private static final String ARG_REQ_CODE = "ARG_REQ_CODE";

    /**
     * Import or Export
     */
    private int mMode;
    private int mReqCode;
    private ImportExportPermissionListener mListener;

    private AdfInfo mAdfInfo;


    /**
     * @param adfInfo           adfInfo
     * @param importOrExport public integer that represents IMPORT/EXPORT enumeration.
     * @return Fragment that should be shown
     */
    public static ImportExportPermissionDialogFragment newInstance(AdfInfo adfInfo, int importOrExport) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_ADF_INFO, adfInfo);
        args.putInt(ARG_MODE, importOrExport);

        ImportExportPermissionDialogFragment fragment = new ImportExportPermissionDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        readArgs();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View dv = LayoutInflater.from(getActivity()).inflate(R.layout.import_export_explain_dialog, null, false);

        initViews(dv);

        builder.setView(dv);
        Dialog dialog = builder.create();

        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!shouldShowPermissionExplanation()) {
            getDialog().hide();
            requestPermission();
        }
    }

    private void requestPermission() {
        if (mMode == IMPORT) {
            requestImportPermission();
        } else {
            requestExportPermission();
        }
    }

    private void requestImportPermission() {
        Intent importIntent = new Intent();
        importIntent.setClassName(INTENT_CLASSPACKAGE, INTENT_IMPORTEXPORT_CLASSNAME);
        importIntent.putExtra(EXTRA_KEY_SOURCEFILE, Utils.getAdfFilePath(mAdfInfo.getUuid()));
        startActivityForResult(importIntent, mReqCode);
    }

    private void requestExportPermission() {
        Intent exportIntent = new Intent();
        exportIntent.setClassName(INTENT_CLASSPACKAGE, INTENT_IMPORTEXPORT_CLASSNAME);
        exportIntent.putExtra(EXTRA_KEY_SOURCEUUID, mAdfInfo.getUuid());
        exportIntent.putExtra(EXTRA_KEY_DESTINATIONFILE, Utils.getAdfFilesFolder());
        startActivityForResult(exportIntent, mReqCode);
    }

    private void readArgs() {
        Bundle b = getArguments();
        mMode = b.getInt(ARG_MODE);
        mReqCode = b.getInt(ARG_REQ_CODE);
        mAdfInfo = (AdfInfo) b.getSerializable(ARG_ADF_INFO);
    }

    private void initViews(View v) {
        TextView title = (TextView) v.findViewById(R.id.tv_title);
        TextView message = (TextView) v.findViewById(R.id.tv_message);
        v.findViewById(R.id.button_positive).setOnClickListener(this);
        if (mMode == IMPORT) {
            title.setText(R.string.adf_import_explain_title);
            message.setText(R.string.adf_import_explain_message);
        } else {
            title.setText(R.string.adf_export_explain_title);
            message.setText(R.string.adf_export_explain_message);
        }
    }

    private boolean shouldShowPermissionExplanation() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
        return sp.getBoolean(getIsDeniedStatusKey(), true);
    }

    private String getIsDeniedStatusKey() {
        if (mMode == IMPORT) {
            return "IMPORT_DENY_STATUS";
        }
        return "EXPORT_DENY_STATUS";
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == mReqCode) {
            // Save deny status.
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
            SharedPreferences.Editor e = sp.edit();
            e.putBoolean(getIsDeniedStatusKey(), resultCode != Activity.RESULT_OK);
            e.apply();

            if (resultCode == Activity.RESULT_OK) {
                mListener.onPermissionGranted(mAdfInfo);
            } else {
                mListener.onPermissionDenied(mAdfInfo);
            }
            dismiss();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (ImportExportPermissionListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement ImportExportPermissionListener");
        }
    }

    // Got it button clicked
    @Override
    public void onClick(View view) {
        getDialog().hide();
        requestPermission();
    }

    public interface ImportExportPermissionListener {
        void onPermissionGranted(AdfInfo adfInfo);

        void onPermissionDenied(AdfInfo adfInfo);
    }
}
