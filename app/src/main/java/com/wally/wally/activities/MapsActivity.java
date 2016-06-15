package com.wally.wally.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.plus.Plus;
import com.wally.wally.App;
import com.wally.wally.R;
import com.wally.wally.Utils;
import com.wally.wally.components.CircleTransform;
import com.wally.wally.datacontroller.callbacks.Callback;
import com.wally.wally.datacontroller.content.Content;
import com.wally.wally.datacontroller.user.User;
import com.wally.wally.fragments.PreviewContentDialogFragment;
import com.wally.wally.userManager.SocialUser;
import com.wally.wally.userManager.UserManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
    private Content mContent;

    private long mLastRequestId;

    private RecyclerView mRecycler;
    private View mEmptyContentView;
    private View mLoadingContentView;

    private MapsRecyclerAdapter mAdapter;

    public static Intent newIntent(Context context, @Nullable Content content) {
        Intent i = new Intent(context, MapsActivity.class);
        i.putExtra("mContent", content);
        return i;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        mContents = new HashSet<>();
        mMarkers = new HashMap<>();

        mContent = (Content) getIntent().getSerializableExtra("mContent");

        initRecyclerView();
        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .enableAutoManage(this, this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .addScope(Plus.SCOPE_PLUS_PROFILE)
                    .addApi(Plus.API)
                    .build();
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mapFragment.setRetainInstance(true);
    }

    private void initRecyclerView() {
        mRecycler = (RecyclerView) findViewById(R.id.recyclerview);
        mRecycler.setLayoutManager(new LinearLayoutManager(getBaseContext()));
        mAdapter = new MapsRecyclerAdapter(null);
        mRecycler.setAdapter(mAdapter);

        mEmptyContentView = findViewById(R.id.empty_view);
        mLoadingContentView = findViewById(R.id.loading_view);
    }

    private void onContentClicked(Content content) {
        startActivity(ContentDetailsActivity.newIntent(this, content));
    }

    public void onMyLocationClick(View v) {
        centerMapOnMyLocation();
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnCameraChangeListener(this);
        mMap.setOnMarkerClickListener(this);

        if (Utils.checkLocationPermission(this)) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_LOCATION_REQUEST_CODE);
        }

        if (mContent != null) {
            centerMapOnContent(mContent);
        } else {
            centerMapOnMyLocation();
        }
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
            dialog.show(getSupportFragmentManager(), PreviewContentDialogFragment.TAG);
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

            mLoadingContentView.setVisibility(View.VISIBLE);
            mEmptyContentView.setVisibility(View.GONE);
            mRecycler.setVisibility(View.GONE);
            app.getDataController().fetchByBounds(bounds, new EnumCallback(mLastRequestId) {

                // TODO this must return list, because we have ordering here. (Also some paging stuff)
                @Override
                public void onResult(Collection<Content> result) {
                    mLoadingContentView.setVisibility(View.GONE);

                    if (result.size() > 0) {
                        mRecycler.setVisibility(View.VISIBLE);
                    } else {
                        mEmptyContentView.setVisibility(View.VISIBLE);
                    }
                    if (mLastRequestId == getId()) {
                        mContents.clear();
                        mContents.addAll(result);
                        showContents();
                    }
                }

                @Override
                public void onError(Exception e) {
                    mLoadingContentView.setVisibility(View.GONE);
                    // TODO show error
                    Log.e(TAG, e.getMessage(), e);
                    Toast.makeText(getApplicationContext(), getString(R.string.error_fetch_failed),
                            Toast.LENGTH_LONG).show();
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
        Log.d(TAG, "showContents() called with: " + "");
        mContents.add(new Content().withLocation(new LatLng(41.72151, 44.8271)).withId("0").withNote("Hi there my name is...").withTitle("Sample note"));
        mContents.add(new Content().withLocation(new LatLng(41.721565, 44.82812)).withId("5").withNote("Some text").withTitle("თქვენ შიგ ხო არ გაქვთ რა ლიმიტი").withImageUri("http://i.imgur.com/RRUe0Mo.png"));
        mContents.add(new Content().withLocation(new LatLng(41.72120, 44.8255)).withId("6").withNote(getString(R.string.large_text)).withTitle("Sample note Title here"));
        mContents.add(new Content().withLocation(new LatLng(41.72180, 44.8270)).withId("7").withNote("Hi there my name is John").withTitle("Sample note"));
        mContents.add(new Content().withLocation(new LatLng(41.72159, 44.8273)).withId("8").withNote("Hi there my name is... I'm programmer here :S"));
        mContents.add(new Content().withLocation(new LatLng(41.72159, 44.8269)).withId("9").withTitle("Sample note Only title"));
        mContents.add(new Content().withLocation(new LatLng(41.72161, 44.8276)).withId("10").withTitle("Sample note").withImageUri("http://www.keenthemes.com/preview/metronic/theme/assets/global/plugins/jcrop/demos/demo_files/image1.jpg"));
        // TODO data controller must return list!
        mAdapter.setData(new ArrayList<>(mContents));

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

    private void centerMapOnContent(Content content) {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(content.getLocation(), 16), 2000, null);
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

    private class MapsRecyclerAdapter extends RecyclerView.Adapter<MapsRecyclerAdapter.VH> {
        private List<Content> mData;

        public MapsRecyclerAdapter(List<Content> data) {
            setData(data);
        }

        public void setData(List<Content> data) {
            mData = data;
            notifyDataSetChanged();
        }

        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater
                    .from(getBaseContext()).inflate(R.layout.maps_content_list_item, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(final VH vh, @SuppressLint("RecyclerView") final int position) {
            Content c = mData.get(position);
            // Free up old images
            vh.ownerImage.setImageDrawable(null);
            vh.ownerImage.setBackground(null);
            vh.noteImage.setImageDrawable(null);
            vh.noteImage.setBackground(null);

            vh.ownerName.setText(null);

            if (!TextUtils.isEmpty(c.getImageUri())) {
                Glide.with(getBaseContext())
                        .load(c.getImageUri())
                        .thumbnail(0.1f)
                        .fitCenter()
                        .crossFade()
                        .placeholder(R.drawable.ic_image_placeholer)
                        .into(vh.noteImage);

                vh.noteImage.setVisibility(View.VISIBLE);
            } else {
                vh.noteImage.setVisibility(View.GONE);
            }

            if (c.getColor() != null) {
                vh.card.setBackgroundColor(c.getColor());
            } else {
                vh.card.setBackgroundColor(Color.WHITE);
            }
            vh.title.setText(c.getTitle());
            vh.title.setVisibility(TextUtils.isEmpty(c.getTitle()) ? View.GONE : View.VISIBLE);

            vh.note.setText(c.getNote());
            vh.note.setVisibility(TextUtils.isEmpty(c.getNote()) ? View.GONE : View.VISIBLE);

            if (TextUtils.isEmpty(c.getAuthorId())) {
                vh.ownerImage.setVisibility(View.GONE);
                vh.ownerName.setVisibility(View.GONE);
                return;
            }
            vh.ownerImage.setVisibility(View.VISIBLE);
            vh.ownerName.setVisibility(View.VISIBLE);
            App.getInstance().getDataController().fetchUser(c.getAuthorId(), new Callback<User>() {
                @Override
                public void onResult(User result) {
                    if (vh.getAdapterPosition() != position) {
                        return;
                    }
                    if (result == null) {
                        vh.ownerImage.setVisibility(View.GONE);
                        vh.ownerName.setVisibility(View.GONE);
                        return;
                    }
                    App.getInstance().getUserManager().loadUser(result, mGoogleApiClient,
                            new UserManager.UserLoadListener() {
                                @Override
                                public void onUserLoad(SocialUser user) {
                                    // if not recycled
                                    if (vh.getAdapterPosition() != position) {
                                        return;
                                    }
                                    if (!TextUtils.isEmpty(user.getAvatarUrl())) {
                                        vh.ownerImage.setVisibility(View.VISIBLE);
                                        // TODO optimize size
                                        Glide.with(getBaseContext())
                                                .load(user.getAvatarUrl())
                                                .crossFade()
                                                .fitCenter()
                                                .thumbnail(0.1f)
                                                .placeholder(R.drawable.ic_account_circle_black_24dp)
                                                .transform(new CircleTransform(getBaseContext()))
                                                .into(vh.ownerImage);
                                    }
                                    vh.ownerName.setVisibility(View.VISIBLE);
                                    vh.ownerName.setText(user.getDisplayName());
                                }

                                @Override
                                public void onUserLoadFailed() {
                                    //TODO implementation missing
                                }
                            });
                }

                @Override
                public void onError(Exception e) {
                    Log.e(TAG, "onError: ", e);
                }
            });
        }

        @Override
        public int getItemCount() {
            return mData == null ? 0 : mData.size();
        }

        public class VH extends RecyclerView.ViewHolder implements View.OnClickListener {
            public View card;
            public ImageView ownerImage;
            public TextView ownerName;

            public ImageView noteImage;
            public TextView title;
            public TextView note;

            public VH(View itemView) {
                super(itemView);
                card = itemView.findViewById(R.id.card);

                ownerImage = (ImageView) itemView.findViewById(R.id.iv_owner_image);
                ownerName = (TextView) itemView.findViewById(R.id.tv_owner_name);

                noteImage = (ImageView) itemView.findViewById(R.id.iv_note_image);
                title = (TextView) itemView.findViewById(R.id.tv_title);
                note = (TextView) itemView.findViewById(R.id.tv_note);

                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                onContentClicked(mData.get(getAdapterPosition()));
            }
        }
    }
}