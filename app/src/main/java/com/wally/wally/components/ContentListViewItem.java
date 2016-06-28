package com.wally.wally.components;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.api.GoogleApiClient;
import com.wally.wally.R;
import com.wally.wally.Utils;
import com.wally.wally.datacontroller.content.Content;
import com.wally.wally.userManager.SocialUser;

/**
 * Created by Meravici on 6/20/2016. yea
 */
public class ContentListViewItem extends CardView {
    private UserInfoView userInfoView;
    private ImageView noteImage;
    private TextView title;
    private TextView note;
    private TextView contentPosition;
    private OnClickListener onClickListener;
    private Content content;

    public ContentListViewItem(Context context) {
        super(context);
        init();
    }

    public ContentListViewItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();

    }

    public ContentListViewItem(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public void clear() {
        setCardBackgroundColor(Color.WHITE);

        noteImage.setImageDrawable(null);
        noteImage.setBackground(null);
        noteImage.setVisibility(View.VISIBLE);

        title.setText(null);
        title.setVisibility(View.VISIBLE);
        note.setText(null);
        note.setVisibility(View.VISIBLE);
        contentPosition.setText(null);

        userInfoView.clearUser();
    }

    public void setContent(Content content, GoogleApiClient googleApiClient) {
        this.content = content;

        // Do not show profile info when in profile already.
        // Because all the contents are from the user.
        // Also this thing avoids us to cycle (User->User->User)
        userInfoView.loadAndSetUser(
                content.getAuthorId(),
                content.getVisibility().isAuthorAnonymous(), googleApiClient);
        userInfoView.setStatus(Utils.formatDateSmart(getContext(), content.getCreationDate().getTime()));


        boolean isOwn = Utils.isCurrentUser(content.getAuthorId());
        // Check if user can see content preview
        if (!content.getVisibility().isPreviewVisible() && !isOwn) {
            setCardBackgroundColor(
                    ContextCompat.getColor(getContext(), R.color.content_not_visible_color));
            title.setText(R.string.content_not_visible_title);
            note.setText(R.string.content_not_visible_note);
            noteImage.setVisibility(View.GONE);
            return;
        }
        if (!TextUtils.isEmpty(content.getImageUri())) {
            Glide.with(getContext())
                    .load(content.getImageUri())
                    .thumbnail(0.1f)
                    .fitCenter()
                    .crossFade()
                    .placeholder(R.drawable.ic_image_placeholer)
                    .into(noteImage);

            noteImage.setVisibility(View.VISIBLE);
        } else {
            noteImage.setVisibility(View.GONE);
        }

        if (content.getColor() != null) {
            setCardBackgroundColor(content.getColor());
        } else {
            setCardBackgroundColor(Color.WHITE);
        }
        title.setText(content.getTitle());
        title.setVisibility(TextUtils.isEmpty(content.getTitle()) ? View.GONE : View.VISIBLE);

        note.setText(content.getNote());
        note.setVisibility(TextUtils.isEmpty(content.getNote()) ? View.GONE : View.VISIBLE);
    }

    private void init() {
        inflate(getContext(), R.layout.content_listview_item, this);

        userInfoView = (UserInfoView) findViewById(R.id.user_info_view);
        noteImage = (ImageView) findViewById(R.id.iv_note_image);
        title = (TextView) findViewById(R.id.tv_title);
        note = (TextView) findViewById(R.id.tv_note);
        contentPosition = (TextView) findViewById(R.id.tv_content_position);

        userInfoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickListener.onProfileClicked(userInfoView.getUser());
            }
        });

        setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickListener.onContentClicked(content);
            }
        });
    }


    public void setPosition(int position) {
        contentPosition.setText("" + position);
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public void showUserInfo(boolean show) {
        userInfoView.setVisibility(show? VISIBLE : View.GONE);
    }


    public interface OnClickListener {
        void onProfileClicked(SocialUser user);

        void onContentClicked(Content content);
    }
}
