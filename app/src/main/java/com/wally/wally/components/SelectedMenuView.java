package com.wally.wally.components;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.api.GoogleApiClient;
import com.wally.wally.App;
import com.wally.wally.R;
import com.wally.wally.datacontroller.callbacks.Callback;
import com.wally.wally.datacontroller.content.Content;
import com.wally.wally.datacontroller.user.User;
import com.wally.wally.userManager.SocialUser;
import com.wally.wally.userManager.UserManager;

/**
 * Created by Meravici on 5/27/2016.
 */
public class SelectedMenuView extends RelativeLayout {
    private static final String TAG = SelectedMenuView.class.getSimpleName();

    private ImageView mOwnerImage;
    private TextView mOwnerName;
    private TextView mNoteDate;
    private View mContentControlPanel;
    private View mLayoutNoteInfo;

    private GoogleApiClient mGoogleApiClient;
    private Content mSelectedContent;
    private SocialUser mContentAuthor;
    private OnSelectedMenuActionListener mOnSelectedMenuActionListener;


    public SelectedMenuView(Context context) {
        super(context);
        init();
    }

    public SelectedMenuView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();

    }

    public SelectedMenuView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (visibility == GONE) {
            mContentControlPanel.setVisibility(GONE);
            mNoteDate.setText("");
            mOwnerImage.setImageBitmap(null);
            mOwnerName.setText("");
        }
    }

    public void setContent(Content content, GoogleApiClient googleApiClient) {
        mContentAuthor = null;
        if (content != null) {
            mGoogleApiClient = googleApiClient;
            mSelectedContent = content;

            SocialUser loggedInUser = App.getInstance().getUserManager().getUser();

            if (content.getAuthorId().equals(loggedInUser.getBaseUser().getId().getId())) {
                setAuthorData(loggedInUser);
                showContentControlPanel();
            } else {
                loadAndSetAuthorData(content.getAuthorId());
            }
// TODO
//        mNoteDate.setText(content.getDate());
        }
    }

    public void setOnSelectedMenuActionListener(OnSelectedMenuActionListener onSelectedMenuActionListener) {
        mOnSelectedMenuActionListener = onSelectedMenuActionListener;
    }


    private void init() {
        inflate(getContext(), R.layout.layout_content_select, this);
        mOwnerImage = (ImageView) findViewById(R.id.owner_image);
        mOwnerName = (TextView) findViewById(R.id.owner_name);
        mNoteDate = (TextView) findViewById(R.id.note_date);
        mContentControlPanel = findViewById(R.id.content_control_panel);
        mLayoutNoteInfo = findViewById(R.id.layout_note_info);

        ImageButton mBtnDeleteContent = (ImageButton) findViewById(R.id.btn_delete_content);
        ImageButton mBtnEditContent = (ImageButton) findViewById(R.id.btn_edit_content);

        mBtnDeleteContent.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnSelectedMenuActionListener != null) {
                    mOnSelectedMenuActionListener.onDeleteSelectedContentClick(mSelectedContent);
                }
            }
        });

        mBtnEditContent.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnSelectedMenuActionListener != null) {
                    mOnSelectedMenuActionListener.onEditSelectedContentClick(mSelectedContent);
                }
            }
        });

        mLayoutNoteInfo.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnSelectedMenuActionListener != null) {
                    mOnSelectedMenuActionListener.onProfileClick(mContentAuthor);
                }
            }
        });
    }

    private void setAuthorData(SocialUser user) {
        Log.d(TAG, "setAuthorData() called with: " + "user = [" + user.getDisplayName() + "]");
        mContentAuthor = user;
        Glide.with(getContext())
                .load(user.getAvatarUrl())
                .transform(new CircleTransform(getContext()))
                .into(mOwnerImage);
        mOwnerName.setText(user.getDisplayName());
    }

    private void loadAndSetAuthorData(String authorId) {
        getUser(authorId, new UserManager.UserLoadListener() {
            @Override
            public void onUserLoad(SocialUser user) {
                setAuthorData(user);
            }
        });
    }

    private void getUser(String userId, final UserManager.UserLoadListener userLoadListener) {
        App.getInstance().getDataController().fetchUser(userId, new Callback<User>() {
            @Override
            public void onResult(User result) {
                App.getInstance().getUserManager().loadUser(result, mGoogleApiClient, userLoadListener);
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "onError: ", e);
            }
        });
    }

    private void showContentControlPanel() {
        mContentControlPanel.setVisibility(VISIBLE);
    }


    public interface OnSelectedMenuActionListener {
        void onDeleteSelectedContentClick(Content selectedContent);

        void onEditSelectedContentClick(Content selectedContent);

        void onProfileClick(SocialUser user);
    }
}
