package com.wally.wally.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.wally.wally.R;
import com.wally.wally.activities.ChoosePhotoActivity;
import com.wally.wally.datacontroller.Content;

/**
 * New Post dialog, that manages adding new content.
 * <p>
 * Created by ioane5 on 4/7/16.
 */
public class NewContentDialogFragment extends DialogFragment implements View.OnClickListener {

    public static final String TAG = NewContentDialogFragment.class.getSimpleName();

    private static final int REQUEST_CODE_CHOOSE_PHOTO = 129;
    private NewContentDialogListener mListener;
    private View mImageContainer;
    private ImageView mImageView;
    private String mImageUri;
    private EditText mTitleEt;
    private EditText mNoteEt;

    // Empty constructor required for DialogFragment
    public NewContentDialogFragment() {
    }

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.new_content_dialog, null, false);

        dialogView.findViewById(R.id.btn_visibility_status).setOnClickListener(this);
        dialogView.findViewById(R.id.btn_add_image).setOnClickListener(this);
        dialogView.findViewById(R.id.btn_remove_image).setOnClickListener(this);
        dialogView.findViewById(R.id.btn_pallette).setOnClickListener(this);

        dialogView.findViewById(R.id.btn_discard_post).setOnClickListener(this);
        dialogView.findViewById(R.id.btn_create_post).setOnClickListener(this);

        mImageView = (ImageView) dialogView.findViewById(R.id.image);
        mImageContainer = dialogView.findViewById(R.id.image_container);

        if (savedInstanceState != null) {
            mImageUri = savedInstanceState.getString("image_uri");
            updateImage();
        }

        mTitleEt = (EditText) dialogView.findViewById(R.id.tv_title);
        mNoteEt = (EditText) dialogView.findViewById(R.id.tv_note);

        builder.setView(dialogView);
        Dialog dialog = builder.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (NewContentDialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement NewContentDialogListener");
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_discard_post:
                if (!postIsEmpty()) {
                    DiscardDoubleCheckDialogFragment dialog = new DiscardDoubleCheckDialogFragment();
                    dialog.show(getChildFragmentManager(), "DiscardDoubleCheckDialogFragment");
                } else {
                    onContentDiscarded();
                }
                break;
            case R.id.btn_create_post:
                dismiss();
                mListener.onContentCreated(new Content()
                        .withTitle(mTitleEt.getText().toString())
                        .withNote(mNoteEt.getText().toString())
                        // TODO add all fields
                        //.withVisibility()
                        //.withRange()
                        //.withTimestamp()
                        .withImageUri(mImageUri)
                );
                break;
            case R.id.btn_add_image:
                startActivityForResult(ChoosePhotoActivity.newIntent(getActivity()), REQUEST_CODE_CHOOSE_PHOTO);
                break;
            case R.id.btn_remove_image:
                mImageUri = null;
                updateImage();
                break;
            case R.id.btn_pallette:
            case R.id.btn_visibility_status:
                Toast.makeText(getActivity(), "Not yet implemented", Toast.LENGTH_SHORT).show();
                break;
            default:
                Log.e(TAG, "onClick: " + v.getId());
        }
    }

    /**
     * Called when user finally discarded post.
     * Now you should clear all caches and destroy self.
     */
    public void onContentDiscarded() {
        dismiss();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CHOOSE_PHOTO) {
            if (resultCode == Activity.RESULT_OK) {
                mImageUri = data.getDataString();
                // TODO maybe save image in local cache?
                updateImage();
            } else {
                // TODO user canceled or error happened.
                Log.i(TAG, "onActivityResult: canceled");
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("image_uri", mImageUri);
    }

    private void updateImage() {
        if (TextUtils.isEmpty(mImageUri)) {
            mImageView.setImageDrawable(null);
            mImageContainer.setVisibility(View.GONE);
        } else {
            Glide.with(getActivity())
                    .load(mImageUri)
                    .fitCenter()
                    .into(mImageView);
            mImageContainer.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Checks if post is touched by user.
     *
     * @return true if everything is untouched by user.
     */
    public boolean postIsEmpty() {
        return TextUtils.isEmpty(mImageUri)
                && TextUtils.isEmpty(mNoteEt.getText())
                && TextUtils.isEmpty(mTitleEt.getText());
    }

    public interface NewContentDialogListener {
        /**
         * When post is created by user, this method is called.
         */
        void onContentCreated(Content content);
    }

    public static class DiscardDoubleCheckDialogFragment extends DialogFragment {

        public DiscardDoubleCheckDialogFragment() {
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.dialog_message_discard_post_doublecheck);
            builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    ((NewContentDialogFragment) getParentFragment()).onContentDiscarded();
                }
            });
            builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            AlertDialog dialog = builder.create();
            dialog.setCanceledOnTouchOutside(false);
            return dialog;
        }
    }
}
