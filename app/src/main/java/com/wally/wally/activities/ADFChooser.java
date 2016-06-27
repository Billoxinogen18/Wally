package com.wally.wally.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.atap.tangoservice.Tango;
import com.google.atap.tangoservice.TangoAreaDescriptionMetaData;
import com.google.atap.tangoservice.TangoErrorException;
import com.wally.wally.App;
import com.wally.wally.R;
import com.wally.wally.Utils;
import com.wally.wally.datacontroller.adf.ADFService;
import com.wally.wally.datacontroller.adf.AdfMetaData;
import com.wally.wally.datacontroller.adf.AdfSyncInfo;
import com.wally.wally.datacontroller.callbacks.Callback;
import com.wally.wally.fragments.PersistentDialogFragment;

import java.util.ArrayList;
import java.util.List;

public class ADFChooser extends AppCompatActivity implements PersistentDialogFragment.PersistentDialogListener, GoogleApiClient.ConnectionCallbacks {

    // Permission Denied explain codes
    public static final int RC_EXPLAIN_LOCATION = 13;
    public static final int RC_EXPLAIN_ADF = 19;
    private static final String TAG = ADFChooser.class.getSimpleName();
    // Permission Request codes
    private static final int RC_REQ_AREA_LEARNING = 182;
    private static final int RC_REQ_LOCATION = 91;
    private static final String EXPLORER_PACKAGE_NAME = "com.projecttango.tangoexplorer";

    private String mSelectedUUID;
    private Tango mTango;
    private boolean mIsTangoReady;

    private ADFListAdapter mAdapter;
    private GoogleApiClient mGoogleApiClient;

    private boolean mExplainLocationPermission;
    private boolean mExplainAdfPermission;
    private boolean mShouldLoadServerAdfs = true;

    private ArrayList<AdfSyncInfo> mServerAdfMetaData;
    private ArrayList<AdfSyncInfo> mLocalAdfMetaData;

