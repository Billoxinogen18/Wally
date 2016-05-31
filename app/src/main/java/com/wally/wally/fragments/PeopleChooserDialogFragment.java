package com.wally.wally.fragments;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wally.wally.R;

import java.util.List;

/**
 * Created by ioane5 on 5/31/16.
 */
public class PeopleChooserDialogFragment extends DialogFragment implements View.OnClickListener {

    public static final String TAG = PeopleChooserDialogFragment.class.getSimpleName();

    private RecyclerView mRecycler;

    public static PeopleChooserDialogFragment newInstance() {
        return new PeopleChooserDialogFragment();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dv = LayoutInflater.from(getContext())
                .inflate(R.layout.people_chooser_dialog, null, false);

        initViews(dv);
        builder.setView(dv);
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    private void initViews(View v) {
        v.findViewById(R.id.btn_dismiss).setOnClickListener(this);


        mRecycler = (RecyclerView) v.findViewById(R.id.recyclerview_people);
        mRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    // TODO add data params here here
    private void finishWithData() {
        PeopleChooserListener listener;
        if (getParentFragment() instanceof PeopleChooserListener) {
            listener = (PeopleChooserListener) getParentFragment();
        } else if (getActivity() instanceof PeopleChooserListener) {
            listener = (PeopleChooserListener) getActivity();
        } else {
            throw new IllegalStateException("No activity or parent fragment were Photo chooser listeners");
        }

        // TODO pass data here
        listener.onPeopleChosen();
        dismiss();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_dismiss:
                // TODO pass data
                finishWithData();
                break;
        }
    }

    public interface PeopleChooserListener {
        // TODO add params here
        void onPeopleChosen();
    }

    private class PeopleListAdapter extends RecyclerView.Adapter<PeopleListAdapter.VH> {

        private List<String> mData;

        // TODO change with your data type
        public PeopleListAdapter(List<String> data) {
            mData = data;
        }

        @SuppressLint("InflateParams")
        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            // TODO use your layout for row item
            View v = LayoutInflater.from(getContext()).inflate(R.layout.gallery_item, null);
            return new VH(v);
        }


        @Override
        public void onBindViewHolder(VH holder, int position) {
            // TODO bind data to row
        }

        @Override
        public int getItemCount() {
            return mData == null ? 0 : mData.size();
        }

        public class VH extends RecyclerView.ViewHolder implements View.OnClickListener {
            // TODO add vies here to later bind data

            public VH(View itemView) {
                super(itemView);
                itemView.setOnClickListener(this);
                // TODO init VH views
            }

            @Override
            public void onClick(View v) {
                // TODO maybe call some method in PeopleChooserDialogFragment
            }
        }
    }
}
