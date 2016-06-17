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
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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
import com.google.android.gms.plus.Plus;
import com.wally.wally.App;
import com.wally.wally.ContentPagingRetriever;
import com.wally.wally.EndlessRecyclerOnScrollListener;
import com.wally.wally.R;
import com.wally.wally.Utils;
import com.wally.wally.components.CircleTransform;
import com.wally.wally.components.ContentListView;
import com.wally.wally.datacontroller.callbacks.Callback;
import com.wally.wally.datacontroller.content.Content;
import com.wally.wally.datacontroller.fetchers.ContentFetcher;
import com.wally.wally.datacontroller.user.User;
import com.wally.wally.userManager.SocialUser;
import com.wally.wally.userManager.UserManager;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnCameraChangeListener, ContentPagingRetriever.ContentPageRetrieveListener {

    private static final String TAG = MapsActivity.class.getSimpleName();
    private static final String KEY_USER = "USER";
    private static final int MY_LOCATION_REQUEST_CODE = 22;

    private SocialUser mUserProfile;
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;


    private ContentListView mContentListView;


    private MapsRecyclerAdapter mAdapter;
    private ContentPagingRetriever mContentRetriever;
    private EndlessRecyclerOnScrollListener mContentScrollListener;

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

        ContentFetcher contentFetcher = App.getInstance().getDataController().createPublicContentFetcher();

        mContentListView = (ContentListView) findViewById(R.id.content_list_view);
        mContentRetriever = new ContentPagingRetriever(contentFetcher, 2);
        mAdapter = new MapsRecyclerAdapter(mContentRetriever);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mContentScrollListener  = new EndlessRecyclerOnScrollListener(linearLayoutManager, 2) {
            @Override
            public void onLoadNext() {
                mContentRetriever.loadNext();
            }

            @Override
            public void onLoadPrevious() {
                mContentRetriever.loadPrevious();
            }
        };

        mContentListView.setLayoutManager(linearLayoutManager);
        mContentListView.setAdapter(mAdapter);
        mContentListView.addOnScrollListener(mContentScrollListener);

        mContentRetriever.registerLoadListener(this);


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

    private void onContentClicked(Content content) {
        startActivity(ContentDetailsActivity.newIntent(this, content));
    }

    private void onUserClicked(SocialUser user) {
        startActivity(MapsActivity.newIntent(this, user));
    }

    public void onMyLocationClick(View v) {
        centerMapOnMyLocation();
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnCameraChangeListener(this);

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

    @Override
    public void onNextPageLoaded() {
        mContentScrollListener.loadingFinished();
    }

    @Override
    public void onPreviousPageLoaded() {
        mContentScrollListener.loadingFinished();
    }

    @Override
    public void onNextPageFailed() {
        mContentScrollListener.loadingFinished();
    }

    @Override
    public void onPreviousPageFailed() {
        mContentScrollListener.loadingFinished();
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        Log.d(TAG, cameraPosition.toString());
        double radius = Utils.getRadius(mMap.getProjection().getVisibleRegion().latLngBounds);
        ContentFetcher contentFetcher = App.getInstance().getDataController().createPublicContentFetcher();
        mContentRetriever.setContentFetcher(contentFetcher);
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

    private class MapsRecyclerAdapter extends RecyclerView.Adapter<MapsRecyclerAdapter.VH> implements ContentPagingRetriever.ContentPageRetrieveListener {
        private ContentPagingRetriever mRetriever;

        public MapsRecyclerAdapter(ContentPagingRetriever retriever) {
            setRetriever(retriever);
            mRetriever.registerLoadListener(this);
        }

        public void setRetriever(ContentPagingRetriever retriever) {
            mRetriever = retriever;
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
            Content c = mRetriever.get(position);
            // Free up old images
            vh.ownerImage.setImageDrawable(null);
            vh.ownerImage.setBackground(null);
            vh.noteImage.setImageDrawable(null);
            vh.noteImage.setBackground(null);
            vh.ownerName.setText(null);
            vh.ownerInfo.setTag(null);

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
                vh.card.setCardBackgroundColor(c.getColor());
            } else {
                vh.card.setCardBackgroundColor(Color.WHITE);
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
                                    vh.ownerInfo.setTag(user);
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
            int size = mRetriever == null ? 0 : mRetriever.size();
            return size;
        }

        @Override
        public void onNextPageLoaded() {
            notifyItemRemoved(0);
            notifyItemRemoved(1);
            notifyItemInserted(5);
            notifyItemInserted(5);
        }

        @Override
        public void onPreviousPageLoaded() {
            notifyItemRemoved(5);
            notifyItemRemoved(6);
            notifyItemInserted(0);
            notifyItemInserted(0);
        }

        @Override
        public void onNextPageFailed() {
//            mContentListView.setLoadingViewVisibility(View.GONE);
//            mContentListView.setListVisibility(View.VISIBLE);
        }

        @Override
        public void onPreviousPageFailed() {

        }

        public class VH extends RecyclerView.ViewHolder implements View.OnClickListener {
            public CardView card;
            public ImageView ownerImage;
            public TextView ownerName;

            public ImageView noteImage;
            public TextView title;
            public TextView note;

            public View ownerInfo;

            public VH(View itemView) {
                super(itemView);
                card = (CardView) itemView.findViewById(R.id.card);

                ownerImage = (ImageView) itemView.findViewById(R.id.iv_owner_image);
                ownerName = (TextView) itemView.findViewById(R.id.tv_owner_name);

                noteImage = (ImageView) itemView.findViewById(R.id.iv_note_image);
                title = (TextView) itemView.findViewById(R.id.tv_title);
                note = (TextView) itemView.findViewById(R.id.tv_note);

                ownerInfo = itemView.findViewById(R.id.owner_profile_info);

                itemView.setOnClickListener(this);
                ownerInfo.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.owner_profile_info) {
                    if (v.getTag() != null) {
                        onUserClicked((SocialUser) v.getTag());
                    }
                } else {
                    onContentClicked(mRetriever.get(getAdapterPosition()));
                }
            }
        }
    }
}