package com.wally.wally.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.wally.wally.App;
import com.wally.wally.R;
import com.wally.wally.components.CircleTransform;
import com.wally.wally.datacontroller.callbacks.FetchResultCallback;
import com.wally.wally.datacontroller.content.Content;
import com.wally.wally.userManager.SocialUser;

import java.util.Collection;

public class ProfileActivity extends AppCompatActivity implements AppBarLayout.OnOffsetChangedListener, FetchResultCallback {

    private static final float PERCENTAGE_TO_SHOW_TITLE_AT_TOOLBAR = 0.9f;
    private static final float PERCENTAGE_TO_HIDE_TITLE_DETAILS = 0.3f;
    private static final int ALPHA_ANIMATIONS_DURATION = 200;
    private static final String SOCIAL_USER = "socialUser";
    private boolean mIsTheTitleVisible = false;
    private boolean mIsTheTitleContainerVisible = true;
    private LinearLayout mTitleContainer;
    private TextView mTitle;
    private TextView mTitleBar;
    private ImageView mAvatarImage;
    private ImageView mCoverImage;
    private AppBarLayout mAppBarLayout;
    private Toolbar mToolbar;
    private SocialUser mUser;

    private ContentAdapter mContentAdapter;

    public static Intent newIntent(Context context, @Nullable SocialUser user) {
        Intent i = new Intent(context, ProfileActivity.class);
        i.putExtra(SOCIAL_USER, user);
        return i;
    }

    public static void startAlphaAnimation(View v, long duration, int visibility) {
        AlphaAnimation alphaAnimation = (visibility == View.VISIBLE)
                ? new AlphaAnimation(0f, 1f)
                : new AlphaAnimation(1f, 0f);

        alphaAnimation.setDuration(duration);
        alphaAnimation.setFillAfter(true);
        v.startAnimation(alphaAnimation);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        extractUserFromBundle(getIntent().getExtras());

        bindViews();
        setData();

        mAppBarLayout.addOnOffsetChangedListener(this);
        mToolbar.inflateMenu(R.menu.menu_main);
        startAlphaAnimation(mTitle, 0, View.INVISIBLE);

        initGridView();
    }

    private void setData() {
        mTitle.setText(mUser.getDisplayName());
        mTitleBar.setText(mUser.getDisplayName());

        Glide.with(getBaseContext())
                .load(mUser.getAvatarUrl())
                .transform(new CircleTransform(getBaseContext()))
                .into(mAvatarImage);

        Glide.with(getBaseContext())
                .load(mUser.getCoverUrl())
                .into(mCoverImage);
    }

    private void bindViews() {
        mToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        mTitle = (TextView) findViewById(R.id.main_textview_title);
        mTitleBar = (TextView) findViewById(R.id.main_title);
        mTitleContainer = (LinearLayout) findViewById(R.id.main_linearlayout_title);
        mAppBarLayout = (AppBarLayout) findViewById(R.id.main_appbar);
        mAvatarImage = (ImageView) findViewById(R.id.main_imageview_avatar);
        mCoverImage = (ImageView) findViewById(R.id.main_imageview_placeholder);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int offset) {
        int maxScroll = appBarLayout.getTotalScrollRange();
        float percentage = (float) Math.abs(offset) / (float) maxScroll;

        handleAlphaOnTitle(percentage);
        handleToolbarTitleVisibility(percentage);
    }

    private void handleToolbarTitleVisibility(float percentage) {
        if (percentage >= PERCENTAGE_TO_SHOW_TITLE_AT_TOOLBAR) {

            if (!mIsTheTitleVisible) {
                startAlphaAnimation(mTitle, ALPHA_ANIMATIONS_DURATION, View.VISIBLE);
                mIsTheTitleVisible = true;
            }

        } else {

            if (mIsTheTitleVisible) {
                startAlphaAnimation(mTitle, ALPHA_ANIMATIONS_DURATION, View.INVISIBLE);
                mIsTheTitleVisible = false;
            }
        }
    }

    private void handleAlphaOnTitle(float percentage) {
        if (percentage >= PERCENTAGE_TO_HIDE_TITLE_DETAILS) {
            if (mIsTheTitleContainerVisible) {
                startAlphaAnimation(mTitleContainer, ALPHA_ANIMATIONS_DURATION, View.INVISIBLE);
                mIsTheTitleContainerVisible = false;
            }

        } else {

            if (!mIsTheTitleContainerVisible) {
                startAlphaAnimation(mTitleContainer, ALPHA_ANIMATIONS_DURATION, View.VISIBLE);
                mIsTheTitleContainerVisible = true;
            }
        }
    }

    private void extractUserFromBundle(Bundle extras) {
        mUser = (SocialUser) extras.getSerializable(SOCIAL_USER);
    }

    @SuppressWarnings("ConstantConditions")
    private void initGridView() {
        App app = App.getInstance();
        app.getDataController().fetchByAuthor(app.getUser().getBaseUser(), this);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL));
        recyclerView.setAdapter(new ContentAdapter());

        mContentAdapter = new ContentAdapter();
        recyclerView.setAdapter(mContentAdapter);

        // TODO add loading, tryAgain, empty screen
    }


    /**
     * Called when data is loaded
     *
     * @param result loaded content of the current user
     */
    @Override
    public void onResult(Collection<Content> result) {
        mContentAdapter.setData(result);

    }

    @Override
    public void onError(Exception e) {
        // TODO get exception and show no internet UI or something.
    }

    private class ContentAdapter extends RecyclerView.Adapter<ContentAdapter.ViewHolder> {

        private Collection<Content> mData;

        public void setData(Collection<Content> data) {
            mData = data;
            // this thing adds insert animation and updates data
            notifyItemRangeInserted(0, getItemCount());
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            @SuppressLint("InflateParams") View v = LayoutInflater.from(getBaseContext())
                    .inflate(R.layout.layout_content_grid, null);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {

        }

        @Override
        public int getItemCount() {
            return mData == null ? 0 : mData.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            public ViewHolder(View itemView) {
                super(itemView);
            }
        }
    }
}
