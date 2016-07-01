package com.wally.wally.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.plus.Plus;
import com.wally.wally.App;
import com.wally.wally.R;
import com.wally.wally.Utils;
import com.wally.wally.components.CircleTransform;
import com.wally.wally.components.ContentListView;
import com.wally.wally.components.ContentListViewItem;
import com.wally.wally.components.MapWindowAdapter;
import com.wally.wally.datacontroller.content.Content;
import com.wally.wally.datacontroller.fetchers.ContentFetcher;
import com.wally.wally.endlessScroll.ContentPagingRetriever;
import com.wally.wally.endlessScroll.EndlessRecyclerOnScrollListener;
import com.wally.wally.endlessScroll.EndlessScrollAdapter;
import com.wally.wally.endlessScroll.MarkerManager;
import com.wally.wally.userManager.SocialUser;

import java.util.List;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnCameraChangeListener,
        ContentPagingRetriever.ContentPageRetrieveListener,
        ContentListViewItem.OnClickListener, ContentListView.OnScrollSettleListener {

    private static final String TAG = MapsActivity.class.getSimpleName();
    private static final String KEY_USER = "USER";
    private static final int MY_LOCATION_REQUEST_CODE = 22;
    private static final int PAGE_LENGTH = 5;

    private SocialUser mUserProfile;

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;

    private ContentListView mContentListView;

    private ContentPagingRetriever mContentRetriever;
    private EndlessRecyclerOnScrollListener mContentScrollListener;

    private Handler mainThreadHandler;
    private MarkerManager mMarkerManager;
    private GoogleMap.CancelableCallback defaultCenterMyLocationCallback = new GoogleMap.CancelableCallback() {
        @Override
        public void onFinish() {
            loadContentNearLocation(mMap.getCameraPosition());
        }

        @Override
        public void onCancel() {
            loadContentNearLocation(mMap.getCameraPosition());
        }
    };

    /**
     * Start to see user profile
     */
    public static Intent newIntent(Context from, SocialUser user) {
        Intent i = new Intent(from, MapsActivity.class);
        i.putExtra(KEY_USER, user);
        return i;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        mainThreadHandler = new Handler(getMainLooper());

        mUserProfile = (SocialUser) getIntent().getSerializableExtra(KEY_USER);
        initFeedTitle();

        if (mUserProfile != null) {
            findViewById(R.id.update_area).setVisibility(View.GONE);
        }

        mContentListView = (ContentListView) findViewById(R.id.content_list_view);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addScope(Plus.SCOPE_PLUS_PROFILE)
                .addApi(Plus.API)
                .build();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
//        mapFragment.setRetainInstance(true);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnCameraChangeListener(this);
        mMap.setInfoWindowAdapter(new MapWindowAdapter(this));

        mMap.getUiSettings().setMapToolbarEnabled(false);
        if (Utils.checkLocationPermission(this)) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
        } else {
            requestLocationPermission();
        }

        mMarkerManager = new MarkerManager(getBaseContext(), mMap);
        mContentScrollListener = new EndlessRecyclerOnScrollListener(PAGE_LENGTH) {
            int markerCounter = 0;

            @Override
            public void onLoadNext() {
                mContentRetriever.loadNext();
            }

            @Override
            public void onVisibleItemChanged(final int previous, final int current) {
                mMarkerManager.selectMarkerAt(current);
                if ((markerCounter % 10 == 0 && mUserProfile != null) || (mContentRetriever.size() <= PAGE_LENGTH * 3 && current == 0)) {
                    markerCounter = 0;
                    centerMapOnVisibleMarkers();
                }
                markerCounter++;
            }
        };

        mContentListView.setLayoutManager(new LinearLayoutManager(this));
        mContentListView.addOnScrollListener(mContentScrollListener);
        mContentListView.setOnScrollSettleListener(this);


        if (mUserProfile == null)
            centerMapOnMyLocation(defaultCenterMyLocationCallback);
        else
            loadContentNearLocation(null);
    }

    @Override
    public void onContentClicked(Content content) {
        if (content.getVisibility().isPreviewVisible()) {
            startActivity(ContentDetailsActivity.newIntent(this, content));
        } else {
            Toast.makeText(MapsActivity.this, R.string.content_not_visible_note, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onProfileClicked(SocialUser user) {
        startActivity(MapsActivity.newIntent(this, user));
    }

    public void onMyLocationClick(View v) {
        if (!Utils.checkLocationPermission(this)) {
            requestLocationPermission();
        } else {
            centerMapOnMyLocation(null);
        }
    }

    public void onBtnCameraClick(View view) {
        onBackPressed();
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {

    }

    @Override
    public void onScrollSettled() {
        if (mUserProfile != null) {
            centerMapOnVisibleMarkers();
        }
    }

    @Override
    public void onNextPageLoad(int pageLength) {
        loadNewPageMarkers(pageLength);
        mContentScrollListener.loadingNextFinished();
    }

    @Override
    public void onNextPageFail() {
        mContentScrollListener.loadingNextFinished();
    }

    public void onAreaUpdateClick(View view) {
        loadContentNearLocation(mMap.getCameraPosition());
        mContentListView.startLoading();
        Toast.makeText(getBaseContext(), R.string.updating_area, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (requestCode == MY_LOCATION_REQUEST_CODE) {
            if (Utils.checkLocationPermission(this)) {
                if (mUserProfile == null) {
                    centerMapOnMyLocation(defaultCenterMyLocationCallback);
                }
            }
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        mGoogleApiClient.unregisterConnectionCallbacks(this);
        if (mUserProfile == null) {
            centerMapOnMyLocation(defaultCenterMyLocationCallback);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    public void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @SuppressWarnings("ConstantConditions")
    private void initFeedTitle() {
        ImageView ownerImage = (ImageView) findViewById(R.id.iv_owner_image);
        TextView ownerName = (TextView) findViewById(R.id.tv_owner_name);

        if (mUserProfile != null) {
            Glide.with(getBaseContext())
                    .load(mUserProfile.getAvatarUrl())
                    .crossFade()
                    .fitCenter()
                    .thumbnail(0.1f)
                    .placeholder(R.drawable.ic_account_circle_black_24dp)
                    .transform(new CircleTransform(getBaseContext()))
                    .into(ownerImage);

            ownerImage.setVisibility(View.VISIBLE);
            String firstName = mUserProfile.getFirstName() == null ? mUserProfile.getDisplayName() : mUserProfile.getFirstName();
            String title;
            if(mUserProfile.equals(App.getInstance().getUserManager().getUser())){
                title = getString(R.string.map_feed_self_title);
            }else{
                title = firstName + getString(R.string.map_feed_profile_title);
            }

            ownerName.setText(title);
        } else {
            ownerName.setText(R.string.map_feed_title);
            ownerImage.setVisibility(View.GONE);
        }
    }

    private void loadNewPageMarkers(final int pageLength) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int size = mContentRetriever.size();
                List<Content> contentList = mContentRetriever.getList();
                contentList = contentList.subList(Math.max(size - pageLength, 0), size);

                for (int i = 0; i < contentList.size(); i++) {
                    Content c = contentList.get(i);
                    int visibility = mContentRetriever.get(i).getVisibility().getSocialVisibility().getMode();
                    mMarkerManager.addMarker("" + (size - pageLength + i + 1), visibility, c.getLocation());
                }
            }
        });
    }

    private void loadContentNearLocation(CameraPosition cameraPosition) {
        ContentFetcher contentFetcher = getContentFetcher(cameraPosition);

        mContentRetriever = new ContentPagingRetriever(contentFetcher, mainThreadHandler, PAGE_LENGTH);
        mContentRetriever.registerLoadListener(this);
        mContentRetriever.registerLoadListener(mContentListView);
        EndlessScrollAdapter adapter = new EndlessScrollAdapter(getBaseContext(), mGoogleApiClient, mContentRetriever);
        adapter.setUserProfile(mUserProfile);
        adapter.setOnClickListener(this);
        mContentListView.setAdapter(adapter);

        mMarkerManager.reset();
    }

    private ContentFetcher getContentFetcher(CameraPosition cameraPosition) {
        double radius = Utils.getRadius(mMap.getProjection().getVisibleRegion().latLngBounds);
        ContentFetcher contentFetcher;

        if (mUserProfile != null && App.getInstance().getUserManager().getUser().equals(mUserProfile)) {
            contentFetcher = App.getInstance().getDataController()
                    .createFetcherForMyContent();
        } else if (mUserProfile != null) {
            contentFetcher = App.getInstance().getDataController()
                    .createFetcherForUserContent(mUserProfile.getBaseUser());
        } else {
            contentFetcher = App.getInstance().getDataController().createFetcherForVisibleContent(
                    cameraPosition.target,
                    radius
            );
        }
        return contentFetcher;
    }

    private void centerMapOnMyLocation(GoogleMap.CancelableCallback callback) {
        if (Utils.checkLocationPermission(this)) {
            Location myLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

            if (myLocation == null) {
                Log.e(TAG, "centerMapOnMyLocation: couldn't get user location");
                return;
            }
            LatLng myPosition = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myPosition, 16), 2000, callback);
        }
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                MY_LOCATION_REQUEST_CODE);
    }

    private void centerMapOnVisibleMarkers() {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Marker marker : mMarkerManager.getVisibleMarkers()) {
            builder.include(marker.getPosition());
        }
        LatLngBounds bounds = builder.build();

        int padding = 100; // offset from edges of the map in pixels
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);

        mMap.animateCamera(cu);

    }
}