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
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.wally.wally.App;
import com.wally.wally.R;
import com.wally.wally.Utils;
import com.wally.wally.activities.ChoosePhotoActivity;
import com.wally.wally.datacontroller.content.Content;
import com.wally.wally.datacontroller.content.Visibility;
import com.wally.wally.datacontroller.user.User;

import java.util.Date;

/**
 * New Post dialog, that manages adding new content.
 * <p/>
 * Created by ioane5 on 4/7/16.
 */
@SuppressWarnings("ALL")
public class NewContentDialogFragment extends DialogFragment implements View.OnClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    public static final String TAG = NewContentDialogFragment.class.getSimpleName();
    public static final String ARG_EDIT_CONTENT = "ARG_EDIT_CONTENT";
    private static final int REQUEST_CODE_CHOOSE_PHOTO = 129;
    private static final int REQUEST_CODE_MY_LOCATION = 22;

    private NewContentDialogListener mListener;

    private View mImageContainer;
    private ImageView mImageView;
    private EditText mTitleEt;
    private EditText mNoteEt;
    private Spinner mVisibilitySpinner;

    private User mAuthor;
    private Content mContent;
    private boolean isEditMode;
    private GoogleApiClient mGoogleApiClient;
    private boolean mIsDialogShown = true;

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
        initContent(savedInstanceState);
        mAuthor = App.getInstance().getUserManager().getUser().getBaseUser();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View dv = LayoutInflater.from(getActivity()).inflate(R.layout.new_content_dialog, null, false);

        dv.findViewById(R.id.btn_add_image).setOnClickListener(this);
        dv.findViewById(R.id.btn_remove_image).setOnClickListener(this);
        dv.findViewById(R.id.btn_pallette).setOnClickListener(this);
        dv.findViewById(R.id.btn_discard_post).setOnClickListener(this);
        dv.findViewById(R.id.btn_create_post).setOnClickListener(this);
        dv.findViewById(R.id.btn_more_settings).setOnClickListener(this);

        mImageView = (ImageView) dv.findViewById(R.id.image);
        mImageContainer = dv.findViewById(R.id.image_container);
        mTitleEt = (EditText) dv.findViewById(R.id.tv_title);
        mNoteEt = (EditText) dv.findViewById(R.id.tv_note);
        mVisibilitySpinner = (Spinner) dv.findViewById(R.id.spinner_visibility_status);
        mVisibilitySpinner.setAdapter(new VisibilityAdapter(getContext()));

        if (isEditMode) {
            Button b = (Button) dv.findViewById(R.id.btn_create_post);
            b.setText(R.string.post_update);
        }
        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
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

    private void initContent(Bundle savedInstanceState) {
        if (getArguments() != null) {
            mContent = (Content) getArguments().getSerializable(ARG_EDIT_CONTENT);
            isEditMode = true;
        } else {
            isEditMode = false;
        }

        if (savedInstanceState != null) {
            mContent = (Content) savedInstanceState.getSerializable("mContent");
            mIsDialogShown = savedInstanceState.getBoolean("mIsDialogShown", true);
        }

        if (mContent == null) {
            mContent = new Content();
        }
        if (mContent.getVisibility() == null) {
            mContent.withVisibility(new Visibility());
        }
    }

    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
        showDialog(mIsDialogShown);
    }

    public void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnected(Bundle connectionHint) {

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
                    dialog.show(getChildFragmentManager(), DiscardDoubleCheckDialogFragment.TAG);
                } else {
                    onContentDiscarded();
                }
                break;
            case R.id.btn_create_post:
                if (Utils.checkLocationPermission(getContext())) {
                    createPost();
                } else {
                    ActivityCompat.requestPermissions(getActivity(),
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            REQUEST_CODE_MY_LOCATION);
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
                Toast.makeText(getActivity(), "Not yet implemented", Toast.LENGTH_SHORT).show();
                break;
            case R.id.btn_more_settings:
                showDialog(false);
                MetaInfoDialogFragment.newInstance(mContent).show(getChildFragmentManager(), MetaInfoDialogFragment.TAG);
                break;
            default:
                Log.e(TAG, "onClick: " + v.getId());
        }
    }

    @SuppressWarnings("MissingPermission")
    private void createPost() {
        dismiss();
        Location myLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (myLocation == null) {
            Log.e(TAG, "centerMapOnMyLocation: couldn't get user location");
            Toast.makeText(getContext(), "Couldn't get user location", Toast.LENGTH_SHORT).show();
        } else {
            mContent.withLocation(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()));
        }
        updateContent();
        mListener.onContentCreated(mContent, isEditMode);
    }

    /**
     * Called when user finally discarded post.
     * Now you should clear all caches and destroy self.
     */
    public void onContentDiscarded() {
        dismiss();
    }


    /**
     * Updates model from views.
     */
    private void updateContent() {
        if (mContent.getVisibility() == null) {
            mContent.withVisibility(new Visibility());
        }
        //noinspection WrongConstant
        Visibility.SocialVisibility socialVisibility =
                new Visibility.SocialVisibility((Integer) mVisibilitySpinner.getSelectedItem());

        mContent.getVisibility().withSocialVisibility(socialVisibility);

        mContent.withTitle(mTitleEt.getText().toString())
                .withNote(mNoteEt.getText().toString());
        mContent.withAuthor(mAuthor);
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_MY_LOCATION) {
            if (Utils.checkLocationPermission(getContext())) {
                createPost();
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        updateContent();
        outState.putSerializable("mContent", mContent);
        outState.putBoolean("mIsDialogShown", mIsDialogShown);
    }

    public void onMetaInfoDialogDismiss(Content content) {
        mContent = content;
        showDialog(true);
    }

    public void showDialog(boolean show) {
        if (getDialog() != null) {
            if (!show) {
                getDialog().hide();
            } else {
                getDialog().show();
            }
        }
        mIsDialogShown = show;
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public interface NewContentDialogListener {
        /**
         * When post is created by user, this method is called.
         */
        void onContentCreated(Content content, boolean isEditMode);
    }

    public static class DiscardDoubleCheckDialogFragment extends DialogFragment {

        public static final String TAG = DiscardDoubleCheckDialogFragment.class.getSimpleName();

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

    public static class MetaInfoDialogFragment extends DialogFragment implements
            AdapterView.OnItemSelectedListener,
            DatePickerDialogFragment.DatePickListener {

        public static final String TAG = MetaInfoDialogFragment.class.getSimpleName();

        private Content mCont;
        private TextView mNoteDeleteTime;
        private View mNoteDeleteTimeClear;

        public MetaInfoDialogFragment() {
        }

        public static MetaInfoDialogFragment newInstance(Content content) {
            Bundle args = new Bundle();
            args.putSerializable(ARG_EDIT_CONTENT, content);
            MetaInfoDialogFragment fragment = new MetaInfoDialogFragment();
            fragment.setArguments(args);
            return fragment;
        }

        @SuppressLint("InflateParams")
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            mCont = (Content) getArguments().getSerializable(ARG_EDIT_CONTENT);
            //noinspection ConstantConditions
            if (mCont.getVisibility() == null) {
                mCont.withVisibility(new Visibility());
            }
            if (savedInstanceState != null) {
                mCont = (Content) savedInstanceState.getSerializable("mContent");
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            View dv = LayoutInflater.from(getContext())
                    .inflate(R.layout.content_meta_info_dialog, null, false);

            initViews(dv);

            builder.setView(dv);
            AlertDialog dialog = builder.create();
            dialog.setCanceledOnTouchOutside(false);
            return dialog;
        }

        private void initViews(View v) {
            v.findViewById(R.id.btn_dismiss).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });

            // Init range visibility
            Spinner rangeSpinner = (Spinner) v.findViewById(R.id.spinner_range_visibility);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                    android.R.layout.simple_spinner_item, Visibility.RangeVisibility.toList());
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            rangeSpinner.setAdapter(adapter);
            rangeSpinner.setOnItemSelectedListener(this);

            // init map preview
            final CheckBox checkBoxMapPreview = (CheckBox) v.findViewById(R.id.checkbox_map_preview);
            checkBoxMapPreview.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    mCont.getVisibility().withVisiblePreview(isChecked);
                }
            });
            v.findViewById(R.id.layout_map_preview).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    checkBoxMapPreview.performClick();
                }
            });

            // init time to live view
            mNoteDeleteTime = (TextView) v.findViewById(R.id.tv_note_delete_time);
            mNoteDeleteTime.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DatePickerDialogFragment.newInstance().show(getChildFragmentManager(),
                            DatePickerDialogFragment.TAG);
                }
            });
            mNoteDeleteTimeClear = v.findViewById(R.id.btn_reset_delete_time);
            mNoteDeleteTimeClear.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onDateSelected(null);
                }
            });

            // Init views from model
            Visibility visibility = mCont.getVisibility();

            if (visibility.getRangeVisibility() != null) {
                rangeSpinner.setSelection(visibility.getRangeVisibility().getRange());
            }
            checkBoxMapPreview.setChecked(visibility.isPreviewVisible());
            updateTimeVisibilityView();
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            super.onDismiss(dialog);
            NewContentDialogFragment frag = (NewContentDialogFragment) getParentFragment();
            frag.onMetaInfoDialogDismiss(mCont);
        }

        // called when visibility item is selected.
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            mCont.getVisibility().withRangeVisibility(new Visibility.RangeVisibility(position));
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            outState.putSerializable("mContent", mCont);
        }

        // Called when time picker dismisses
        @Override
        public void onDateSelected(Date selectedDate) {
            mCont.getVisibility().withTimeVisibility(selectedDate);
            updateTimeVisibilityView();
        }

        // Just update time visibility view with date from content
        @SuppressWarnings("ConstantConditions")
        private void updateTimeVisibilityView() {
            Date date = mCont.getVisibility().getVisibleUntil();
            String text;
            if (date == null) {
                text = getString(R.string.empty_note_time_visibility);
                mNoteDeleteTimeClear.setVisibility(View.INVISIBLE);
            } else {
                text = String.format(getString(R.string.note_time_visibility),
                        Utils.formatDateSmart(getContext(), date.getTime()));
                mNoteDeleteTimeClear.setVisibility(View.VISIBLE);
            }
            mNoteDeleteTime.setText(text);
        }
    }
}
