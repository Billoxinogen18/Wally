package com.wally.wally.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.atap.tangoservice.Tango;
import com.google.atap.tangoservice.TangoAreaDescriptionMetaData;
import com.google.atap.tangoservice.TangoErrorException;
import com.wally.wally.R;
import com.wally.wally.Utils;
import com.wally.wally.fragments.PersistentDialogFragment;

import java.util.ArrayList;
import java.util.List;

public class ADFChooser extends AppCompatActivity implements PersistentDialogFragment.PersistentDialogListener {
    public static final int RC_AREA_LEARNING = 182;
    public static final int RC_EXPORT_ADF = 22;
    private static final String TAG = ADFChooser.class.getSimpleName();
    private static final String EXPLORER_PACKAGE_NAME = "com.projecttango.tangoexplorer";
    private static final String INTENT_CLASSPACKAGE = "com.projecttango.tango";
    private static final String INTENT_IMPORTEXPORT_CLASSNAME = "com.google.atap.tango.RequestImportExportActivity";

    // startActivityForResult requires a code number.
    private static final String EXTRA_KEY_SOURCEUUID = "SOURCE_UUID";
    private static final String EXTRA_KEY_DESTINATIONFILE = "DESTINATION_FILE";
    private static final int RC_EXPLAIN_EXPORT = 192;

    private boolean mFlagShowImportExplanation = false;
    private boolean mFlagStartUploading = false;
    private String mSelectedUUID;

    public static Intent newIntent(Context context) {
        Intent i = new Intent(context, ADFChooser.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        return i;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adfchooser);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (Utils.isTangoDevice(getBaseContext())) {
            if (!Utils.hasADFPermissions(getBaseContext())) {
                requestADFPermission();
            }
        } else {
            startActivity(CameraARStandardActivity.newIntent(getBaseContext()));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Synchronize against disconnecting while the service is being used in the OpenGL thread or
        // in the UI thread.
        synchronized (this) {
            if (Utils.isTangoDevice(getBaseContext()) && Utils.hasADFPermissions(getBaseContext())) {
                loadRecycler();
            } else {
                Log.i(TAG, "onResume: No Tango or Didn't have ADF permission returning.");
            }
        }

        Log.d(TAG, "onResume() called with: " + mFlagShowImportExplanation + " " + mFlagStartUploading);
        if (mFlagShowImportExplanation) {
            mFlagShowImportExplanation = false;

            PersistentDialogFragment.newInstance(this, RC_EXPLAIN_EXPORT,
                    R.string.explain_adf_export_permission,
                    R.string.give_permission,
                    R.string.close_application)
                    .show(getSupportFragmentManager(), PersistentDialogFragment.TAG);
        }
        if (mFlagStartUploading) {
            mFlagStartUploading = false;

            startUploading();
        }
    }

    private void loadRecycler() {
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerview_adf);
        assert recyclerView != null;
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        ADFListAdapter adapter = new ADFListAdapter(getADFList());
        recyclerView.setAdapter(adapter);
    }

