package com.wally.wally.components;

import android.animation.Animator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnimationUtils;
import android.view.animation.BounceInterpolator;
import android.view.animation.LinearInterpolator;
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
//        mTickView.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.bounce_scale));
//        mTickView.setVisibility(checked ? VISIBLE : GONE);



        if(checked){
            mTickView.setScaleX(0);
            mTickView.setScaleY(0);
            mTickView.setVisibility(VISIBLE);
            mTickView.animate().scaleX(1).scaleY(1).setInterpolator(new BounceInterpolator());
        }else {
            mTickView.animate().scaleX(0).scaleY(0).setInterpolator(new AccelerateDecelerateInterpolator());
        }


    }

    public boolean isChecked(){
        return mCheckedStatus;
    }

    public void setUser(SocialUser user){
        reset();
        mSocialUser = user;
        setData();
    }

    public SocialUser getUser(){
        return mSocialUser;
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

    private void reset(){
        mTickView.setScaleX(0);
        mTickView.setScaleY(0);
        mAvatarImageView.setImageBitmap(null); //TODO set default image
        mNameTextView.setText("");
        mCheckedStatus = false;
    }
}
