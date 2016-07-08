package com.wally.wally.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.atap.tangoservice.Tango;
import com.google.atap.tangoservice.TangoAreaDescriptionMetaData;
import com.google.atap.tangoservice.TangoErrorException;
import com.wally.wally.App;
import com.wally.wally.R;
import com.wally.wally.Utils;
import com.wally.wally.adfCreator.AdfCreatorActivity;
import com.wally.wally.datacontroller.adf.ADFService;
import com.wally.wally.datacontroller.adf.AdfMetaData;
import com.wally.wally.datacontroller.adf.AdfSyncInfo;
import com.wally.wally.datacontroller.callbacks.Callback;
import com.wally.wally.fragments.ImportExportPermissionDialogFragment;
import com.wally.wally.fragments.PersistentDialogFragment;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ADFChooser extends AppCompatActivity implements
        PersistentDialogFragment.PersistentDialogListener,
        GoogleApiClient.ConnectionCallbacks,
        ImportExportPermissionDialogFragment.ImportExportPermissionListener {

    private static final String TAG = ADFChooser.class.getSimpleName();

    // Permission Denied explain codes
    private static final int RC_EXPLAIN_LOCATION = 13;
    private static final int RC_EXPLAIN_ADF = 14;

    // Permission Request codes
    private static final int RC_REQ_AREA_LEARNING = 17;
    private static final int RC_REQ_LOCATION = 18;
    private static final int RC_REQ_ADF_IMPORT = 19;
    private static final int RC_REQ_ADF_EXPORT = 20;
    private static final int RC_REQ_ADF_CREATE = 21;

    private final Object adfLoadLock = new Object();
    private AdfSyncInfo mSelectedAdf;
    private Tango mTango;
    private ADFListAdapter mAdapter;
    private GoogleApiClient mGoogleApiClient;

    private boolean mExplainLocationPermission;
    private boolean mExplainAdfPermission;
    private boolean mShouldLoadServerAdfs = true;

    private ArrayList<AdfSyncInfo> mServerAdfMetaData;
    private ArrayList<AdfSyncInfo> mLocalAdfMetaData;
    private LatLng mCurrentLocation;
    private boolean mIsLoading = false;
    private View mLoadingView;

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

        updateLoadingAdfsStatus(true);
    }

    private void initViews() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerview_adf);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mAdapter = new ADFListAdapter();
        recyclerView.setAdapter(mAdapter);

        mLoadingView = findViewById(R.id.synchronizing_view);
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
    protected void onPause() {
        super.onPause();
        synchronized (this) {
            if (mTango != null) {
                mTango.disconnect();
            }
        }
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
        updateLoadingView();
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_REQ_AREA_LEARNING) {
            if (resultCode != RESULT_OK) {
                mExplainAdfPermission = true;
            }
        }else if (requestCode == RC_REQ_ADF_CREATE) {
            if (resultCode == RESULT_OK) {
                String uuid = data.getData().toString();
                Toast.makeText(getBaseContext(), "Shit yea", Toast.LENGTH_SHORT).show();
                //TODO export adf and start camera
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
        switch (requestCode) {
            case RC_EXPLAIN_LOCATION:
                requestLocationPermissions();
                break;
            case RC_EXPLAIN_ADF:
                requestADFPermission();
                break;
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
        } else {
            Log.w(TAG, "Didn't have adf permissions");
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
        Utils.getNewLocation(mGoogleApiClient, new Callback<LatLng>() {
            @Override
            public void onResult(LatLng result) {
                mCurrentLocation = result;
                loadServerADfList(mCurrentLocation);
            }

            @Override
            public void onError(Exception e) {

            }
        });
    }

    private void loadServerADfList(LatLng location) {
        updateLoadingAdfsStatus(true);

        ADFService s = App.getInstance().getDataController().getADFService();
        s.searchADfMetaDataNearLocation(location, new Callback<List<AdfMetaData>>() {
            @Override
            public void onResult(List<AdfMetaData> result) {
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
                // Sleep for 1 sec and then try again.
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tryLoadServerADfList();
                            }
                        });
                    }
                });
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
        updateLoadingAdfsStatus(false);
        // No need to bind Tango service, in onResume, local adfs will be reloaded thus this method
        // Will be called again
        ArrayList<AdfSyncInfo> synchronizedList = new ArrayList<>();
        for (AdfSyncInfo syncInfo : mServerAdfMetaData) {
            if (mLocalAdfMetaData.contains(syncInfo)) {
                syncInfo.setIsSynchronized(true);
                synchronizedList.add(syncInfo);
            }
        }
        Set<AdfSyncInfo> syncInfoSet = new HashSet<>();
        syncInfoSet.addAll(synchronizedList);
        syncInfoSet.addAll(mServerAdfMetaData);
        syncInfoSet.addAll(mLocalAdfMetaData);

        ArrayList<AdfSyncInfo> list = new ArrayList<>(syncInfoSet);
        Utils.sortWithLocation(list, mCurrentLocation);

        mAdapter.setData(list);
    }

    public void startWithNewADF(View v) {
        Intent intent = AdfCreatorActivity.newIntent(getBaseContext());
        startActivityForResult(intent, RC_REQ_ADF_CREATE);
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

    private void onAdfExported() {
        startArWithSelectedAdf();
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
        DialogFragment df = ImportExportPermissionDialogFragment
                .newInstance(uuid, ImportExportPermissionDialogFragment.IMPORT, RC_REQ_ADF_IMPORT);
        df.show(getSupportFragmentManager(), ImportExportPermissionDialogFragment.TAG);
    }

    private void requestExportPermission() {
        String uuid = mSelectedAdf.getAdfMetaData().getUuid();
        DialogFragment df = ImportExportPermissionDialogFragment
                .newInstance(uuid, ImportExportPermissionDialogFragment.EXPORT, RC_REQ_ADF_EXPORT);
        df.show(getSupportFragmentManager(), ImportExportPermissionDialogFragment.TAG);
    }

    @Override
    public void onPermissionGranted(int reqCode) {
        if (reqCode == RC_REQ_ADF_IMPORT) {
            startArWithSelectedAdf();
        } else if (reqCode == RC_REQ_ADF_EXPORT) {
            onAdfExported();
        }
    }

    @Override
    public void onPermissionDenied(int reqCode) {
        // Just leave it as is
    }

    private void startArWithSelectedAdf() {
        mSelectedAdf.getAdfMetaData().setLatLng(mCurrentLocation);
        startActivity(CameraARTangoActivity.newIntent(this, mSelectedAdf));
    }


    private void updateLoadingAdfsStatus(boolean isLoading) {
        mIsLoading = isLoading;
        updateSyncView(R.string.loading_adfs);
    }

    private void updateSyncStatus(boolean isLoading) {
        mIsLoading = isLoading;
        updateSyncView(R.string.synchronizing_adfs);
    }

    private void updateSyncView(@StringRes int loadingText) {
        TextView tv = (TextView) mLoadingView.findViewById(R.id.loading_text);
        tv.setText(loadingText);
        updateLoadingView();
    }

    private void updateLoadingView() {
        mLoadingView.setVisibility(mIsLoading ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("mSelectedAdf", mSelectedAdf);
        outState.putBoolean("mExplainAdfPermission", mExplainAdfPermission);
        outState.putBoolean("mExplainLocationPermission", mExplainLocationPermission);

        outState.putBoolean("mShouldLoadServerAdfs", mShouldLoadServerAdfs);
        outState.putBoolean("mIsLoading", mIsLoading);

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

            mShouldLoadServerAdfs = savedInstanceState.getBoolean("mShouldLoadServerAdfs");
            mIsLoading = savedInstanceState.getBoolean("mIsLoading");

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
            AdfSyncInfo syncInfo = mData.get(position);
            AdfMetaData data = syncInfo.getAdfMetaData();

            holder.name.setText(data.getName());
            holder.uuid.setText(data.getUuid());

            int statusResId;
            if (syncInfo.isSynchronized()) {
                statusResId = R.drawable.ic_adf_synchronized_24dp;
            } else if (syncInfo.isLocal()) {
                statusResId = R.drawable.ic_adf_on_device_24dp;
            } else {
                statusResId = R.drawable.ic_adf_on_cloud_24dp;
            }
            holder.status.setImageResource(statusResId);
        }

        @Override
        public int getItemCount() {
            return mData == null ? 0 : mData.size();
        }

        class ADFViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            TextView name;
            TextView uuid;
            ImageView status;

            public ADFViewHolder(View itemView) {
                super(itemView);
                name = (TextView) itemView.findViewById(R.id.tv_name);
                uuid = (TextView) itemView.findViewById(R.id.tv_uuid);
                status = (ImageView) itemView.findViewById(R.id.ic_adf_status);
                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                onAdfSelected(mData.get(getAdapterPosition()));
            }
        }
    }
}