    public static Intent newIntent(Context context) {
        return new Intent(context, ADFChooser.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adfchooser);

        if (!Utils.isTangoDevice(getBaseContext())) {
            throw new IllegalStateException("Starting ADF chooser on Non-Tango device");
        }
        initViews();

        if (!Utils.hasADFPermissions(getBaseContext())) {
            requestADFPermission();
        }

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, null)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .build();
    }

    private void initViews() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerview_adf);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mAdapter = new ADFListAdapter();
        recyclerView.setAdapter(mAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        tryLoadServerADfList();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        tryLoadLocalAdfList();

        if (mExplainLocationPermission) {
            mExplainLocationPermission = false;
            PersistentDialogFragment.newInstance(
                    this,
                    RC_EXPLAIN_LOCATION,
                    R.string.explain_location_permission,
                    R.string.give_permission,
                    R.string.close_application)
                    .show(getSupportFragmentManager(), PersistentDialogFragment.TAG);
        } else if (mExplainAdfPermission) {
            mExplainAdfPermission = false;
            PersistentDialogFragment.newInstance(
                    this,
                    RC_EXPLAIN_ADF,
                    R.string.explain_adf_permission,
                    R.string.give_permission,
                    R.string.close_application)
                    .show(getSupportFragmentManager(), PersistentDialogFragment.TAG);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mTango != null) {
            synchronized (this) {
                // Unbinds Tango Service
                mTango.disconnect();
                mIsTangoReady = false;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_REQ_AREA_LEARNING) {
            if (resultCode != RESULT_OK) {
                mExplainAdfPermission = true;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RC_REQ_LOCATION) {
            if (Utils.checkLocationPermission(this)) {
                tryLoadServerADfList();
            } else {
                mExplainLocationPermission = true;
            }
        }
    }

    @Override
    public void onDialogPositiveClicked(int requestCode) {
        if (requestCode == RC_EXPLAIN_LOCATION) {
            requestLocationPermissions();
        } else if (requestCode == RC_EXPLAIN_ADF) {
            requestADFPermission();
        }
    }

    @Override
    public void onDialogNegativeClicked(int requestCode) {
        finish();
        System.exit(0);
    }

    private void tryLoadLocalAdfList() {
        if (Utils.hasADFPermissions(getBaseContext())) {
            if (mIsTangoReady) {
                loadLocalAdfList();
            } else {
                mTango = new Tango(this, new Runnable() {
                    public void run() {
                        mIsTangoReady = true;
                        loadLocalAdfList();
                    }
                });
            }
        }
    }

    // The method is synchronized because it uses mTango
    private synchronized void loadLocalAdfList() {
        if (!mIsTangoReady) {
            return;
        }
        try {
            final ArrayList<AdfSyncInfo> adfDataList = new ArrayList<>();
            for (String uuid : mTango.listAreaDescriptions()) {
                TangoAreaDescriptionMetaData metadata = mTango.loadAreaDescriptionMetaData(uuid);
                adfDataList.add(AdfSyncInfo.fromMetadata(metadata));
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onLocalAdfsLoaded(adfDataList);
                }
            });
        } catch (TangoErrorException e) {
            Toast.makeText(this, R.string.tango_error, Toast.LENGTH_SHORT).show();
        }
    }

    private void tryLoadServerADfList() {
        if (!mGoogleApiClient.isConnected() && !mShouldLoadServerAdfs) {
            return;
        }
        if (!Utils.checkLocationPermission(this)) {
            requestLocationPermissions();
            return;
        }
        mShouldLoadServerAdfs = false;

        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                if (location != null) {
                    LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
                    loadServerADfList(Utils.extractLatLng(location));
                }
            }
        };

        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(0);
        locationRequest.setFastestInterval(0);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, locationListener);
    }

    private void loadServerADfList(LatLng location) {
        ADFService s = App.getInstance().getDataController().getADFService();
        s.searchADfMetaDataNearLocation(location, new Callback<List<AdfMetaData>>() {
            @Override
            public void onResult(List<AdfMetaData> result) {

            }

            @Override
            public void onError(Exception e) {
                mShouldLoadServerAdfs = true;
            }
        });
    }

    private synchronized void onServerAdfsLoaded(ArrayList<AdfSyncInfo> localAdfs) {

    }

    private synchronized void onLocalAdfsLoaded(ArrayList<AdfSyncInfo> localAdfs) {
        mLocalAdfMetaData = localAdfs;
        mAdapter.setData(localAdfs);
    }

    public void startWithNewADF(View v) {
        Intent intent = getPackageManager().getLaunchIntentForPackage(EXPLORER_PACKAGE_NAME);
        if (intent == null) {
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + EXPLORER_PACKAGE_NAME));
        }
        startActivity(intent);
    }

    private void requestADFPermission() {
        startActivityForResult(
                Tango.getRequestPermissionIntent(Tango.PERMISSIONTYPE_ADF_LOAD_SAVE),
                RC_REQ_AREA_LEARNING);
    }

    private void requestLocationPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                RC_REQ_LOCATION);
    }

    private void onAdfSelected(String uuid) {
        mSelectedUUID = uuid;
        // TODO start sync
        startActivity(CameraARTangoActivity.newIntent(this, uuid));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("mSelectedUUID", mSelectedUUID);
        outState.putBoolean("mExplainAdfPermission", mExplainAdfPermission);
        outState.putBoolean("mExplainLocationPermission", mExplainLocationPermission);
        outState.putBoolean("mShouldLoadServerAdfs", mShouldLoadServerAdfs);

        outState.putSerializable("mServerAdfMetaData", mServerAdfMetaData);
        outState.putSerializable("mLocalAdfMetaData", mLocalAdfMetaData);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            mSelectedUUID = savedInstanceState.getString("mSelectedUUID");
            mExplainAdfPermission = savedInstanceState.getBoolean("mExplainAdfPermission");
            mExplainLocationPermission = savedInstanceState.getBoolean("mExplainLocationPermission");
            mShouldLoadServerAdfs = savedInstanceState.getBoolean("mShouldLoadServerAdfs");

            mServerAdfMetaData = (ArrayList<AdfSyncInfo>) savedInstanceState.getSerializable("mServerAdfMetaData");
            mLocalAdfMetaData = (ArrayList<AdfSyncInfo>) savedInstanceState.getSerializable("mLocalAdfMetaData");
        }
    }

    private class ADFListAdapter extends RecyclerView.Adapter<ADFListAdapter.ADFViewHolder> {

        private List<AdfSyncInfo> mData;

        public void setData(List<AdfSyncInfo> data) {
            mData = data;
            notifyDataSetChanged();
        }

        @Override
        public ADFViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            @SuppressLint("InflateParams") View v = LayoutInflater.from(getBaseContext())
                    .inflate(R.layout.adf_list_item, null);
            return new ADFViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ADFViewHolder holder, int position) {
            AdfMetaData data = mData.get(position).getAdfMetaData();

            holder.name.setText(data.getName());
            holder.uuid.setText(data.getUuid());
        }

        @Override
        public int getItemCount() {
            return mData == null ? 0 : mData.size();
        }

        class ADFViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            TextView name;
            TextView uuid;

            public ADFViewHolder(View itemView) {
                super(itemView);
                name = (TextView) itemView.findViewById(R.id.tv_name);
                uuid = (TextView) itemView.findViewById(R.id.tv_uuid);
                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                // TODO if is from server do other
                String uuid = mData.get(getAdapterPosition()).getAdfMetaData().getUuid();
                onAdfSelected(uuid);
            }
        }
    }
}