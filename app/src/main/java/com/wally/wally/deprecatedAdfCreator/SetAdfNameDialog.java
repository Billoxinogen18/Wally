package com.wally.wally.deprecatedAdfCreator;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.wally.wally.R;

/**
 * Created by Meravici on 7/8/2016. yea
 */
@Deprecated
public class SetAdfNameDialog extends DialogFragment {

    public static final String TAG = SetAdfNameDialog.class.getSimpleName();
    private CallbackListener mCallbackListener;

    public static SetAdfNameDialog newInstance() {
        return new SetAdfNameDialog();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View dv = LayoutInflater.from(getActivity()).inflate(R.layout.set_name_dialog, null, false);

        TextView title = (TextView) dv.findViewById(R.id.tv_title);
        title.setText(R.string.set_name_dialog_title);

        final TextInputEditText nameEditText = (TextInputEditText) dv.findViewById(R.id.et_adf_name);

        Button okButton = (Button) dv.findViewById(R.id.btn_ok);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCallbackListener.onAdfNameOk(nameEditText.getText().toString());
                dismiss();
            }
        });

        Button cancelButton = (Button) dv.findViewById(R.id.btn_cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCallbackListener.onAdfNameCancelled();
                dismiss();
            }
        });

        setCancelable(false);

        builder.setView(dv);
        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();
        int width = getResources().getDimensionPixelSize(R.dimen.adf_name_dialog_width);
        int height = getResources().getDimensionPixelSize(R.dimen.adf_name_dialog_height);
        getDialog().getWindow().setLayout(width, height);
    }

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        mCallbackListener = (CallbackListener) activity;
    }

    interface CallbackListener {
        void onAdfNameOk(String name);

        void onAdfNameCancelled();
    }
}
