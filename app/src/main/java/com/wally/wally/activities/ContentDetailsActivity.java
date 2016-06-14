package com.wally.wally.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.plus.Plus;
import com.wally.wally.App;
import com.wally.wally.R;
import com.wally.wally.components.CircleTransform;
import com.wally.wally.datacontroller.callbacks.Callback;
import com.wally.wally.datacontroller.content.Content;
import com.wally.wally.datacontroller.user.User;
import com.wally.wally.userManager.SocialUser;
import com.wally.wally.userManager.UserManager;

public class ContentDetailsActivity extends AppCompatActivity {

    private static final String KEY_CONTENT = "KEY_CONTENT";
    private static final String TAG = ContentDetailsActivity.class.getSimpleName();

    private GoogleApiClient mGoogleApiClient;
    private Content mContent;
    private boolean mIsOwnPost;

    private ImageView mOwnerImage;
    private TextView mOwnerName;
    private ImageView mNoteImage;
    private TextView mNoteTitle;
    private TextView mNote;
    private View mCard;

    public static Intent newIntent(Context from, Content content) {
        Intent i = new Intent(from, ContentDetailsActivity.class);
        i.putExtra(KEY_CONTENT, content);
        return i;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content_details);

        mContent = (Content) getIntent().getSerializableExtra(KEY_CONTENT);

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addScope(Plus.SCOPE_PLUS_PROFILE)
                    .addApi(Plus.API)
                    .build();
        }

        User currentUser = App.getInstance().getDataController().getCurrentUser();
        if (currentUser.getId() != null) {
            mIsOwnPost = TextUtils.equals(currentUser.getId().getId(), mContent.getAuthorId());
        }
        bindViews();
        initViewsWithContent();
    }

    private void bindViews() {
        mOwnerImage = (ImageView) findViewById(R.id.iv_owner_image);
        mOwnerName = (TextView) findViewById(R.id.tv_owner_name);
        mNote = (TextView) findViewById(R.id.tv_note);
        mNoteTitle = (TextView) findViewById(R.id.tv_title);
        mNoteImage = (ImageView) findViewById(R.id.iv_note_image);
        mCard = findViewById(R.id.card);
    }

    private void initViewsWithContent() {
        if (!TextUtils.isEmpty(mContent.getImageUri())) {
            Glide.with(getBaseContext())
                    .load(mContent.getImageUri())
                    .fitCenter()
                    .into(mNoteImage);

            mNoteImage.setVisibility(View.VISIBLE);
        } else {
            mNoteImage.setVisibility(View.GONE);
        }

        if (mContent.getColor() != null) {
            mCard.setBackgroundColor(mContent.getColor());
        }
        mNoteTitle.setText(mContent.getTitle());
        mNoteTitle.setVisibility(TextUtils.isEmpty(mContent.getTitle()) ? View.GONE : View.VISIBLE);

        mNote.setText(mContent.getNote());
        mNote.setVisibility(TextUtils.isEmpty(mContent.getNote()) ? View.GONE : View.VISIBLE);

        if (TextUtils.isEmpty(mContent.getAuthorId())) {
            mOwnerImage.setVisibility(View.GONE);
            mOwnerName.setVisibility(View.GONE);
            return;
        }
        if (mIsOwnPost) {
            onUserLoaded(App.getInstance().getUserManager().getUser());
            return;
        }
        // Load user if is other than current
        App.getInstance().getDataController().fetchUser(mContent.getAuthorId(), new Callback<User>() {
            @Override
            public void onResult(User result) {
                App.getInstance().getUserManager().loadUser(result, mGoogleApiClient,
                        new UserManager.UserLoadListener() {
                            @Override
                            public void onUserLoad(SocialUser user) {
                                onUserLoaded(user);
                            }

                            @Override
                            public void onUserLoadFailed() {
                                onUserLoaded(null);
                            }
                        });
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "onError: ", e);
            }
        });
    }

    private void onUserLoaded(@Nullable SocialUser user) {
        if (user == null) {
            return;
        }
        if (!TextUtils.isEmpty(user.getAvatarUrl())) {
            mOwnerImage.setVisibility(View.VISIBLE);
            // TODO optimize size
            Glide.with(getBaseContext())
                    .load(user.getAvatarUrl())
                    .fitCenter()
                    .transform(new CircleTransform(getBaseContext()))
                    .into(mOwnerImage);
        }
        mOwnerName.setVisibility(View.VISIBLE);
        mOwnerName.setText(user.getDisplayName());
    }

    public void onBackPress(View view) {
        onBackPressed();
    }
}
