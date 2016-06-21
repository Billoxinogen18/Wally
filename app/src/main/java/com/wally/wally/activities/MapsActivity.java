package com.wally.wally.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Camera;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
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
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.plus.Plus;
import com.wally.wally.App;
import com.wally.wally.EndlessRecyclerOnScrollListener;
import com.wally.wally.R;
import com.wally.wally.StubContentFetcher;
import com.wally.wally.Utils;
import com.wally.wally.components.CircleTransform;
import com.wally.wally.components.ContentListView;
import com.wally.wally.components.ContentListViewItem;
import com.wally.wally.datacontroller.content.Content;
import com.wally.wally.datacontroller.fetchers.ContentFetcher;
import com.wally.wally.endlessScroll.ContentPagingRetriever;
import com.wally.wally.endlessScroll.MainAdapter;
import com.wally.wally.endlessScroll.MarkerGenerator;
import com.wally.wally.userManager.SocialUser;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnCameraChangeListener,
        ContentPagingRetriever.ContentPageRetrieveListener,
        ContentListViewItem.OnClickListener {

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

    private List<Marker> mMarkerList;
    // Generates and adds markers in Background
    private AsyncTask mMarkerGeneratorTask;

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

        mUserProfile = (SocialUser) getIntent().getSerializableExtra(KEY_USER);
        initUserProfileView();

        mContentListView = (ContentListView) findViewById(R.id.content_list_view);

        mContentScrollListener = new EndlessRecyclerOnScrollListener(PAGE_LENGTH) {
            @Override
            public void onLoadNext() {
                mContentRetriever.loadNext();
            }

            @Override
            public void onLoadPrevious() {
                mContentRetriever.loadPrevious();
            }
        };

        mContentListView.setLayoutManager(new LinearLayoutManager(this));
        mContentListView.addOnScrollListener(mContentScrollListener);


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
        mapFragment.setRetainInstance(true);
    }

    @SuppressWarnings("ConstantConditions")
    private void initUserProfileView() {
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
            ownerName.setText(mUserProfile.getDisplayName());
        } else {
            findViewById(R.id.owner_profile_info).setVisibility(View.GONE);
        }
    }

    public void onContentClicked(Content content) {
        if (content.getVisibility().isPreviewVisible()) {
            startActivity(ContentDetailsActivity.newIntent(this, content));
        } else {
            Toast.makeText(MapsActivity.this, R.string.content_not_visible_note, Toast.LENGTH_SHORT).show();
        }
    }

    public void onProfileClicked(SocialUser user) {
        startActivity(MapsActivity.newIntent(this, user));
    }

    public void onMyLocationClick(View v) {
        centerMapOnMyLocation();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnCameraChangeListener(this);

        mMap.getUiSettings().setMapToolbarEnabled(false);
        if (Utils.checkLocationPermission(this)) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_LOCATION_REQUEST_CODE);
        }
        //TODO what to do when in profile
        centerMapOnMyLocation();
    }

    public void onBtnCameraClick(View view) {
        onBackPressed();
    }

    public void onPageLoaded() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mMarkerList != null) {
                    for (Marker marker : mMarkerList) {
                        marker.remove();
                    }
                }
                mMarkerList = new ArrayList<>();

                final List<Content> contentList = mContentRetriever.getList();

                if (mMarkerGeneratorTask != null) {
                    mMarkerGeneratorTask.cancel(true);
                }

                mMarkerGeneratorTask = new MarkerGenerator(getBaseContext(), contentList) {
                    @Override
                    protected void onPostExecute(List<Bitmap> markerIcons) {
                        if (markerIcons == null) {
                            return;
                        }
                        for (int i = 0; i < contentList.size(); i++) {
                            Content c = contentList.get(i);
                            Bitmap ic = markerIcons.get(i);

                            Marker marker = mMap.addMarker(
                                    new MarkerOptions()
                                            .position(c.getLocation())
                                            .icon(BitmapDescriptorFactory.fromBitmap(ic)));
                            mMarkerList.add(marker);
                        }
                    }
                }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });


    }

    @Override
    public void onNextPageLoad(int pageLength) {
        onPageLoaded();
        mContentScrollListener.loadingFinished();
    }

    @Override
    public void onPreviousPageLoad(int pageLength) {
        onPageLoaded();
        mContentScrollListener.loadingFinished();
    }

    @Override
    public void onBeforeNextPageLoad() {
        mContentScrollListener.loadingFinished();
    }

    @Override
    public void onBeforePreviousPageLoad() {
        mContentScrollListener.loadingFinished();
    }

    @Override
    public void onNextPageFail() {
        mContentScrollListener.loadingFinished();
    }

    @Override
    public void onPreviousPageFail() {
        mContentScrollListener.loadingFinished();
    }

    @Override
    public void onInit() {
        onPageLoaded();
        mContentScrollListener.loadingFinished();
    }

    @Override
    public void onFail() {
        mContentScrollListener.loadingFinished();
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        ContentFetcher contentFetcher = getContentFetcher(cameraPosition);

        mContentRetriever = new ContentPagingRetriever(contentFetcher, PAGE_LENGTH);
        mContentRetriever.registerLoadListener(this);
        mContentRetriever.setContentFetcher(contentFetcher);
        MainAdapter adapter = new MainAdapter(getBaseContext(), mGoogleApiClient, mContentRetriever);
        adapter.setOnClickListener(this);
        mContentListView.setAdapter(adapter);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (requestCode == MY_LOCATION_REQUEST_CODE) {
            if (permissions.length == 1 &&
                    permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION) &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                centerMapOnMyLocation();
            }
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        mGoogleApiClient.unregisterConnectionCallbacks(this);
        centerMapOnMyLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    private ContentFetcher getContentFetcher(CameraPosition cameraPosition) {
        double radius = Utils.getRadius(mMap.getProjection().getVisibleRegion().latLngBounds);
        ContentFetcher contentFetcher;

        if(mUserProfile != null && App.getInstance().getUserManager().getUser().equals(mUserProfile)){
            contentFetcher = App.getInstance().getDataController()
                    .createFetcherForMyContent(
                            cameraPosition.target,
                            radius);
        }else if(mUserProfile != null){
            contentFetcher = App.getInstance().getDataController()
                    .createFetcherForUserContent(mUserProfile.getBaseUser(),
                            cameraPosition.target,
                            radius);
        }else{
            contentFetcher = App.getInstance().getDataController().createFetcherForVisibleContent(
                    cameraPosition.target,
                    radius
            );
        }
        return contentFetcher;
    }

    private void centerMapOnMyLocation() {
        if (Utils.checkLocationPermission(this)) {
            Location myLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

            if (myLocation == null) {
                Log.e(TAG, "centerMapOnMyLocation: couldn't get user location");
                return;
            }
            LatLng myPosition = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myPosition, 16), 2000, null);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_LOCATION_REQUEST_CODE);
        }
    }
}