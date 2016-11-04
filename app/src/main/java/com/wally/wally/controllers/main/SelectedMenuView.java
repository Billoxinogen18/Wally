package com.wally.wally.controllers.main;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.wally.wally.R;
import com.wally.wally.Utils;
import com.wally.wally.components.UserInfoView;
import com.wally.wally.objects.content.Content;
import com.wally.wally.userManager.SocialUser;

/**
 * This custom view manages content selection behavior and layout
 * Created by Meravici on 5/27/2016.
 */
public class SelectedMenuView extends RelativeLayout {

    @SuppressWarnings("unused")
    private static final String TAG = SelectedMenuView.class.getSimpleName();

    private UserInfoView mUserInfoView;
    private TextView mNoteDate;
    private View mContentControlPanel;

    private Content mSelectedContent;
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
            mNoteDate.setText("");
            mUserInfoView.clearUser();
        }
    }

    public void setContent(Content content, GoogleApiClient googleApiClient) {
        if (content != null) {
            mSelectedContent = content;
            mUserInfoView.loadAndSetUser(
                    content.getAuthorId(),
                    content.getVisibility().isAuthorAnonymous(),
                    googleApiClient);

            if (content.getCreationDate() != null) {
                mNoteDate.setText(Utils.formatDateSmart(getContext(), content.getCreationDate().getTime()));
            }
            showContentControlPanel(Utils.isCurrentUser(content.getAuthorId()));
        }
    }

    public void setOnSelectedMenuActionListener(OnSelectedMenuActionListener onSelectedMenuActionListener) {
        mOnSelectedMenuActionListener = onSelectedMenuActionListener;
    }


    private void init() {
        inflate(getContext(), R.layout.layout_content_select, this);
        mUserInfoView = (UserInfoView) findViewById(R.id.user_info_view);
        mNoteDate = (TextView) mUserInfoView.findViewById(R.id.note_date);
        mContentControlPanel = findViewById(R.id.content_control_panel);

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

        mUserInfoView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnSelectedMenuActionListener != null) {
                    mOnSelectedMenuActionListener.onProfileClick(mUserInfoView.getUser(), false);
                }
            }
        });
    }

    private void showContentControlPanel(boolean show) {
        mContentControlPanel.setVisibility(show ? VISIBLE : GONE);
    }


    public interface OnSelectedMenuActionListener {
        void onDeleteSelectedContentClick(Content selectedContent);

        void onEditSelectedContentClick(Content selectedContent);

        void onProfileClick(SocialUser user, boolean type);
    }
}
