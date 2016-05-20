package com.wally.wally.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.wally.wally.App;
import com.wally.wally.R;
import com.wally.wally.Utils;
import com.wally.wally.components.CircleTransform;
import com.wally.wally.datacontroller.callbacks.FetchResultCallback;
import com.wally.wally.datacontroller.content.Content;
import com.wally.wally.fragments.NewContentDialogFragment;
import com.wally.wally.fragments.PreviewContentDialogFragment;
import com.wally.wally.userManager.SocialUser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ProfileActivity extends AppCompatActivity implements AppBarLayout.OnOffsetChangedListener, FetchResultCallback, NewContentDialogFragment.NewContentDialogListener {

    private static final String TAG = ProfileActivity.class.getSimpleName();

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
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(
                getGridColumnCount(), StaggeredGridLayoutManager.VERTICAL));
        recyclerView.setAdapter(new ContentAdapter());

        mContentAdapter = new ContentAdapter();
        recyclerView.setAdapter(mContentAdapter);
        // TODO add loading, tryAgain, empty screen
    }

    /**
     * @return optimal quantity based on rotation and size
     */
    private int getGridColumnCount() {
        return (int) (Utils.getScreenWidthDpi(this) / 170);
    }

    /**
     * Called when data is loaded
     *
     * @param result loaded content of the current user
     */
    @Override
    public void onResult(Collection<Content> result) {
        Log.d(TAG, "Loaded content size = [" + result.size() + "]");
        ArrayList<Content> data = new ArrayList<>(result.size());
        data.addAll(result);
        // TODO delete dumb data
        data.add(new Content().withId("0").withNote("Hi there my name is...").withTitle("Sample note"));
        data.add(new Content().withId("5").withNote(getString(R.string.large_text)).withTitle("თქვენ შიგ ხო არ გაქვთ რა ლიმიტი").withImageUri("http://i.imgur.com/RRUe0Mo.png"));
        data.add(new Content().withId("6").withNote(getString(R.string.large_text)).withTitle("Sample note Title here"));
        data.add(new Content().withId("7").withNote("Hi there my name is John").withTitle("Sample note"));
        data.add(new Content().withId("8").withNote("Hi there my name is... I'm programmer here :S"));
        data.add(new Content().withId("9").withTitle("Sample note Only title"));
        data.add(new Content().withId("10").withTitle("Sample note").withImageUri("http://www.keenthemes.com/preview/metronic/theme/assets/global/plugins/jcrop/demos/demo_files/image1.jpg"));

        mContentAdapter.setData(data);
    }

    @Override
    public void onError(Exception e) {
        // TODO get exception and show no internet UI or something.
        Log.e(TAG, "onError: " + e);
    }

    private void onDeleteContent(Content content) {
        mContentAdapter.removeItem(content);
        // TODO call db
    }

    private void onEditContent(Content content) {
        NewContentDialogFragment.newInstance(content).show(getSupportFragmentManager(), NewContentDialogFragment.TAG);
    }

    @Override
    public void onContentCreated(Content content, boolean isEditMode) {
        Log.d(TAG, "onContentCreated() called with: " + "content = [" + content + "], isEditMode = [" + isEditMode + "]");
        mContentAdapter.updateItem(content);
        // TODO call db
    }

    private void onContentClicked(Content content, View view) {
        PreviewContentDialogFragment pd = PreviewContentDialogFragment.newInstance(content);
        pd.show(getSupportFragmentManager(), PreviewContentDialogFragment.TAG);
    }

    private class ContentAdapter extends RecyclerView.Adapter<ContentAdapter.ViewHolder> {

        private List<Content> mData;

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            @SuppressLint("InflateParams") View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.layout_content_grid, null);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder vh, int position) {
            Content c = mData.get(position);

            if (!TextUtils.isEmpty(c.getImageUri())) {
                Glide.with(getBaseContext())
                        .load(c.getImageUri())
                        .fitCenter()
                        .into(vh.image);

                vh.image.setVisibility(View.VISIBLE);
            } else {
                vh.image.setVisibility(View.GONE);
            }
            vh.title.setText(c.getTitle());
            vh.title.setVisibility(TextUtils.isEmpty(c.getTitle()) ? View.GONE : View.VISIBLE);

            vh.note.setText(c.getNote());
            vh.note.setVisibility(TextUtils.isEmpty(c.getNote()) ? View.GONE : View.VISIBLE);
        }

        @Override
        public int getItemCount() {
            return mData == null ? 0 : mData.size();
        }

        public void setData(List<Content> data) {
            mData = data;
            // this thing adds insert animation and updates data
            notifyItemRangeInserted(0, getItemCount());
        }

        public int getItemPosition(Content content) {
            return mData.indexOf(content);
        }

        public void removeItem(Content content) {
            int pos = getItemPosition(content);
            mData.remove(pos);
            notifyItemRemoved(pos);
        }

        public void updateItem(Content content) {
            int pos = getItemPosition(content);
            mData.set(pos, content);
            notifyItemChanged(pos);
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            public TextView note;
            public TextView title;
            public ImageView image;

            public ViewHolder(View v) {
                super(v);
                note = (TextView) v.findViewById(R.id.tv_note);
                title = (TextView) v.findViewById(R.id.tv_title);
                image = (ImageView) v.findViewById(R.id.image_view);
                v.findViewById(R.id.btn_more_settings).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showPopup(v);
                    }
                });

                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Content content = mData.get(getAdapterPosition());
                        onContentClicked(content, v);
                    }
                });
            }

            public void showPopup(View v) {
                PopupMenu popup = new PopupMenu(v.getContext(), v);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.grid_content_actions, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        Content content = mData.get(getAdapterPosition());
                        switch (item.getItemId()) {
                            case R.id.delete:
                                onDeleteContent(content);
                                break;
                            case R.id.edit:
                                onEditContent(content);
                                break;
                        }
                        return false;
                    }
                });
                popup.show();
            }
        }
    }
}
