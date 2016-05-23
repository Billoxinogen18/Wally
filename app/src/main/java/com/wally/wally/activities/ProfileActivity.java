package com.wally.wally.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
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
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
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


public class ProfileActivity extends AppCompatActivity implements FetchResultCallback, NewContentDialogFragment.NewContentDialogListener, AppBarLayout.OnOffsetChangedListener {

    private static final String TAG = ProfileActivity.class.getSimpleName();
    private static final String ARG_SOCIAL_USER = "ARG_SOCIAL_USER";
    private static final int PERCENTAGE_TO_ANIMATE_AVATAR = 5;
    private ImageView mAvatarImage;
    private ImageView mCoverImage;
    private CollapsingToolbarLayout mCollapseToolbar;
    private Toolbar mToolbar;
    private boolean mIsAvatarShown = true;

    private int mMaxScrollSize;
    private ContentAdapter mContentAdapter;

    private SocialUser mUser;
    private int mSortType;
    private int mDrawableTintColor = -1;

    public static Intent newIntent(Context context, @Nullable SocialUser user) {
        Intent i = new Intent(context, ProfileActivity.class);
        i.putExtra(ARG_SOCIAL_USER, user);
        return i;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        if (savedInstanceState != null) {
            mSortType = savedInstanceState.getInt("mSortType");
        } else {
            mSortType = 0;
        }

        extractUserFromBundle(getIntent().getExtras());
        initViews();
        setData();
        initGridView();
    }

    private void setData() {
        mCollapseToolbar.setTitle(mUser.getDisplayName());

        if (!TextUtils.isEmpty(mUser.getAvatarUrl())) {
            Glide.with(getBaseContext())
                    .load(mUser.getAvatarUrl())
                    .transform(new CircleTransform(getBaseContext()))
                    .into(mAvatarImage);
        }

        if (!TextUtils.isEmpty(mUser.getCoverUrl())) {
            Glide.with(getBaseContext())
                    .load(mUser.getCoverUrl())
                    .asBitmap()
                    //.override(1500, 1500)
                    .listener(new RequestListener<String, Bitmap>() {
                        @Override
                        public boolean onException(Exception e, String model, Target<Bitmap> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            Log.wtf(TAG, "onResourceReady: " + resource.getWidth() + "X" + resource.getHeight());

                            int left = resource.getWidth() / 2 - 100;
                            int right = resource.getWidth() / 2 + 100;
                            if (left < 0) {
                                left = 0;
                                right = resource.getWidth() - 1;
                            }
                            Palette.from(resource).clearFilters()
                                    // Get colors only from top side of the image
                                    .setRegion(left, 0, right, Math.min(resource.getHeight() - 1, 200))
                                    .generate(new Palette.PaletteAsyncListener() {

                                        @Override
                                        public void onGenerated(Palette palette) {
                                            Palette.Swatch darkMuted = palette.getDarkMutedSwatch();
                                            Palette.Swatch lightMuted = palette.getLightMutedSwatch();
                                            Palette.Swatch swatch = darkMuted != null ? darkMuted :
                                                    lightMuted != null ?
                                                            lightMuted : Utils.getMostPopulousSwatch(palette);
                                            if (swatch == null) {
                                                return;
                                            }
                                            boolean isDark = swatch.getHsl()[2] < 0.5f;

                                            int scrimColor = Utils.modifyAlpha(swatch.getRgb(), 255);

                                            int collapsedTitleColor = Utils.modifyAlpha(swatch.getTitleTextColor(), 200);
                                            int expandedTitleColor = Utils.modifyAlpha(swatch.getTitleTextColor(), 240);

                                            mCollapseToolbar.setStatusBarScrimColor(scrimColor);
                                            mCollapseToolbar.setContentScrimColor(scrimColor);
                                            mCollapseToolbar.setCollapsedTitleTextColor(collapsedTitleColor);
                                            mCollapseToolbar.setExpandedTitleColor(expandedTitleColor);

                                            int darkTint = ContextCompat.getColor(getBaseContext(), R.color.cover_dark_tint);
                                            int lightTint = ContextCompat.getColor(getBaseContext(), R.color.cover_light_tint);
                                            mCoverImage.setColorFilter(isDark ? darkTint : lightTint);
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                                getWindow().setStatusBarColor(Utils.scrimify(scrimColor, isDark, 0.08f));
                                            }
                                            mDrawableTintColor = expandedTitleColor;
                                            // Invalidate menu to recreate menu items.
                                            invalidateOptionsMenu();
                                        }
                                    });
                            return false;
                        }
                    }).into(mCoverImage);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("mSortType", mSortType);
    }

    @SuppressWarnings("ConstantConditions")
    private void initViews() {
        mAvatarImage = (ImageView) findViewById(R.id.imageview_avatar);
        mCoverImage = (ImageView) findViewById(R.id.imageview_cover);

        mCollapseToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapse_toolbar_layout);
        AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.appbar);
        appBarLayout.addOnOffsetChangedListener(this);
        mMaxScrollSize = appBarLayout.getTotalScrollRange();

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_profile, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // Tint drawables
        if (mDrawableTintColor != -1) {
            for (int i = 0; i < menu.size(); i++) {
                Drawable drawable = menu.getItem(i).getIcon();
                if (drawable == null) {
                    continue;
                }
                menu.getItem(i).setIcon(Utils.tintDrawable(drawable, mDrawableTintColor));
            }
            Drawable upArrow = ContextCompat.getDrawable(this, R.drawable.ic_arrow_back_black_24dp);
            //noinspection ConstantConditions
            getSupportActionBar().setHomeAsUpIndicator(Utils.tintDrawable(upArrow, mDrawableTintColor));

            Drawable overflowIcon = mToolbar.getOverflowIcon();
            if (overflowIcon != null) {
                mToolbar.setOverflowIcon(Utils.tintDrawable(overflowIcon, mDrawableTintColor));
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_sort:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.sort_dialog_title);
                builder.setSingleChoiceItems(R.array.sort_options, mSortType, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        mSortType = item;
                        dialog.dismiss();
                        mContentAdapter.sort();
                    }
                });
                builder.show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int i) {
        /**
         * Just remove image on scroll
         */
        if (mMaxScrollSize == 0)
            mMaxScrollSize = appBarLayout.getTotalScrollRange();

        int percentage = (Math.abs(i)) * 100 / mMaxScrollSize;

        if (percentage >= PERCENTAGE_TO_ANIMATE_AVATAR && mIsAvatarShown) {
            mIsAvatarShown = false;
            mAvatarImage.animate().scaleY(0).scaleX(0).setDuration(200).start();
        }

        if (percentage <= PERCENTAGE_TO_ANIMATE_AVATAR && !mIsAvatarShown) {
            mIsAvatarShown = true;

            mAvatarImage.animate()
                    .scaleY(1).scaleX(1)
                    .start();
        }
    }


    private void extractUserFromBundle(Bundle extras) {
        mUser = (SocialUser) extras.getSerializable(ARG_SOCIAL_USER);
    }

    @SuppressWarnings("ConstantConditions")
    private void initGridView() {
        App app = App.getInstance();
        app.getDataController().fetchByAuthor(app.getUser().getBaseUser(), this);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerview_content);
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
        return (int) (Utils.getScreenWidthDpi(this) / 250);
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

    @SuppressWarnings("UnusedParameters")
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
            vh.image.setImageDrawable(null);
            vh.image.setBackground(null);
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

        public void sort() {
            if (mSortType == 0) {
                // sort by date
                // TODO sort
            } else {
                // sort by location
                // TODO Meravici
            }
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
