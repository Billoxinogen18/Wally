package com.wally.wally.controllers.map;


import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.plus.Plus;
import com.mapbox.mapboxsdk.MapboxAccountManager;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerView;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdate;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.geometry.VisibleRegion;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.wally.wally.App;
import com.wally.wally.R;
import com.wally.wally.Utils;
import com.wally.wally.controllers.map.contentList.Adapter;
import com.wally.wally.controllers.map.contentList.OnScrollListener;
import com.wally.wally.controllers.map.contentList.PagingRetriever;
import com.wally.wally.controllers.map.contentList.View;
import com.wally.wally.controllers.map.contentList.ViewItem;
import com.wally.wally.datacontroller.DataController;
import com.wally.wally.datacontroller.content.Content;
import com.wally.wally.userManager.SocialUser;

import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MapsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MapsFragment extends BaseFragment implements
        GoogleApiClient.ConnectionCallbacks,
        PagingRetriever.ContentPageRetrieveListener,
        ViewItem.OnClickListener,
        View.OnScrollSettleListener,
        android.view.View.OnClickListener, OnMapReadyCallback {

    private static final String TAG = MapsFragment.class.getSimpleName();
    private static final String KEY_USER = "USER";
    private static final int PAGE_LENGTH = 5;
    private static final int RC_MY_LOCATION_CLICK = 921;

    private MapOpenCloseListener mListener;

    private SocialUser mUserProfile;

    private MapView mMapView;
    private MapboxMap mMap;
    private GoogleApiClient mGoogleApiClient;

    private com.wally.wally.controllers.map.contentList.View mContentListView;

    private PagingRetriever mContentRetriever;
    private OnScrollListener mContentScrollListener;

    private Handler mainThreadHandler;
    private MarkerManager mMarkerManager;
    private MapboxMap.CancelableCallback defaultCenterMyLocationCallback = new MapboxMap.CancelableCallback() {
        @Override
        public void onFinish() {
            loadContentNearLocation(mMap.getCameraPosition());
        }

        @Override
        public void onCancel() {
            loadContentNearLocation(mMap.getCameraPosition());
        }
    };

    public MapsFragment() {
        // Required empty public constructor
    }


    public static MapsFragment newInstance(SocialUser user) {
        MapsFragment fragment = new MapsFragment();
        Bundle args = new Bundle();
        args.putSerializable(KEY_USER, user);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mUserProfile = (SocialUser) getArguments().getSerializable(KEY_USER);
        }

        mainThreadHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public android.view.View onCreateView(LayoutInflater inflater, ViewGroup container,
                                          Bundle savedInstanceState) {
        MapboxAccountManager.start(getContext(), getString(R.string.mapbox_api_key));
        android.view.View v = inflater.inflate(R.layout.fragment_maps, container, false);

        initFeedTitle(v);

        if (mUserProfile != null) {
            v.findViewById(R.id.update_area).setVisibility(View.GONE);
        }


        v.findViewById(R.id.back).setOnClickListener(this);
        v.findViewById(R.id.my_location).setOnClickListener(this);
        v.findViewById(R.id.update_area).setOnClickListener(this);

        mContentListView = (com.wally.wally.controllers.map.contentList.View) v.findViewById(R.id.content_list_view);

        mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .addScope(Plus.SCOPE_PLUS_PROFILE)
                .addApi(Plus.API)
                .build();


        mMapView = (MapView) v.findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);
        mMapView.getMapAsync(this);
        return v;
    }

    // Add the mapView lifecycle to the activity's lifecycle methods
    @Override
    public void onResume() {
        super.onResume();
        if (mMapView != null)
            mMapView.onResume();
        mListener.onMapOpen();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mMapView != null)
            mMapView.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mMapView != null)
            mMapView.onLowMemory();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mMapView != null)
            mMapView.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mMapView != null)
            mMapView.onSaveInstanceState(outState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (MapOpenCloseListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement MapOpenCloseListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener.onMapClose();
    }

    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        mMap = mapboxMap;

        if (Utils.checkLocationPermission(getContext())) {
            mMap.setMyLocationEnabled(true);
        } else {
            requestLocationPermission(0);
        }

        mMarkerManager = new MarkerManager(getContext(), mMap);
        mContentScrollListener = new OnScrollListener(PAGE_LENGTH) {
            int markerCounter = 0;

            @Override
            public void onLoadNext() {
                mContentRetriever.loadNext();
            }

            @Override
            public void onVisibleItemChanged(final int previous, final int current) {
                mMarkerManager.selectMarkerAt(current);
                if (markerCounter % 10 == 0 || (mContentRetriever.size() <= PAGE_LENGTH * 3 && current == 0)) {
                    markerCounter = 0;
                    centerMapOnVisibleMarkers();
                }
                markerCounter++;
            }
        };

        mContentListView.setLayoutManager(new LinearLayoutManager(getContext()));
        mContentListView.addOnScrollListener(mContentScrollListener);
        mContentListView.setOnScrollSettleListener(this);


        if (mUserProfile == null)
            centerMapOnMyLocation(defaultCenterMyLocationCallback);
        else
            loadContentNearLocation(null);
    }

    @Override
    public void onProfileClicked(SocialUser user) {
        mListener.openMapFragment(user);
    }

    public void onMyLocationClick() {
        if (!Utils.checkLocationPermission(getContext())) {
            requestLocationPermission(RC_MY_LOCATION_CLICK);
        } else {
            centerMapOnMyLocation(null);
        }
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

    public void onAreaUpdateClick() {
        loadContentNearLocation(mMap.getCameraPosition());
        mContentListView.startLoading();
        Toast.makeText(getContext(), R.string.updating_area, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLocationPermissionGranted(int locationRequestCode) {
        if (locationRequestCode == RC_MY_LOCATION_CLICK) {
            if (mUserProfile == null) {
                centerMapOnMyLocation(defaultCenterMyLocationCallback);
            }
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        mGoogleApiClient.unregisterConnectionCallbacks(this);
        if (mUserProfile == null && mMap != null) {
            centerMapOnMyLocation(defaultCenterMyLocationCallback);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onClick(android.view.View view) {
        if (view.getId() == R.id.back)
            getActivity().getSupportFragmentManager().popBackStack();
        else if (view.getId() == R.id.my_location)
            onMyLocationClick();
        else if (view.getId() == R.id.update_area)
            onAreaUpdateClick();

    }

    @SuppressWarnings("ConstantConditions")
    private void initFeedTitle(android.view.View v) {
        ImageView ownerImage = (ImageView) v.findViewById(R.id.iv_owner_image);
        TextView ownerName = (TextView) v.findViewById(R.id.tv_owner_name);
        TextView fragmentTitle = (TextView) v.findViewById(R.id.fragment_title);

        if (mUserProfile != null) {
            Glide.with(getContext())
                    .load(mUserProfile.getAvatarUrl())
                    .crossFade()
                    .fitCenter()
                    .thumbnail(0.1f)
                    .placeholder(R.drawable.ic_account_circle_black_24dp)
                    .dontAnimate()
                    .into(ownerImage);

            ownerImage.setVisibility(View.VISIBLE);
            String firstName = mUserProfile.getFirstName() == null ? mUserProfile.getDisplayName() : mUserProfile.getFirstName();
            ownerName.setText(firstName);
            fragmentTitle.setText(R.string.profile);
        } else {
            v.findViewById(R.id.owner_profile_info).setVisibility(View.GONE);
            fragmentTitle.setText(R.string.map_feed_title);
        }
    }

    private void loadNewPageMarkers(final int pageLength) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final int size = mContentRetriever.size();
                List<Content> contentList = mContentRetriever.getList();
                contentList = contentList.subList(Math.max(size - pageLength, 0), size);
                final CountDownLatch latch = new CountDownLatch(contentList.size());

                for (int i = 0; i < contentList.size(); i++) {
                    final Content c = contentList.get(i);
                    final String name = "" + (size - pageLength + i + 1);
                    mainThreadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mMarkerManager.addMarker(name, c, new MarkerManager.OnMarkerAddListener() {
                                @Override
                                public void onMarkerAdd() {
                                    latch.countDown();
                                }
                            });
                        }
                    });
                }

                try {
                    latch.await();
                } catch (InterruptedException ignored) {

                }

                mainThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        centerMapOnVisibleMarkers();
                    }
                });
            }
        }).start();
    }

    private void loadContentNearLocation(CameraPosition cameraPosition) {
        DataController.Fetcher contentFetcher = getContentFetcher(cameraPosition);

        mContentRetriever = new PagingRetriever(contentFetcher, mainThreadHandler, PAGE_LENGTH);
        mContentRetriever.registerLoadListener(this);
        mContentRetriever.registerLoadListener(mContentListView);
        Adapter adapter = new Adapter(getContext(), mGoogleApiClient, mContentRetriever);
        adapter.setUserProfile(mUserProfile);
        adapter.setOnClickListener(this);
        mContentListView.setAdapter(adapter);

        mMarkerManager.reset();
    }

    private DataController.Fetcher getContentFetcher(CameraPosition cameraPosition) {
        VisibleRegion visibleRegion = mMap.getProjection().getVisibleRegion();
        double radius = Utils.getRadius(visibleRegion.latLngBounds.getCenter(), visibleRegion.farRight);
        DataController.Fetcher contentFetcher;

        if (mUserProfile != null && App.getInstance().getSocialUserManager().getUser().equals(mUserProfile)) {
            contentFetcher = App.getInstance().getDataController()
                    .createFetcherForMyContent();
        } else if (mUserProfile != null) {
            contentFetcher = App.getInstance().getDataController()
                    .createFetcherForUserContent(mUserProfile.getBaseUser());
        } else {
            contentFetcher = App.getInstance().getDataController().createFetcherForVisibleContent(
                    Utils.latLngToSerializableLatLng(cameraPosition.target),
                    radius
            );
        }
        return contentFetcher;
    }

    private void centerMapOnMyLocation(MapboxMap.CancelableCallback callback) {
        if (Utils.checkLocationPermission(getContext())) {
            Location myLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

            if (myLocation == null) {
                Log.e(TAG, "centerMapOnMyLocation: couldn't get user location");
                return;
            }
            LatLng myPosition = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myPosition, 16), 2000, callback);
        }
    }

    private void centerMapOnVisibleMarkers() {
        List<MarkerView> markers = mMarkerManager.getVisibleMarkers();
        if (markers.size() > 1) {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (Marker marker : markers) {
                builder.include(marker.getPosition());
            }
            LatLngBounds bounds = builder.build();

            int padding = 100; // offset from edges of the map in pixels
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
            mMap.animateCamera(cu);
        }
    }

    public interface MapOpenCloseListener {
        void onMapClose();

        void onMapOpen();

        void openMapFragment(SocialUser socialUser);
    }
}
