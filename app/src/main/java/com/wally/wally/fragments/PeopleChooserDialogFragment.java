package com.wally.wally.fragments;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wally.wally.App;
import com.wally.wally.R;
import com.wally.wally.Utils;
import com.wally.wally.components.CircleUserView;
import com.wally.wally.userManager.SocialUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ioane5 on 5/31/16.
 */
public class PeopleChooserDialogFragment extends DialogFragment implements View.OnClickListener {

    public static final String TAG = PeopleChooserDialogFragment.class.getSimpleName();

    private RecyclerView mRecycler;
    private PeopleListAdapter mAdapter;

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

        mAdapter = new PeopleListAdapter(App.getInstance().getUserManager().getUser().getFriends());

        mRecycler = (RecyclerView) v.findViewById(R.id.recyclerview_people);
        mRecycler.setLayoutManager(new GridLayoutManager(getContext(), getGridColumnCount()));
        mRecycler.setAdapter(mAdapter);

    }

    private int getGridColumnCount() {
        // This is optimal quantity based on rotation.
        return (int) (Utils.getScreenWidthDpi(getContext()) / 100);
    }


    private void finishWithData(List<SocialUser> users) {
        PeopleChooserListener listener;
        if (getParentFragment() instanceof PeopleChooserListener) {
            listener = (PeopleChooserListener) getParentFragment();
        } else if (getActivity() instanceof PeopleChooserListener) {
            listener = (PeopleChooserListener) getActivity();
        } else {
            throw new IllegalStateException("No activity or parent fragment were Photo chooser listeners");
        }

        listener.onPeopleChosen(users);
        dismiss();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_dismiss:
                finishWithData(mAdapter.getSelectedUsers());
                break;
        }
    }

    public interface PeopleChooserListener {
        void onPeopleChosen(List<SocialUser> users);
    }

    private class PeopleListAdapter extends RecyclerView.Adapter<PeopleListAdapter.VH> {

        private List<SocialUser> mData;
        private List<SocialUser> mSelectedUsers;

        public PeopleListAdapter(List<SocialUser> data) {
            mData = data;
            mSelectedUsers = new ArrayList<>(); //TODO maybe hashSet
        }

        public List<SocialUser> getSelectedUsers(){
            return mSelectedUsers;
        }

        @SuppressLint("InflateParams")
        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            CircleUserView v = new CircleUserView(getContext());
            return new VH(v);
        }


        @Override
        public void onBindViewHolder(VH holder, int position) {
            holder.userView.setUser(mData.get(position));
            if(mSelectedUsers.contains(mData.get(position))){
                holder.userView.setChecked(true);
            }
        }

        @Override
        public int getItemCount() {
            return mData == null ? 0 : mData.size();
        }

        public class VH extends RecyclerView.ViewHolder implements View.OnClickListener {
            CircleUserView userView;

            public VH(CircleUserView itemView) {
                super(itemView);
                userView = itemView;
                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                if(userView.isChecked()){
                    userView.setChecked(false);
                    mSelectedUsers.remove(userView.getUser());
                }else{
                    userView.setChecked(true);
                    mSelectedUsers.add(userView.getUser());
                }
            }
        }
    }
}
