package com.wally.wally.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
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

import java.util.ArrayList;
import java.util.List;

public class ADFChooser extends AppCompatActivity {
    private static final String TAG = ADFChooser.class.getSimpleName();
    private static final String EXPLORER_PACKAGE_NAME = "com.projecttango.tangoexplorer";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adfchooser);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (Utils.hasNoADFPermissions(getBaseContext())) {
            requestADFPermission();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Synchronize against disconnecting while the service is being used in the OpenGL thread or
        // in the UI thread.
        synchronized (this) {
            if (Utils.hasNoADFPermissions(getBaseContext())) {
                Log.i(TAG, "onResume: Didn't have ADF permission returning.");
            } else {
                loadRecycler();
            }
        }
    }

    private void loadRecycler() {
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerview_adf);
        assert recyclerView != null;
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        ADFListAdapter adapter = new ADFListAdapter(getADFList());
        recyclerView.setAdapter(adapter);
    }

    private ArrayList<Pair<String, TangoAreaDescriptionMetaData>> getADFList(){
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
        if(intent == null){
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + EXPLORER_PACKAGE_NAME));
        }
        startActivity(intent);
    }

    private void requestADFPermission() {
        startActivityForResult(
                Tango.getRequestPermissionIntent(Tango.PERMISSIONTYPE_ADF_LOAD_SAVE),
                Tango.TANGO_INTENT_ACTIVITYCODE);
    }


    private class ADFListAdapter extends RecyclerView.Adapter<ADFListAdapter.ADFViewHolder> {

        private final List<Pair<String, TangoAreaDescriptionMetaData>> mData;

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
                startActivity(MainActivity.newIntent(getBaseContext(), uuid));
            }
        }

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
    }
}