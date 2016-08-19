package com.wally.wally.controllers.contentCreator;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.wally.wally.App;
import com.wally.wally.R;
import com.wally.wally.Utils;
import com.wally.wally.components.TextChangeListenerAdapter;
import com.wally.wally.components.TiltDialogFragment;
import com.wally.wally.controllers.contentCreator.peopleChooser.PeopleChooserPopup;
import com.wally.wally.datacontroller.content.Content;
import com.wally.wally.datacontroller.content.Visibility;
import com.wally.wally.datacontroller.user.Id;
import com.wally.wally.datacontroller.user.User;
import com.wally.wally.events.WallyEvent;
import com.wally.wally.tip.LocalTipService;
import com.wally.wally.tip.MapEventListener;
import com.wally.wally.tip.Tip;
import com.wally.wally.tip.TipManager;
import com.wally.wally.tip.TipView;
import com.wally.wally.userManager.SocialUser;

import java.util.ArrayList;
import java.util.List;

/**
 * New Post dialog, that manages adding new content.
 * <p/>
 * Created by ioane5 on 4/7/16.
 */
public class NewContentDialogFragment extends TiltDialogFragment implements
        View.OnClickListener,
        PhotoChooserPopup.PhotoChooserListener,
        PeopleChooserPopup.PeopleChooserListener,
        TextChangeListenerAdapter.TextChangeListener {

    public static final String TAG = NewContentDialogFragment.class.getSimpleName();
    private static final String ARG_EDIT_CONTENT = "ARG_EDIT_CONTENT";
    private static final int RC_PERMISSION_READ_EXTERNAL_STORAGE = 11;


    private NewContentDialogListener mListener;

    private View mNoteView;
    private View mRootView;
    private View mBottomPanel;
    private View mImageContainer;
    private View mBtnPhotoChooser;
    private ImageView mImageView;
    private EditText mTitleEt;
    private EditText mNoteEt;
    private Button mSocialVisibilityBtn;

    private User mAuthor;
    private Content mContent;
    private boolean isEditMode;
    private boolean mIsDialogShown = true;
    private Button mPostCreateBtn;

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
        mAuthor = App.getInstance().getSocialUserManager().getUser().getBaseUser();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.CustomDialogTheme);
        View dv = LayoutInflater.from(getActivity()).inflate(R.layout.new_content_dialog, null, false);
        initViews(dv);
        // setUpTiltingDialog(dv.findViewById(R.id.scroll_view));

        builder.setView(dv);
        Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        setCancelable(false);
        return dialog;
    }

    private void initViews(View v) {
        mBtnPhotoChooser = v.findViewById(R.id.btn_add_image);
        mBtnPhotoChooser.setOnClickListener(this);
        v.findViewById(R.id.btn_social_visibility).setOnClickListener(this);
        v.findViewById(R.id.btn_remove_image).setOnClickListener(this);
        v.findViewById(R.id.btn_pallette).setOnClickListener(this);
        v.findViewById(R.id.btn_discard_post).setOnClickListener(this);
        v.findViewById(R.id.btn_create_post).setOnClickListener(this);
        v.findViewById(R.id.btn_more_settings).setOnClickListener(this);
        v.findViewById(R.id.root).setOnClickListener(this);
        v.findViewById(R.id.space).setOnClickListener(this);

        TipView tipView = (TipView)v.findViewById(R.id.tip_view);
        TipManager tipManager = new TipManager(tipView, LocalTipService.getInstance(getContext()));
        tipManager.onWallyEvent(WallyEvent.createEventWithId(WallyEvent.ON_NEW_CONTENT_DIALOG_SHOW));


        mNoteView = v.findViewById(R.id.note_view);
        mRootView = v.findViewById(R.id.root);
        mBottomPanel = v.findViewById(R.id.bottom_panel);
        mImageView = (ImageView) v.findViewById(R.id.image);
        mImageContainer = v.findViewById(R.id.image_container);
        mTitleEt = (EditText) v.findViewById(R.id.tv_title);
        mNoteEt = (EditText) v.findViewById(R.id.tv_note);
        mSocialVisibilityBtn = (Button) v.findViewById(R.id.btn_social_visibility);
        mPostCreateBtn = (Button) v.findViewById(R.id.btn_create_post);

        // Listen to text changes
        TextChangeListenerAdapter textChangeAdapter = new TextChangeListenerAdapter(this);
        mTitleEt.addTextChangedListener(textChangeAdapter);
        mNoteEt.addTextChangedListener(textChangeAdapter);

        mRootView.getBackground().setDither(true);
        if (isEditMode) {
            mPostCreateBtn.setText(R.string.post_update);
            Button b = (Button) v.findViewById(R.id.btn_discard_post);
            b.setText(R.string.post_cancel_edit);
        }
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
        if (mContent.getVisibility().getSocialVisibility() == null) {
            mContent.getVisibility().withSocialVisibility(
                    new Visibility.SocialVisibility(Visibility.SocialVisibility.PUBLIC));
        }
    }

    public void onStart() {
        super.onStart();
        updateViews();
        showDialog(mIsDialogShown);
        getDialog().getWindow().setFormat(PixelFormat.RGBA_8888);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (NewContentDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement NewContentDialogListener");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RC_PERMISSION_READ_EXTERNAL_STORAGE) {
            if (Utils.checkExternalStorageReadPermission(getContext())) {
                new PhotoChooserPopup().show(mBtnPhotoChooser, this);
            }
        }
    }

    @Override
    public void onClick(final View v) {
//        Utils.hideSoftKeyboard(mNoteEt, getContext());
//        Utils.hideSoftKeyboard(mTitleEt, getContext());
        updateContent();
        switch (v.getId()) {
            case R.id.btn_social_visibility:
                Visibility.SocialVisibility sv = mContent.getVisibility().getSocialVisibility();
                new SocialVisibilityPopup().show(v, sv, new SocialVisibilityPopup.SocialVisibilityListener() {
                    @Override
                    public void onVisibilityChosen(int socialVisibilityMode) {
                        if (mContent.getVisibility().getSocialVisibility() == null) {
                            mContent.getVisibility().withSocialVisibility(new Visibility.SocialVisibility(Visibility.SocialVisibility.PRIVATE));
                        }

                        if (socialVisibilityMode != Visibility.SocialVisibility.PEOPLE) {
                            mContent.getVisibility().getSocialVisibility().setMode(socialVisibilityMode);
                            setDataOnSocialVisibilityButton(mContent.getVisibility().getSocialVisibility());
                        }
                        if (socialVisibilityMode == Visibility.SocialVisibility.PEOPLE) {
                            List<SocialUser> sharedWith = toSocialUserList(mContent.getVisibility().getSocialVisibility().getSharedWith());
                            new PeopleChooserPopup().show(v, sharedWith, NewContentDialogFragment.this);
                        }
                    }
                });
                break;
            case R.id.btn_discard_post:
                if (!isPostEmpty() && !isEditMode) {
                    DiscardDoubleCheckDialogFragment dialog = new DiscardDoubleCheckDialogFragment();
                    dialog.show(getChildFragmentManager(), DiscardDoubleCheckDialogFragment.TAG);
                } else {
                    onContentDiscarded();
                }
                break;
            case R.id.btn_create_post:
                dismiss();
                mListener.onContentCreated(mContent, isEditMode);
                break;
            case R.id.btn_add_image:
                if (Utils.checkExternalStorageReadPermission(getContext())) {
                    new PhotoChooserPopup().show(v, this);
                } else {
                    requestExternalStoragePermissions();
                }
                break;
            case R.id.btn_remove_image:
                mContent.withImageUri(null);
                updateViews();
                break;
            case R.id.btn_pallette:
                new ColorPickerPopup().show(v, new ColorPickerPopup.ColorPickerListener() {
                    @Override
                    public void colorPicked(int color, int textColor) {
                        mContent.withColor(color);
                        mContent.withTextColor(textColor);
                        updateViews();
                    }
                });
                break;
            case R.id.btn_more_settings:
                new ContentMetaInfoPopup().show(v, mContent);
                break;
            case R.id.space:
                if (mNoteEt.requestFocus()) {
                    Utils.showKeyboard(mNoteEt, getContext());
                }
                break;
            case R.id.root:
                Utils.hideSoftKeyboard(mTitleEt, getContext());
                Utils.hideSoftKeyboard(mNoteEt, getContext());
                return;
            default:
                Log.e(TAG, "onClick: " + v.getId());
        }
    }

    //TODO very bad very bad
    private List<SocialUser> toSocialUserList(List<Id> sharedWith) {
        List<SocialUser> friendList = App.getInstance().getSocialUserManager().getUser().getFriends();
        List<SocialUser> result = new ArrayList<>();
        for (Id id : sharedWith) {
            if (id.getProvider().equals(Id.PROVIDER_GOOGLE)) {
                for (SocialUser current : friendList) {
                    if (current.getBaseUser().getGgId().equals(id))
                        result.add(current);
                }
            }
        }

        return result;
    }

    /**
     * Called when user finally discarded post.
     * Now you should clear all caches and destroy self.
     */
    private void onContentDiscarded() {
        dismiss();
    }


    /**
     * Updates model from views.
     */
    private void updateContent() {
        mContent.withTitle(mTitleEt.getText().toString())
                .withNote(mNoteEt.getText().toString());
        mContent.withAuthorId(mAuthor.getId().getId());
    }

    /**
     * Just updates vies from model.
     * Call this method whenever content model is changed.
     */
    private void updateViews() {
        mNoteEt.setText(mContent.getNote());
        mTitleEt.setText(mContent.getTitle());

        if (mContent.getColor() != null) {
            mNoteView.setBackgroundColor(mContent.getColor());
        }

        if (mContent.getTextColor() != null) {
            int textColor = mContent.getTextColor();
            int hintColor = Utils.modifyAlpha(textColor, 96);
            mNoteEt.setTextColor(textColor);
            mNoteEt.setHintTextColor(hintColor);
            mTitleEt.setTextColor(textColor);
            mTitleEt.setHintTextColor(hintColor);
        }

        if (TextUtils.isEmpty(mContent.getImageUri())) {
            mImageView.setImageDrawable(null);
            mImageContainer.setVisibility(View.GONE);
        } else {
            Log.d(TAG, "updateViews: " + mContent.getImageUri());
            Glide.with(getActivity())
                    .load(mContent.getImageUri())
                    .fitCenter()
                    .into(mImageView);
            mImageContainer.setVisibility(View.VISIBLE);
        }
        setDataOnSocialVisibilityButton(mContent.getVisibility().getSocialVisibility());
    }

    /**
     * Checks if post is touched by user.
     *
     * @return true if everything is untouched by user.
     */
    private boolean isPostEmpty() {
        return TextUtils.isEmpty(mContent.getImageUri())
                && TextUtils.isEmpty(mNoteEt.getText())
                && TextUtils.isEmpty(mTitleEt.getText());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        updateContent();
        outState.putSerializable("mContent", mContent);
        outState.putBoolean("mIsDialogShown", mIsDialogShown);
    }

    @Override
    public void onPhotoChosen(String uri) {
        if (!TextUtils.isEmpty(uri)) {
            mContent.withImageUri(uri);
        }
        updateViews();
        showDialog(true);
    }

    @Override
    public void onPeopleChosen(List<SocialUser> users) {
        if (users != null) {
            mContent.getVisibility().withSocialVisibility(new Visibility.SocialVisibility(Visibility.SocialVisibility.PEOPLE));

            List<Id> sharedWith = new ArrayList<>();
            mContent.getVisibility().getSocialVisibility().withSharedWith(sharedWith);
            if (!users.isEmpty()) {
                for (SocialUser current : users) {
                    if (current.getBaseUser().getGgId() != null) {
                        sharedWith.add(current.getBaseUser().getGgId());
                    }
                }
            } else {
                mContent.getVisibility().getSocialVisibility().setMode(Visibility.SocialVisibility.PRIVATE);
            }
        }
        updateViews();
        showDialog(true);
    }

    private void setDataOnSocialVisibilityButton(Visibility.SocialVisibility visibility) {
        int mode = visibility.getMode();
        mSocialVisibilityBtn.setText(Visibility.SocialVisibility.getStringRepresentation(mode));

        Drawable drawable = ContextCompat.getDrawable(getContext(), Visibility.SocialVisibility.toDrawableRes(mode));
        if (mContent.getTextColor() != null) {
            drawable = Utils.tintDrawable(drawable, mContent.getTextColor());
            mSocialVisibilityBtn.setTextColor(mContent.getTextColor());
        }
        mSocialVisibilityBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(drawable, null, null, null);
    }

    private void showDialog(boolean show) {
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
    public void onTextChanged() {
        onPostContentChanged();
    }

    /**
     * This method is called whenever something is updated in the post.
     */
    private void onPostContentChanged() {
        boolean isPostEmpty = isPostEmpty();
        if (isPostEmpty == mPostCreateBtn.isEnabled()) {
            mPostCreateBtn.setEnabled(!isPostEmpty);
        }
    }

    private void requestExternalStoragePermissions() {
        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                RC_PERMISSION_READ_EXTERNAL_STORAGE);
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
}
