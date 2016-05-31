package com.wally.wally.components;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.bumptech.glide.Glide;
import com.wally.wally.R;
import com.wally.wally.userManager.SocialUser;

/**
 * Created by Xato on 5/31/2016.
 */
public class CircleUserView extends RelativeLayout {

    private boolean mCheckedStatus;
    private SocialUser mSocialUser;

    private View mTickView;
    private ImageView mAvatarImageView;
    private TextView mNameTextView;

    public CircleUserView(Context context) {
        super(context);
        init();
    }

    public CircleUserView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();

    }

    public CircleUserView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public void setChecked(boolean checked){
        mCheckedStatus = checked;

        mTickView.setVisibility(checked ? VISIBLE : GONE);

    }

    public boolean isChecked(){
        return mCheckedStatus;
    }

    public void setUser(SocialUser user){
        mSocialUser = user;
        setData();
    }

    private void setData() {
        Glide.with(getContext())
                .load(mSocialUser.getAvatarUrl())
                .transform(new CircleTransform(getContext()))
                .into(mAvatarImageView);

        mNameTextView.setText(mSocialUser.getDisplayName());
    }

    private void init() {
        inflate(getContext(), R.layout.circle_user_view, this);

        mTickView = findViewById(R.id.tick_view);
        mAvatarImageView = (ImageView) findViewById(R.id.avatar_image_view);
        mNameTextView = (TextView) findViewById(R.id.name_text_view);
    }
}
