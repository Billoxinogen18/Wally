package com.wally.wally.activities;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.api.GoogleApiClient;
import com.mapbox.mapboxsdk.MapboxAccountManager;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.wally.wally.R;
import com.wally.wally.components.ContentListView;
import com.wally.wally.endlessScroll.ContentPagingRetriever;
import com.wally.wally.endlessScroll.EndlessRecyclerOnScrollListener;
import com.wally.wally.endlessScroll.MarkerManager;
import com.wally.wally.userManager.SocialUser;

public class MapsFragmentt extends Fragment implements OnMapReadyCallback
//        GoogleApiClient.ConnectionCallbacks,
//        GoogleApiClient.OnConnectionFailedListener,
//        MapboxMap.OnCameraChangeListener,
//        ContentPagingRetriever.ContentPageRetrieveListener,
//        ContentListViewItem.OnClickListener, ContentListView.OnScrollSettleListener
{

    private static final String TAG = MapsFragmentt.class.getSimpleName();
    private static final String KEY_USER = "USER";
    private static final int MY_LOCATION_REQUEST_CODE = 22;
    private static final int PAGE_LENGTH = 5;

    private SocialUser mUserProfile;

    private MapView mMapView;
    private MapboxMap mMap;
    private GoogleApiClient mGoogleApiClient;

    private ContentListView mContentListView;

    private ContentPagingRetriever mContentRetriever;
    private EndlessRecyclerOnScrollListener mContentScrollListener;

    private Handler mainThreadHandler;
    private MarkerManager mMarkerManager;
//    private MapboxMap.CancelableCallback defaultCenterMyLocationCallback = new MapboxMap.CancelableCallback() {
//        @Override
//        public void onFinish() {
//            loadContentNearLocation(mMap.getCameraPosition());
//        }
//
//        @Override
//        public void onCancel() {
//            loadContentNearLocation(mMap.getCameraPosition());
//        }
//    };

    /**
     * Start to see user profile
     */
    public static MapsFragmentt newInstance(SocialUser user) {
        Bundle args = new Bundle();
        args.putSerializable(KEY_USER, user);
        MapsFragmentt mf = new MapsFragmentt();
        mf.setArguments(args);
        // TODO read args
        return mf;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        MapboxAccountManager.start(getContext(), getString(R.string.mapbox_api_key));
        View v = inflater.inflate(R.layout.fragment_maps, container, false);

//        mainThreadHandler = new Handler(getMainLooper());
//
//        mUserProfile = (SocialUser) getIntent().getSerializableExtra(KEY_USER);
//        initFeedTitle();
//
//        if (mUserProfile != null) {
//            findViewById(R.id.update_area).setVisibility(View.GONE);
//        }
//
//        mContentListView = (ContentListView) findViewById(R.id.content_list_view);
//
//        mGoogleApiClient = new GoogleApiClient.Builder(this)
//                .enableAutoManage(this, this)
//                .addConnectionCallbacks(this)
//                .addOnConnectionFailedListener(this)
//                .addApi(LocationServices.API)
//                .addScope(Plus.SCOPE_PLUS_PROFILE)
//                .addApi(Plus.API)
//                .build();
//


        mMapView = (MapView) v.findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);
        mMapView.getMapAsync(this);
        return v;
    }


    // Add the mapView lifecycle to the activity's lifecycle methods
    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

