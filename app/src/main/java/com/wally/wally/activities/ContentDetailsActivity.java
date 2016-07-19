package com.wally.wally.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.plus.Plus;
import com.mapbox.mapboxsdk.MapboxAccountManager;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.wally.wally.App;
import com.wally.wally.R;
import com.wally.wally.Utils;
import com.wally.wally.components.UserInfoView;
import com.wally.wally.datacontroller.content.Content;
import com.wally.wally.datacontroller.user.User;
import com.wally.wally.fragments.NewContentDialogFragment;

public class ContentDetailsActivity extends AppCompatActivity implements OnMapReadyCallback, NewContentDialogFragment.NewContentDialogListener {

    private static final String KEY_CONTENT = "KEY_CONTENT";
    private static final String TAG = ContentDetailsActivity.class.getSimpleName();

    private GoogleApiClient mGoogleApiClient;
    private Content mContent;
    private boolean mIsOwnPost;

    private UserInfoView mUserInfoView;
    private ImageView mNoteImage;
    private TextView mNoteTitle;
    private TextView mNote;
    private CardView mCard;

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

        if (!mContent.getVisibility().isPreviewVisible()) {
            finish();
            return;
        }
        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addScope(Plus.SCOPE_PLUS_PROFILE)
                    .addApi(Plus.API)
                    .build();
        }

        MapView mapFragment = (MapView) findViewById(R.id.map);

        MapboxAccountManager.start(getBaseContext(), "pk.eyJ1Ijoid2FsbHlub3RlcyIsImEiOiJjaXFydnB1OHYwMDg3aHRubTYyZXNnZmo3In0.fvgLu8rLKJdX0j8N3QFAwg");

        mapFragment.getMapAsync(this);

        User currentUser = App.getInstance().getDataController().getCurrentUser();
        if (currentUser.getId() != null) {
            mIsOwnPost = TextUtils.equals(currentUser.getId().getId(), mContent.getAuthorId());
            invalidateOptionsMenu();
        }

        bindViews();
        initViewsWithContent();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_content_details, menu);
        menu.findItem(R.id.edit).setVisible(mIsOwnPost);
        menu.findItem(R.id.delete).setVisible(mIsOwnPost);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit:
                if (mIsOwnPost) {
                    onEditContent();
                }
                return true;
            case R.id.delete:
                if (mIsOwnPost) {
                    onDeleteContent();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    private void bindViews() {
        mUserInfoView = (UserInfoView) findViewById(R.id.user_info_view);
        mNote = (TextView) findViewById(R.id.tv_note);
        mNoteTitle = (TextView) findViewById(R.id.tv_title);
        mNoteImage = (ImageView) findViewById(R.id.iv_note_image);
        mCard = (CardView) findViewById(R.id.card);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setTitle(null);
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
            mCard.setCardBackgroundColor(mContent.getColor());
        }
        mNoteTitle.setText(mContent.getTitle());
        mNoteTitle.setVisibility(TextUtils.isEmpty(mContent.getTitle()) ? View.GONE : View.VISIBLE);

        mNote.setText(mContent.getNote());
        mNote.setVisibility(TextUtils.isEmpty(mContent.getNote()) ? View.GONE : View.VISIBLE);

        mUserInfoView.loadAndSetUser(
                mContent.getAuthorId(),
                mContent.getVisibility().isAuthorAnonymous(),
                mGoogleApiClient);
    }

    public void onBackPress(View view) {
        onBackPressed();
    }

    @Override
    public void onMapReady(MapboxMap googleMap) {
        LatLng pos = Utils.serializableLatLngToLatLng(mContent.getLocation());
        googleMap.addMarker(new MarkerOptions()
                .position(pos)
                .title(mContent.getTitle()));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 10f));
    }

    private void onDeleteContent() {
        App.getInstance().getDataController().delete(mContent);
        finish();
    }

    private void onEditContent() {
        NewContentDialogFragment.newInstance(mContent)
                .show(getSupportFragmentManager(), NewContentDialogFragment.TAG);
    }


    @Override
    public void onContentCreated(Content content, boolean isEditMode) {
        mContent = content;
        App.getInstance().getDataController().save(content);
        initViewsWithContent();
    }
}