    private ArrayList<Pair<String, TangoAreaDescriptionMetaData>> getADFList() {
        Tango mTango = new Tango(this);
        ArrayList<Pair<String, TangoAreaDescriptionMetaData>> mFullADFList = new ArrayList<>();
        ArrayList<String> mFullUUIDList = new ArrayList<>();
        try {
            mFullUUIDList = mTango.listAreaDescriptions();
        } catch (TangoErrorException e) {
            Toast.makeText(this, R.string.tango_error, Toast.LENGTH_SHORT).show();
        }
        if (mFullUUIDList.size() == 0) {
            Toast.makeText(this, R.string.no_adfs_tango_error, Toast.LENGTH_SHORT).show();
        }

        for (String uuid : mFullUUIDList) {
            TangoAreaDescriptionMetaData metadata = mTango.loadAreaDescriptionMetaData(uuid);
            mFullADFList.add(new Pair<>(uuid, metadata));
        }

        return mFullADFList;
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
                RC_AREA_LEARNING);
    }

    private void onAdfSelected(String uuid) {
        mSelectedUUID = uuid;

        Intent exportIntent = new Intent();
        exportIntent.setClassName(INTENT_CLASSPACKAGE, INTENT_IMPORTEXPORT_CLASSNAME);
        exportIntent.putExtra(EXTRA_KEY_SOURCEUUID, uuid);
        exportIntent.putExtra(EXTRA_KEY_DESTINATIONFILE, Utils.getAdfFilesFolder());
        startActivityForResult(exportIntent, RC_EXPORT_ADF);
    }

    @Override
    public void onDialogPositiveClicked(int requestCode) {
        if (requestCode == RC_EXPLAIN_EXPORT) {
            onAdfSelected(mSelectedUUID);
        }
    }

    @Override
    public void onDialogNegativeClicked(int requestCode) {
        if (requestCode == RC_EXPLAIN_EXPORT) {
            finish();
            System.exit(0);
        }
    }

    @SuppressWarnings("MissingPermission")
    private void startUploading() {
        Log.d(TAG, "startUploading() called with: " + mSelectedUUID);
        CameraARTangoActivity.newIntent(getBaseContext(), mSelectedUUID);
//        App.getInstance().getDataController().getADFService().upload(
//                Utils.getAdfFilesFolder() + "/" + mSelectedUUID,
//                mSelectedUUID,
//                new LatLng(41, 41),
//                new Callback<Void>() {
//
//                    @Override
//                    public void onResult(Void result) {
//                        Toast.makeText(ADFChooser.this, "SUCCESS UPLOADED FKIN", Toast.LENGTH_SHORT).show();
//                        Log.d(TAG, "onResult() called with: " + "result = [" + result + "]");
//                    }
//
//                    @Override
//                    public void onError(Exception e) {
//                        Log.d(TAG, "onError() called with: " + "e = [" + e + "]");
//                    }
//                });
        // TODO after uploaded startActivity(CameraARTangoActivity.newIntent(getBaseContext(), uuid));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult() called with: " + "requestCode = [" + requestCode + "], resultCode = [" + resultCode + "], data = [" + data + "]");
        if (requestCode == RC_EXPORT_ADF) {
            if (resultCode == RESULT_OK) {
                mFlagStartUploading = true;
            } else {
                mFlagShowImportExplanation = true;
            }
        } else if (requestCode == RC_AREA_LEARNING) {
            // TODO
            if (resultCode == RESULT_OK) {

            } else {

            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("mSelectedUUID", mSelectedUUID);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            mSelectedUUID = savedInstanceState.getString("mSelectedUUID");
        }
    }

    private class ADFListAdapter extends RecyclerView.Adapter<ADFListAdapter.ADFViewHolder> {

        private final List<Pair<String, TangoAreaDescriptionMetaData>> mData;

        public ADFListAdapter(List<Pair<String, TangoAreaDescriptionMetaData>> data) {
            mData = data;
        }

        @Override
        public ADFViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            @SuppressLint("InflateParams") View v = LayoutInflater.from(getBaseContext())
                    .inflate(R.layout.adf_list_item, null);
            return new ADFViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ADFViewHolder holder, int position) {
            Pair<String, TangoAreaDescriptionMetaData> data = mData.get(position);

            byte[] nameBytes = data.second.get(TangoAreaDescriptionMetaData.KEY_NAME);
            if (nameBytes == null) {
                holder.name.setVisibility(View.GONE);
            } else {
                holder.name.setVisibility(View.VISIBLE);
                holder.name.setText(new String(nameBytes));
            }
            holder.uuid.setText(data.first);
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
                String uuid = mData.get(getAdapterPosition()).first;
                onAdfSelected(uuid);
            }
        }
    }
}