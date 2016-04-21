package com.wally.wally.fragments;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.wally.wally.R;
import com.wally.wally.dal.Content;


public class PreviewContentDialogFragment extends DialogFragment {

    public static final String TAG = PreviewContentDialogFragment.class.getSimpleName();

    // Empty constructor required for DialogFragment
    public PreviewContentDialogFragment() {
    }

    public static PreviewContentDialogFragment newInstance(Content content) {
        Bundle args = new Bundle();
        args.putSerializable("content", content);

        PreviewContentDialogFragment fragment = new PreviewContentDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Content content = (Content)getArguments().getSerializable("content");
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.preview_content_dialog, null, false);

        ImageView mImageView = (ImageView) dialogView.findViewById(R.id.image);

        TextView mTitleEt = (TextView) dialogView.findViewById(R.id.et_title);
        TextView mNoteEt = (TextView) dialogView.findViewById(R.id.et_note);

        if(!TextUtils.isEmpty(content.getTitle())){
            mTitleEt.setText(content.getTitle());
            mTitleEt.setVisibility(View.VISIBLE);
        }
        mNoteEt.setText(content.getNote());


        if(!TextUtils.isEmpty(content.getImageUri())){
            Glide.with(getActivity())
                    .load(content.getImageUri())
                    .fitCenter()
                    .into(mImageView);
            mImageView.setVisibility(View.VISIBLE);
        }

        builder.setView(dialogView);
        Dialog dialog = builder.create();
        return dialog;
    }
}