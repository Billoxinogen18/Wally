package com.wally.wally.controllers.map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.api.GoogleApiClient;
import com.wally.wally.R;
import com.wally.wally.Utils;
import com.wally.wally.components.UserInfoView;
import com.wally.wally.datacontroller.content.Content;
import com.wally.wally.datacontroller.content.Visibility;
import com.wally.wally.userManager.SocialUser;

/**
 * Created by Meravici on 6/20/2016. yea
 */
public class ContentListViewItem extends LinearLayout {

    @SuppressWarnings("unused")
    private static final String TAG = ContentListViewItem.class.getSimpleName();

    private CardView mCardView;
    private UserInfoView mUserInfoView;
    private ImageView mNoteImageView;
    private TextView mTitleView;
    private TextView mNoteView;
    private TextView mContentPositionVIew;
    private View mPreviewNotVisible;
    private OnClickListener onClickListener;
    private Content mContent;

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

    private void init() {
        inflate(getContext(), R.layout.content_listview_item, this);
        setOrientation(HORIZONTAL);
        setBackgroundColor(Color.TRANSPARENT);

        mCardView = (CardView) findViewById(R.id.note_card);
        mUserInfoView = (UserInfoView) findViewById(R.id.user_info_view);
        mNoteImageView = (ImageView) findViewById(R.id.iv_note_image);
        mTitleView = (TextView) findViewById(R.id.tv_title);
        mNoteView = (TextView) findViewById(R.id.tv_note);
        mPreviewNotVisible = findViewById(R.id.preview_not_visible);

        mContentPositionVIew = new TextView(getContext());
        mContentPositionVIew.setTextColor(Color.WHITE);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                Utils.dpToPx(getContext(), 30),
                ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, Utils.dpToPx(getContext(), 28), Utils.dpToPx(getContext(), 8), 0);
        mContentPositionVIew.setLayoutParams(lp);
        mContentPositionVIew.setGravity(Gravity.CENTER);
        addView(mContentPositionVIew, 0);

        mUserInfoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickListener.onProfileClicked(mUserInfoView.getUser());
            }
        });

        setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickListener.onContentClicked(mContent);
            }
        });
    }

    public void setContent(Content content, GoogleApiClient googleApiClient) {
        clear();
        this.mContent = content;

        if (content.getTextColor() == null) {
            content.withTextColor(Color.BLACK);
        }
        mUserInfoView.setTextColor(content.getTextColor());
        mNoteView.setTextColor(content.getTextColor());
        mTitleView.setTextColor(content.getTextColor());

        mUserInfoView.loadAndSetUser(
                content.getAuthorId(),
                content.getVisibility().isAuthorAnonymous(), googleApiClient);
        mUserInfoView.setStatus(createUserStatus(content));

        if (content.getColor() != null) {
            mCardView.setCardBackgroundColor(content.getColor());
        } else {
            mCardView.setCardBackgroundColor(Color.WHITE);
        }

        boolean isOwn = Utils.isCurrentUser(content.getAuthorId());
        // Check if user can see mContent preview
        if (!content.getVisibility().isPreviewVisible() && !isOwn) {
            mPreviewNotVisible.setVisibility(VISIBLE);
            return;
        } else {
            mPreviewNotVisible.setVisibility(GONE);
        }
        if (!TextUtils.isEmpty(content.getImageUri())) {
            Glide.with(getContext())
                    .load(content.getImageUri())
                    .thumbnail(0.1f)
                    .fitCenter()
                    .crossFade()
                    .placeholder(R.drawable.ic_image_placeholer)
                    .dontAnimate()
                    .into(mNoteImageView);

            mNoteImageView.setVisibility(View.VISIBLE);
        } else {
            mNoteImageView.setVisibility(View.GONE);
        }

        mTitleView.setText(content.getTitle());
        mTitleView.setVisibility(TextUtils.isEmpty(content.getTitle()) ? View.GONE : View.VISIBLE);

        mNoteView.setText(content.getNote());
        mNoteView.setVisibility(TextUtils.isEmpty(content.getNote()) ? View.GONE : View.VISIBLE);
    }

    public ImageSpan getImageSpanForStatus(Drawable d) {
        d.setBounds(0, 0, Utils.dpToPx(getContext(), 13), Utils.dpToPx(getContext(), 13));
        return new ImageSpan(d, ImageSpan.ALIGN_BASELINE) {
            public void draw(Canvas canvas, CharSequence text, int start,
                             int end, float x, int top, int y, int bottom,
                             Paint paint) {
                Drawable b = getDrawable();
                canvas.save();

                int transY = bottom - b.getBounds().bottom - 3;
                // this is the key
                transY -= paint.getFontMetricsInt().descent / 2;

                canvas.translate(x, transY);
                b.draw(canvas);
                canvas.restore();
            }
        };
    }

    public CharSequence createUserStatus(Content content) {
        int sv = content.getVisibility().getSocialVisibility().getMode();

        String str = String.format("! %s   @ %s",
                Visibility.SocialVisibility.getStringRepresentation(sv),
                Utils.formatDateSmartShort(getContext(), content.getCreationDate().getTime()));
        int visibilityDrawableIndex = str.indexOf('!');
        int timeDrawableIndex = str.indexOf('@');

        SpannableString ss = new SpannableString(str);


        Drawable visibilityDrawable = ContextCompat.getDrawable(getContext(), Visibility.SocialVisibility.toSmallDrawableRes(sv));
        visibilityDrawable = Utils.tintDrawable(visibilityDrawable, content.getTextColor());
        ss.setSpan(
                getImageSpanForStatus(visibilityDrawable),
                visibilityDrawableIndex,
                visibilityDrawableIndex + 1,
                Spannable.SPAN_INCLUSIVE_EXCLUSIVE);

        Drawable timeDrawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_time_24dp);
        timeDrawable = Utils.tintDrawable(timeDrawable, content.getTextColor());
        ss.setSpan(getImageSpanForStatus(timeDrawable),
                timeDrawableIndex,
                timeDrawableIndex + 1,
                Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        return ss;
    }

    @SuppressLint("SetTextI18n")
    public void setPosition(int position) {
        mContentPositionVIew.setText("" + position);
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public void showUserInfo(boolean show) {
        mUserInfoView.setVisibility(show ? VISIBLE : View.GONE);
    }

    /**
     * Clears mContent out of View
     */
    public void clear() {
        mCardView.setCardBackgroundColor(Color.WHITE);
        mNoteImageView.setImageDrawable(null);
        mNoteImageView.setBackground(null);
        mNoteImageView.setVisibility(View.VISIBLE);

        mTitleView.setText(null);
        mTitleView.setVisibility(View.VISIBLE);
        mNoteView.setText(null);
        mNoteView.setVisibility(View.VISIBLE);
        mContentPositionVIew.setText(null);

        mUserInfoView.clearUser();
    }


    public interface OnClickListener {
        void onProfileClicked(SocialUser user);

        void onContentClicked(Content content);
    }
}
