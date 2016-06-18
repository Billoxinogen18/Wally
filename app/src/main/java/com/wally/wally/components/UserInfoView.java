package com.wally.wally.components;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.annotation.DimenRes;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.api.GoogleApiClient;
import com.wally.wally.App;
import com.wally.wally.R;
import com.wally.wally.Utils;
import com.wally.wally.datacontroller.callbacks.Callback;
import com.wally.wally.datacontroller.user.User;
import com.wally.wally.userManager.SocialUser;
import com.wally.wally.userManager.UserManager;

/**
 * User info view that is simple profile picture with user name on it.
 * </p>
 * Better to use because of several advantages
 * <ul>
 * <li> Has all the views at once, thus reduces code size </li>
 * <li> Has small and normal size versions </li>
 * <li> Handles user setting and validating stuff </li>
 * </ul>
 */
public class UserInfoView extends LinearLayout {
    private static final String TAG = UserInfoView.class.getSimpleName();

    private ImageView mUserImage;
    private TextView mUserName;

    private SocialUser mUser;
    // This variable is to check if new request comes while loading user.
    private String mLastUserId;

    public UserInfoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);

    }

    public UserInfoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    @SuppressLint("SetTextI18n")
    private void init(Context context, AttributeSet attrs) {
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.UserInfoView,
                0, 0);

        try {
            if (a.hasValue(R.styleable.UserInfoView_custom_layout)) {
                int customLayoutRes = a.getResourceId(R.styleable.UserInfoView_custom_layout, -1);
                initFromCustomLayout(customLayoutRes);
            } else {
                int sizeEnum = a.getInt(R.styleable.UserInfoView_view_size, 0);
                initWithSize(sizeEnum);
            }
        } finally {
            a.recycle();
        }

        // Just show sample data in preview
        if (isInEditMode()) {
            mUserImage.setImageResource(R.drawable.sample_user_image);
            mUserName.setText("Giorgi Gogiashvili");
        }
    }

    private void initFromCustomLayout(int layoutId) {
        inflate(getContext(), layoutId, this);
        mUserImage = (ImageView) findViewById(R.id.iv_owner_image);
        mUserName = (TextView) findViewById(R.id.owner_name);
    }

    private void initWithSize(int sizeEnum) {
        Context context = getContext();
        // Choose resources according to xml attribute
        @DimenRes int imageSizeResId;
        @DimenRes int textSizeResId;
        switch (sizeEnum) {
            case 0:
                imageSizeResId = R.dimen.normal_profile_picture_size;
                textSizeResId = R.dimen.normal_profile_text_size;
                break;
            case 1:
                imageSizeResId = R.dimen.small_profile_picture_size;
                textSizeResId = R.dimen.small_profile_text_size;
                break;
            default:
                throw new IllegalArgumentException("unknown size Enumeration");
        }
        // Setup Image
        mUserImage = new ImageView(context);
        int imageSize = context.getResources().getDimensionPixelSize(imageSizeResId);
        // set image size
        mUserImage.setLayoutParams(new LinearLayout.LayoutParams(imageSize, imageSize));

        float textSize = context.getResources().getDimension(textSizeResId);
        mUserName = new TextView(context);
        mUserName.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        mUserName.setTextColor(Color.BLACK);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(imageSize / 4, 0, 0, 0);
        mUserName.setLayoutParams(lp);
        setGravity(Gravity.CENTER_VERTICAL);

        // Now add views to parent
        addView(mUserImage);
        addView(mUserName);
    }

    public void setAnonymousUser() {
        mLastUserId = null;
        mUser = null;
        mUserName.setText(R.string.anonymous);
        mUserImage.setImageResource(R.drawable.ic_anonymous_user_icon);
    }

    /**
     * Clears data from view
     */
    public void clearUser() {
        mLastUserId = null;
        mUser = null;
        mUserImage.setImageDrawable(null);
        mUserImage.setBackground(null);
        mUserName.setText(null);
    }

    /**
     * Same as {@link #loadAndSetUser(String, GoogleApiClient)}
     * but with additional checks for anonymous users. <br/>
     * If is current user and is anonymous it sets current user.<br/>
     * If is other user and is anonymous , just sets anonymous.<br/>
     * If is normal user sets normally.<br/>
     *
     * @param isAnonymous true if you want to set as anonymous (If it's not current user) <br/>
     */
    public void loadAndSetUser(final String userId, boolean isAnonymous, final GoogleApiClient googleApiClient) {
        boolean isOwn = Utils.isCurrentUser(userId);
        if (isAnonymous && !isOwn) {
            setAnonymousUser();
        } else {
            if (isOwn) {
                setUser(App.getInstance().getUserManager().getUser());
            } else {
                loadAndSetUser(userId, googleApiClient);
            }
        }
    }

    /**
     * Loads data from network and sets on user.
     * Also if new request comes while downloading, old request is stopped and new is set as active.
     *
     * @param userId          user Id to set.
     * @param googleApiClient already set up google api client.
     */
    public void loadAndSetUser(final String userId, final GoogleApiClient googleApiClient) {
        clearUser();
        mLastUserId = userId;

        mUserImage.setImageResource(R.drawable.ic_account_circle_black_24dp);
        mUserName.setText(R.string.loading);
        App.getInstance().getDataController().fetchUser(userId, new Callback<User>() {
            @Override
            public void onResult(User result) {
                // Check if new request came while we were loading user data.
                if (!TextUtils.equals(mLastUserId, userId) || result == null) {
                    return;
                }
                App.getInstance().getUserManager().loadUser(result, googleApiClient, new UserManager.UserLoadListener() {
                    @Override
                    public void onUserLoad(SocialUser user) {
                        // Second check here
                        if (!TextUtils.equals(mLastUserId, userId) || user == null) {
                            return;
                        }
                        setUser(user);
                    }

                    @Override
                    public void onUserLoadFailed() {

                    }
                });
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "onError: ", e);
            }
        });
    }

    public SocialUser getUser() {
        return mUser;
    }

    /**
     * This method directly sets user on view, without loading
     *
     * @param user user to set
     */
    public void setUser(SocialUser user) {
        mLastUserId = null;
        mUser = user;
        if (user == null) {
            clearUser();
            return;
        }
        if (TextUtils.isEmpty(user.getAvatarUrl())) {
            mUserImage.setImageDrawable(null);
            mUserImage.setBackground(null);
        } else {
            // TODO optimize avatar image size
            Glide.with(getContext())
                    .load(user.getAvatarUrl())
                    .crossFade()
                    .fitCenter()
                    .thumbnail(0.1f)
                    .placeholder(R.drawable.ic_account_circle_black_24dp)
                    .transform(new CircleTransform(getContext()))
                    .into(mUserImage);
        }
        mUserName.setText(user.getDisplayName());
    }
}