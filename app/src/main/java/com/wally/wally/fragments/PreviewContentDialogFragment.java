package com.wally.wally.fragments;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.wally.wally.R;
import com.wally.wally.datacontroller.content.Content;


public class PreviewContentDialogFragment extends DialogFragment {

    public static final String TAG = PreviewContentDialogFragment.class.getSimpleName();

    private Content mContent;
    private View mRootView;

    // Empty constructor required for DialogFragment
    public PreviewContentDialogFragment() {
    }

    public static PreviewContentDialogFragment newInstance(Content content) {
        Bundle args = new Bundle();
        args.putSerializable("content", content);
        Log.d(TAG, content.toString());
        PreviewContentDialogFragment fragment = new PreviewContentDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mContent = (Content) getArguments().getSerializable("content");
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.preview_content_dialog, null, false);

        ImageView mImageView = (ImageView) dialogView.findViewById(R.id.image);
        TextView mTitleEt = (TextView) dialogView.findViewById(R.id.tv_title);
        TextView mNoteEt = (TextView) dialogView.findViewById(R.id.tv_note);
        mRootView = dialogView.findViewById(R.id.root);

        if (!TextUtils.isEmpty(mContent.getTitle())) {
            mTitleEt.setText(mContent.getTitle());
            mTitleEt.setVisibility(View.VISIBLE);
        }
        if (!TextUtils.isEmpty(mContent.getNote())) {
            mNoteEt.setText(mContent.getNote());
            mNoteEt.setVisibility(View.VISIBLE);
        }
        if (!TextUtils.isEmpty(mContent.getImageUri())) {
            mImageView.setVisibility(View.VISIBLE);
            Glide.with(getActivity())
                    .load(mContent.getImageUri())
                    .fitCenter()
                    .into(mImageView);
        }

        builder.setView(dialogView);
        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mContent.getColor() != null) {
            View v = (View) mRootView.getParent();
            v.setBackgroundColor(mContent.getColor());
        }
    }
}
