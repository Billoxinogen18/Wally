package com.wally.wally;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.wally.wally.activities.ChoosePhotoActivity;

/**
 * New Post dialog, that manages adding new content.
 * <p/>
 * Created by ioane5 on 4/7/16.
 */
public class NewPostDialogFragment extends AppCompatDialogFragment implements View.OnClickListener {

    public static final String TAG = NewPostDialogFragment.class.getSimpleName();

    private static final int REQUEST_CODE_CHOOSE_PHOTO = 129;

    private View mImageContainer;
    private ImageView mImageView;
    private String mImageUri;

    // Empty constructor required for DialogFragment
    public NewPostDialogFragment() {
    }

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.new_content_dialog, null, false);

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

        builder.setView(dialogView);
        Dialog dialog = builder.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_discard_post:
                // TODO dialog to double check user.
                break;
            case R.id.btn_create_post:
                // TODO callback to activity.
                break;

            case R.id.btn_add_image:
                startActivityForResult(ChoosePhotoActivity.newIntent(getContext()), REQUEST_CODE_CHOOSE_PHOTO);
                break;
            case R.id.btn_remove_image:
                mImageUri = null;
                updateImage();
                break;
            case R.id.btn_pallette:
            case R.id.btn_visibility_status:
                Toast.makeText(getContext(), "Not yet implemented", Toast.LENGTH_SHORT).show();
                break;
            default:
                Log.e(TAG, "onClick: " + v.getId());
        }
    }

    private void updateImage() {
        if (TextUtils.isEmpty(mImageUri)) {
            mImageView.setImageDrawable(null);
            mImageContainer.setVisibility(View.GONE);
        } else {
            Glide.with(getContext())
                    .load(mImageUri)
                    .fitCenter()
                    .into(mImageView);
            mImageContainer.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CHOOSE_PHOTO) {
            if (resultCode == Activity.RESULT_OK) {
                mImageUri = data.getDataString();
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
}
