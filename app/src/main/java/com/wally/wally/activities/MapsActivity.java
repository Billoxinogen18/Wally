package com.wally.wally.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.firebase.client.annotations.NotNull;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.wally.wally.App;
import com.wally.wally.R;
import com.wally.wally.Utils;
import com.wally.wally.datacontroller.content.Content;
import com.wally.wally.fragments.PreviewContentDialogFragment;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnCameraChangeListener, GoogleMap.OnMarkerClickListener {

    private static final String TAG = MapsActivity.class.getSimpleName();
    private static final int MY_LOCATION_REQUEST_CODE = 22;

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private Set<Content> mContents;
    private Map<Content, Marker> mMarkers;

    private long mLastRequestId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        mContents = new HashSet<>();
        mMarkers = new HashMap<>();

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mapFragment.setRetainInstance(true);
    }

    public void onMyLocationClick(View v) {
        centerMapOnMyLocation();
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnCameraChangeListener(this);
        mMap.setOnMarkerClickListener(this);
        centerMapOnMyLocation();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Content content = null;
        for (Content curr : mMarkers.keySet()) {
            if (mMarkers.get(curr).equals(marker)) {
                content = curr;
                break;
            }
        }
        if (content != null) {
            PreviewContentDialogFragment dialog = PreviewContentDialogFragment.newInstance(content);
            dialog.show(getSupportFragmentManager(), "PreviewContentDialogFragment");
        }

        return true;
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        Log.d(TAG, cameraPosition.toString());
        if (cameraPosition.zoom > 15) {
            App app = (App) getApplicationContext();
            LatLngBounds bounds = mMap.getProjection().getVisibleRegion().latLngBounds;
            Log.d(TAG, bounds.toString());
            markersSetVisible(true);
            mLastRequestId = System.currentTimeMillis();
            app.getDataController().fetch(bounds, new EnumCallback(mLastRequestId) {

                @Override
                public void call(@NotNull Collection<Content> result, @Nullable Exception e) {
                    if (mLastRequestId == getId()) {
                        if (e == null) {
                            mContents.clear();
                            mContents.addAll(result);
                            showContents();
                        } else {
                            Log.e(TAG, e.getMessage(), e);
                            Toast.makeText(getApplicationContext(), getString(R.string.error_fetch_failed),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                }
            });
        } else {
            markersSetVisible(false);
        }
    }

    private void showContents() {
//        for (Content cur : mMarkers.keySet())
//            if (!mContents.contains(cur))
//                mMarkers.remove(cur);

        for (Content content : mContents) {
            if (!mMarkers.keySet().contains(content)) {
                Marker m = mMap.addMarker(new MarkerOptions()
                        .position(content.getLocation())
                        .title(content.getTitle())
                );
                mMarkers.put(content, m);
            }
        }
    }

    private void markersSetVisible(boolean visible) {
        for (Marker m : mMarkers.values()) {
            m.setVisible(visible);
        }
    }


    private void centerMapOnMyLocation() {
        if (Utils.checkLocationPermission(this)) {
            if (!mMap.isMyLocationEnabled()) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
            }

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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
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
        centerMapOnMyLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public void onBtnCameraClick(View view) {
        onBackPressed();
    }
}