//    @Override
//    public void onContentClicked(Content content) {
//        if (content.getVisibility().isPreviewVisible()) {
//            startActivity(ContentDetailsActivity.newIntent(this, content));
//        } else {
//            Toast.makeText(MapsFragment.this, R.string.content_not_visible_note, Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    @Override
//    public void onProfileClicked(SocialUser user) {
//        startActivity(MapsFragment.newIntent(this, user));
//    }
//
//    public void onMyLocationClick(View v) {
//        if (!Utils.checkLocationPermission(this)) {
//            requestLocationPermission();
//        } else {
//            centerMapOnMyLocation(null);
//        }
//    }
//
//    public void onBtnCameraClick(View view) {
//        onBackPressed();
//    }
//
//    @Override
//    public void onCameraChange(CameraPosition cameraPosition) {
//
//    }
//
//    @Override
//    public void onScrollSettled() {
//        if (mUserProfile != null) {
//            centerMapOnVisibleMarkers();
//        }
//    }
//
//    @Override
//    public void onNextPageLoad(int pageLength) {
//        loadNewPageMarkers(pageLength);
//        mContentScrollListener.loadingNextFinished();
//    }
//
//    @Override
//    public void onNextPageFail() {
//        mContentScrollListener.loadingNextFinished();
//    }
//
//    public void onAreaUpdateClick(View view) {
//        loadContentNearLocation(mMap.getCameraPosition());
//        mContentListView.startLoading();
//        Toast.makeText(getBaseContext(), R.string.updating_area, Toast.LENGTH_SHORT).show();
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
//        if (requestCode == MY_LOCATION_REQUEST_CODE) {
//            if (Utils.checkLocationPermission(this)) {
//                if (mUserProfile == null) {
//                    centerMapOnMyLocation(defaultCenterMyLocationCallback);
//                }
//            }
//        }
//    }
//
//    @Override
//    public void onConnected(Bundle connectionHint) {
//        mGoogleApiClient.unregisterConnectionCallbacks(this);
//        if (mUserProfile == null && mMap != null) {
//            centerMapOnMyLocation(defaultCenterMyLocationCallback);
//        }
//    }
//
//    @Override
//    public void onConnectionSuspended(int i) {
//
//    }
//
//    @Override
//    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
//
//    }
//
//    @Override
//    public void onStart() {
//        mGoogleApiClient.connect();
//        super.onStart();
//    }
//
//    @Override
//    public void onStop() {
//        mGoogleApiClient.disconnect();
//        super.onStop();
//    }
//
//    @SuppressWarnings("ConstantConditions")
//    private void initFeedTitle() {
//        ImageView ownerImage = (ImageView) findViewById(R.id.iv_owner_image);
//        TextView ownerName = (TextView) findViewById(R.id.tv_owner_name);
//
//        if (mUserProfile != null) {
//            Glide.with(getBaseContext())
//                    .load(mUserProfile.getAvatarUrl())
//                    .crossFade()
//                    .fitCenter()
//                    .thumbnail(0.1f)
//                    .placeholder(R.drawable.ic_account_circle_black_24dp)
//                    .dontAnimate()
//                    .transform(new CircleTransform(getBaseContext()))
//                    .into(ownerImage);
//
//            ownerImage.setVisibility(View.VISIBLE);
//            String firstName = mUserProfile.getFirstName() == null ? mUserProfile.getDisplayName() : mUserProfile.getFirstName();
//            String title;
//            if (mUserProfile.equals(App.getInstance().getUserManager().getUser())) {
//                title = getString(R.string.map_feed_self_title);
//            } else {
//                title = firstName + getString(R.string.map_feed_profile_title);
//            }
//
//            ownerName.setText(title);
//        } else {
//            ownerName.setText(R.string.map_feed_title);
//            ownerImage.setVisibility(View.GONE);
//        }
//    }
//
//    private void loadNewPageMarkers(final int pageLength) {
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                int size = mContentRetriever.size();
//                List<Content> contentList = mContentRetriever.getList();
//                contentList = contentList.subList(Math.max(size - pageLength, 0), size);
//
//                for (int i = 0; i < contentList.size(); i++) {
//                    Content c = contentList.get(i);
//                    int visibility = mContentRetriever.get(i).getVisibility().getSocialVisibility().getMode();
//                    mMarkerManager.addMarker("" + (size - pageLength + i + 1), visibility, Utils.serializableLatLngToLatLng(c.getLocation()));
//                }
//            }
//        });
//    }
//
//    private void loadContentNearLocation(CameraPosition cameraPosition) {
//        ContentFetcher contentFetcher = getContentFetcher(cameraPosition);
//
//        mContentRetriever = new ContentPagingRetriever(contentFetcher, mainThreadHandler, PAGE_LENGTH);
//        mContentRetriever.registerLoadListener(this);
//        mContentRetriever.registerLoadListener(mContentListView);
//        EndlessScrollAdapter adapter = new EndlessScrollAdapter(getBaseContext(), mGoogleApiClient, mContentRetriever);
//        adapter.setUserProfile(mUserProfile);
//        adapter.setOnClickListener(this);
//        mContentListView.setAdapter(adapter);
//
//        mMarkerManager.reset();
//    }
//
//    private ContentFetcher getContentFetcher(CameraPosition cameraPosition) {
//        VisibleRegion visibleRegion = mMap.getProjection().getVisibleRegion();
//        double radius = Utils.getRadius(visibleRegion.latLngBounds.getCenter(), visibleRegion.farRight);
//        ContentFetcher contentFetcher;
//
//        if (mUserProfile != null && App.getInstance().getUserManager().getUser().equals(mUserProfile)) {
//            contentFetcher = App.getInstance().getDataController()
//                    .createFetcherForMyContent();
//        } else if (mUserProfile != null) {
//            contentFetcher = App.getInstance().getDataController()
//                    .createFetcherForUserContent(mUserProfile.getBaseUser());
//        } else {
//            contentFetcher = App.getInstance().getDataController().createFetcherForVisibleContent(
//                    Utils.latLngToSerializableLatLng(cameraPosition.target),
//                    radius
//            );
//        }
//        return contentFetcher;
//    }
//
//    private void centerMapOnMyLocation(MapboxMap.CancelableCallback callback) {
//        if (Utils.checkLocationPermission(this)) {
//            Location myLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
//
//            if (myLocation == null) {
//                Log.e(TAG, "centerMapOnMyLocation: couldn't get user location");
//                return;
//            }
//            LatLng myPosition = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
//            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myPosition, 16), 2000, callback);
//        }
//    }
//
//    private void requestLocationPermission() {
//        ActivityCompat.requestPermissions(this,
//                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
//                MY_LOCATION_REQUEST_CODE);
//    }
//
//    private void centerMapOnVisibleMarkers() {
//        LatLngBounds.Builder builder = new LatLngBounds.Builder();
//        for (Marker marker : mMarkerManager.getVisibleMarkers()) {
//            builder.include(marker.getPosition());
//        }
//        LatLngBounds bounds = builder.build();
//
//        int padding = 100; // offset from edges of the map in pixels
//        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
//
//        mMap.animateCamera(cu);
//
//    }

    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        mMap = mapboxMap;
