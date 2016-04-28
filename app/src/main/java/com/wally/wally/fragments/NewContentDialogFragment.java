package com.wally.wally.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.wally.wally.R;
import com.wally.wally.Utils;
import com.wally.wally.activities.ChoosePhotoActivity;
import com.wally.wally.datacontroller.content.Content;

/**
 * New Post dialog, that manages adding new content.
 * <p/>
 * Created by ioane5 on 4/7/16.
 */
public class NewContentDialogFragment extends DialogFragment implements View.OnClickListener {

    public static final String TAG = NewContentDialogFragment.class.getSimpleName();
    public static final String ARG_EDIT_CONTENT = "ARG_EDIT_CONTENT";
    private static final int REQUEST_CODE_CHOOSE_PHOTO = 129;
    private static final int MY_LOCATION_REQUEST_CODE = 22;

    private NewContentDialogListener mListener;
    private View mImageContainer;
    private ImageView mImageView;
    private EditText mTitleEt;
    private EditText mNoteEt;
    private Content mContent;
    private boolean isEditMode;
    private GoogleApiClient mGoogleApiClient;

    // Empty constructor required for DialogFragment
    public NewContentDialogFragment() {
    }

    public static NewContentDialogFragment newInstance(Content content) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_EDIT_CONTENT, content);
        NewContentDialogFragment fragment = new NewContentDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static NewContentDialogFragment newInstance() {
        return new NewContentDialogFragment();
    }

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (getArguments() != null) {
            mContent = (Content) getArguments().getSerializable(ARG_EDIT_CONTENT);
            isEditMode = true;
        } else {
            isEditMode = false;
        }
        if (mContent == null) {
            mContent = new Content();
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View dv = LayoutInflater.from(getActivity()).inflate(R.layout.new_content_dialog, null, false);

        dv.findViewById(R.id.btn_visibility_status).setOnClickListener(this);
        dv.findViewById(R.id.btn_add_image).setOnClickListener(this);
        dv.findViewById(R.id.btn_remove_image).setOnClickListener(this);
        dv.findViewById(R.id.btn_pallette).setOnClickListener(this);

        dv.findViewById(R.id.btn_discard_post).setOnClickListener(this);
        dv.findViewById(R.id.btn_create_post).setOnClickListener(this);

        if (isEditMode) {
            Button b = (Button) dv.findViewById(R.id.btn_create_post);
            b.setText(R.string.post_update);
        }
        mImageView = (ImageView) dv.findViewById(R.id.image);
        mImageContainer = dv.findViewById(R.id.image_container);

        if (savedInstanceState != null) {
            mContent = (Content) savedInstanceState.getSerializable("mContent");
        }

        mTitleEt = (EditText) dv.findViewById(R.id.tv_title);
        mNoteEt = (EditText) dv.findViewById(R.id.tv_note);
        
        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                    .addApi(LocationServices.API)
                    .build();
        }

        updateViews();
        builder.setView(dv);
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

                if (Utils.checkLocationPermission(getContext())) {
                    Location myLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

                    if (myLocation == null) {
                        Log.e(TAG, "centerMapOnMyLocation: couldn't get user location");
                        return;
                    }
                    mContent.withLocation(new com.wally.wally.datacontroller.content.Location(myLocation.getLatitude(), myLocation.getLongitude()));

                    updateContent();
                    if (isEditMode) {
                        mListener.onContentUpdated(mContent);
                    } else {
                        mListener.onContentCreated(mContent);
                    }
                } else {
                    ActivityCompat.requestPermissions(getActivity(),
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            MY_LOCATION_REQUEST_CODE);
                }
                break;
            case R.id.btn_add_image:
                startActivityForResult(ChoosePhotoActivity.newIntent(getActivity()), REQUEST_CODE_CHOOSE_PHOTO);
                break;
            case R.id.btn_remove_image:
                mContent.withImageUri(null);
                updateViews();
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
                mContent.withImageUri(data.getDataString());
                // TODO maybe save image in local cache?
                updateViews();
            } else {
                // TODO user canceled or error happened.
                Log.i(TAG, "onActivityResult: canceled");
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        updateContent();
        outState.putSerializable("mContent", mContent);
    }

    /**
     * Updates model from views.
     */
    private void updateContent() {
        mContent.withTitle(mTitleEt.getText().toString())
                .withNote(mNoteEt.getText().toString());
    }

    /**
     * Just updates vies from model.
     * Call this method whenever content model is changed.
     */
    private void updateViews() {
        mNoteEt.setText(mContent.getNote());
        mTitleEt.setText(mContent.getTitle());

        if (TextUtils.isEmpty(mContent.getImageUri())) {
            mImageView.setImageDrawable(null);
            mImageContainer.setVisibility(View.GONE);
        } else {
            Glide.with(getActivity())
                    .load(mContent.getImageUri())
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
        return TextUtils.isEmpty(mContent.getImageUri())
                && TextUtils.isEmpty(mNoteEt.getText())
                && TextUtils.isEmpty(mTitleEt.getText());
    }

    public interface NewContentDialogListener {
        /**
         * When post is created by user, this method is called.
         */
        void onContentCreated(Content content);

        /**
         * When post is updated by user, this method is called.
         */
        void onContentUpdated(Content content);
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
