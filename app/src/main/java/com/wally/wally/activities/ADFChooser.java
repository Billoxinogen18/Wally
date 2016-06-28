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
import android.text.TextUtils;
import android.util.Log;
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

    private static final String TAG = ADFChooser.class.getSimpleName();

    // Permission Denied explain codes
    private static final int RC_EXPLAIN_LOCATION = 13;
    private static final int RC_EXPLAIN_ADF = 14;
    private static final int RC_EXPLAIN_ADF_IMPORT = 15;
    private static final int RC_EXPLAIN_ADF_EXPORT = 16;

    // Permission Request codes
    private static final int RC_REQ_AREA_LEARNING = 17;
    private static final int RC_REQ_LOCATION = 18;
    private static final int RC_REQ_ADF_IMPORT = 19;
    private static final int RC_REQ_ADF_EXPORT = 20;

    private static final String EXPLORER_PACKAGE_NAME = "com.projecttango.tangoexplorer";

    private static final String INTENT_CLASSPACKAGE = "com.projecttango.tango";
    private static final String INTENT_IMPORTEXPORT_CLASSNAME = "com.google.atap.tango.RequestImportExportActivity";

    private static final String EXTRA_KEY_SOURCEFILE = "SOURCE_FILE";
    private static final String EXTRA_KEY_SOURCEUUID = "SOURCE_UUID";
    private static final String EXTRA_KEY_DESTINATIONFILE = "DESTINATION_FILE";
    private final Object adfLoadLock = new Object();
    private AdfSyncInfo mSelectedAdf;
    private Tango mTango;
    private ADFListAdapter mAdapter;
    private GoogleApiClient mGoogleApiClient;

    private boolean mExplainLocationPermission;
    private boolean mExplainAdfPermission;
    private boolean mShouldLoadServerAdfs = true;
    private boolean mExplainAdfImportPermission;
    private boolean mExplainAdfExportPermission;

    private ArrayList<AdfSyncInfo> mServerAdfMetaData;
    private ArrayList<AdfSyncInfo> mLocalAdfMetaData;
    private LatLng mCurrentLocation;
    private boolean mIsSynchronizingAdfs = false;
    private View mSynchronizingView;

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

        mSynchronizingView = findViewById(R.id.synchronizing_view);
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
        updateSyncView();
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
        } else if (mExplainAdfImportPermission) {
            mExplainAdfImportPermission = false;
            // TODO start explain
        } else if (mExplainAdfExportPermission) {
            mExplainAdfExportPermission = false;
            // TODO explain
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_REQ_AREA_LEARNING) {
            if (resultCode != RESULT_OK) {
                mExplainAdfPermission = true;
            }
        } else if (requestCode == RC_REQ_ADF_IMPORT) {
            if (resultCode == RESULT_OK) {
                if (TextUtils.equals(data.getDataString(), mSelectedAdf.getAdfMetaData().getUuid())) {
                    throw new IllegalStateException("Selected ADF uuid is different from imported one");
                }
                startArWithSelectedAdf();
            } else {
                mExplainAdfImportPermission = true;
            }
        } else if (requestCode == RC_REQ_ADF_EXPORT) {
            if (resultCode == RESULT_OK) {
                tryUploadSelectedAdf();
            } else {
                mExplainAdfExportPermission = true;
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
            mTango = new Tango(this, new Runnable() {
                public void run() {
                    loadLocalAdfList();
                }
            });
        }
    }

    // The method is synchronized because it uses mTango
    private synchronized void loadLocalAdfList() {
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
                    mCurrentLocation = Utils.extractLatLng(location);
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
                Log.d(TAG, "onResult() called with: " + "result = [" + result + "]");
                ArrayList<AdfSyncInfo> arrayList = new ArrayList<>(result.size());
                for (AdfMetaData adfMetaData : result) {
                    AdfSyncInfo syncInfo = new AdfSyncInfo(adfMetaData, false);
                    arrayList.add(syncInfo);
                }
                onServerAdfsLoaded(arrayList);
            }

            @Override
            public void onError(Exception e) {
                mShouldLoadServerAdfs = true;
                // TODO show error
            }
        });
    }

    /**
     * Called when Server ADFs are loaded
     */
    private void onServerAdfsLoaded(final ArrayList<AdfSyncInfo> adfs) {
        synchronized (adfLoadLock) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mServerAdfMetaData = adfs;
                    if (mLocalAdfMetaData != null) {
                        onAdfsLoaded();
                    }
                }
            });
        }
    }

    /**
     * Called when Local ADFs are loaded
     */
    private void onLocalAdfsLoaded(final ArrayList<AdfSyncInfo> adfs) {
        synchronized (adfLoadLock) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mLocalAdfMetaData = adfs;
                    if (mServerAdfMetaData != null) {
                        onAdfsLoaded();
                    }
                }
            });
        }
    }

    /**
     * Called when all the adfs are loaded
     */
    private void onAdfsLoaded() {
        // No need to bind Tango service, in onResume, local adfs will be reloaded thus this method
        // Will be called again
        ArrayList<AdfSyncInfo> synchronizedList = new ArrayList<>();
        for (AdfSyncInfo syncInfo : mServerAdfMetaData) {
            if (mLocalAdfMetaData.contains(syncInfo)) {
                syncInfo.setIsSynchronized(true);
                synchronizedList.add(syncInfo);
            }
        }
        mServerAdfMetaData.removeAll(synchronizedList);
        mLocalAdfMetaData.removeAll(synchronizedList);
        // Sort with location.
        Utils.sortWithLocation(synchronizedList, mCurrentLocation);
        Utils.sortWithLocation(mServerAdfMetaData, mCurrentLocation);
        Utils.sortWithLocation(mLocalAdfMetaData, mCurrentLocation);

        List<AdfSyncInfo> list = new ArrayList<>(mServerAdfMetaData.size() + mLocalAdfMetaData.size());
        list.addAll(synchronizedList);
        list.addAll(mServerAdfMetaData);
        list.addAll(mLocalAdfMetaData);

        mAdapter.setData(list);
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

    private void onAdfSelected(AdfSyncInfo adf) {
        mSelectedAdf = adf;
        if (mSelectedAdf.isSynchronized()) {
            startArWithSelectedAdf();
        } else if (mSelectedAdf.isLocal()) {
            requestExportPermission();
        } else {
            tryDownloadSelectedAdf();
        }

        // startArWithSelectedAdf();
    }

    private void tryUploadSelectedAdf() {
        updateSyncStatus(true);
        String uuid = mSelectedAdf.getAdfMetaData().getUuid();

        ADFService s = App.getInstance().getDataController().getADFService();
        s.upload(Utils.getAdfFilePath(uuid), mSelectedAdf.getAdfMetaData(), new Callback<Void>() {
            @Override
            public void onResult(Void result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        startArWithSelectedAdf();
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateSyncStatus(false);
                        Toast.makeText(ADFChooser.this, "Error uploading ADF", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void tryDownloadSelectedAdf() {
        updateSyncStatus(true);

        String uuid = mSelectedAdf.getAdfMetaData().getUuid();

        ADFService s = App.getInstance().getDataController().getADFService();
        s.download(Utils.getAdfFilePath(uuid), uuid, new Callback<Void>() {
            @Override
            public void onResult(Void result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateSyncStatus(false);
                        requestImportPermission();
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateSyncStatus(false);
                        Toast.makeText(ADFChooser.this, "Error downloading ADF", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void requestImportPermission() {
        String uuid = mSelectedAdf.getAdfMetaData().getUuid();

        Intent importIntent = new Intent();
        importIntent.setClassName(INTENT_CLASSPACKAGE, INTENT_IMPORTEXPORT_CLASSNAME);
        importIntent.putExtra(EXTRA_KEY_SOURCEFILE, uuid);
        startActivityForResult(importIntent, RC_REQ_ADF_IMPORT);
    }

    private void requestExportPermission() {
        String uuid = mSelectedAdf.getAdfMetaData().getUuid();

        Intent exportIntent = new Intent();
        exportIntent.setClassName(INTENT_CLASSPACKAGE, INTENT_IMPORTEXPORT_CLASSNAME);
        exportIntent.putExtra(EXTRA_KEY_SOURCEUUID, uuid);
        exportIntent.putExtra(EXTRA_KEY_DESTINATIONFILE, Utils.getAdfFilesFolder());
        startActivityForResult(exportIntent, RC_REQ_ADF_EXPORT);
    }

    private void startArWithSelectedAdf() {
        startActivity(CameraARTangoActivity.newIntent(this, mSelectedAdf.getAdfMetaData().getUuid()));
        finish();
    }

    private void updateSyncStatus(boolean isSynchronizing) {
        mIsSynchronizingAdfs = isSynchronizing;
        updateSyncView();
    }

    private void updateSyncView() {
        mSynchronizingView.setVisibility(mIsSynchronizingAdfs ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("mSelectedAdf", mSelectedAdf);
        outState.putBoolean("mExplainAdfPermission", mExplainAdfPermission);
        outState.putBoolean("mExplainLocationPermission", mExplainLocationPermission);
        outState.putBoolean("mExplainAdfImportPermission", mExplainAdfImportPermission);
        outState.putBoolean("mExplainAdfExportPermission", mExplainAdfExportPermission);

        outState.putBoolean("mShouldLoadServerAdfs", mShouldLoadServerAdfs);
        outState.putBoolean("mIsSynchronizingAdfs", mIsSynchronizingAdfs);

        outState.putSerializable("mServerAdfMetaData", mServerAdfMetaData);
        outState.putSerializable("mLocalAdfMetaData", mLocalAdfMetaData);
        outState.putParcelable("mCurrentLocation", mCurrentLocation);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            mSelectedAdf = (AdfSyncInfo) savedInstanceState.getSerializable("mSelectedAdf");
            mExplainAdfPermission = savedInstanceState.getBoolean("mExplainAdfPermission");
            mExplainLocationPermission = savedInstanceState.getBoolean("mExplainLocationPermission");
            mExplainAdfImportPermission = savedInstanceState.getBoolean("mExplainAdfImportPermission");
            mExplainAdfExportPermission = savedInstanceState.getBoolean("mExplainAdfExportPermission");

            mShouldLoadServerAdfs = savedInstanceState.getBoolean("mShouldLoadServerAdfs");
            mIsSynchronizingAdfs = savedInstanceState.getBoolean("mIsSynchronizingAdfs");

            mServerAdfMetaData = (ArrayList<AdfSyncInfo>) savedInstanceState.getSerializable("mServerAdfMetaData");
            mLocalAdfMetaData = (ArrayList<AdfSyncInfo>) savedInstanceState.getSerializable("mLocalAdfMetaData");
            mCurrentLocation = savedInstanceState.getParcelable("mCurrentLocation");
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
                onAdfSelected(mData.get(getAdapterPosition()));
            }
        }
    }
}