//        mMap.setOnCameraChangeListener(this);
//        mMap.setInfoWindowAdapter(new MapWindowAdapter(this));
//
//        if (Utils.checkLocationPermission(this)) {
//            mMap.setMyLocationEnabled(true);
//        } else {
//            requestLocationPermission();
//        }
//
//        mMarkerManager = new MarkerManager(getBaseContext(), mMap);
//        mContentScrollListener = new EndlessRecyclerOnScrollListener(PAGE_LENGTH) {
//            int markerCounter = 0;
//
//            @Override
//            public void onLoadNext() {
//                mContentRetriever.loadNext();
//            }
//
//            @Override
//            public void onVisibleItemChanged(final int previous, final int current) {
//                mMarkerManager.selectMarkerAt(current);
//                if ((markerCounter % 10 == 0 && mUserProfile != null) || (mContentRetriever.size() <= PAGE_LENGTH * 3 && current == 0)) {
//                    markerCounter = 0;
//                    centerMapOnVisibleMarkers();
//                }
//                markerCounter++;
//            }
//        };
//
//        mContentListView.setLayoutManager(new LinearLayoutManager(this));
//        mContentListView.addOnScrollListener(mContentScrollListener);
//        mContentListView.setOnScrollSettleListener(this);
//
//
//        if (mUserProfile == null)
//            centerMapOnMyLocation(defaultCenterMyLocationCallback);
//        else
//            loadContentNearLocation(null);
    }
}