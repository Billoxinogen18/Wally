package com.wally.wally.fragments;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.doodle.android.chips.ChipsView;
import com.wally.wally.App;
import com.wally.wally.R;
import com.wally.wally.Utils;
import com.wally.wally.components.CircleUserView;
import com.wally.wally.components.FilterRecyclerViewAdapter;
import com.wally.wally.components.UserSelectListener;
import com.wally.wally.datacontroller.user.User;
import com.wally.wally.userManager.SocialUser;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ioane5 on 5/31/16.
 */
public class PeopleChooserDialogFragment extends DialogFragment implements View.OnClickListener, UserSelectListener, ChipsView.ChipsListener {

    public static final String TAG = PeopleChooserDialogFragment.class.getSimpleName();

    private PeopleListAdapter mAdapter;
    private ChipsView mChipsView;

    public static PeopleChooserDialogFragment newInstance() {
        return new PeopleChooserDialogFragment();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        @SuppressLint("InflateParams")
        View dv = LayoutInflater.from(getContext())
                .inflate(R.layout.people_chooser_dialog, null, false);

        initViews(dv);

        if (savedInstanceState != null && savedInstanceState.containsKey("selectedUsers")) {
            @SuppressWarnings("unchecked")
            List<SocialUser> selectedUsers = (List<SocialUser>) savedInstanceState
                    .getSerializable("selectedUsers");
            mAdapter.setSelectedUsers(selectedUsers);
            for(SocialUser user : selectedUsers){
                mChipsView.addChip(user.getDisplayName(), user.getAvatarUrl(), user);
            }
        } else {
            mAdapter.setSelectedUsers(new ArrayList<SocialUser>());
        }

        builder.setView(dv);
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("selectedUsers", (Serializable) mAdapter.getSelectedUsers());
    }

    private void initViews(View v) {
        v.findViewById(R.id.btn_dismiss).setOnClickListener(this);

        mAdapter = new PeopleListAdapter();
        mAdapter.setData(App.getInstance().getUserManager().getUser().getFriends());
        mAdapter.setUserSelectListener(this);

        RecyclerView mRecycler = (RecyclerView) v.findViewById(R.id.recyclerview_people);
        mRecycler.setLayoutManager(new GridLayoutManager(getContext(), getGridColumnCount()));
        mRecycler.setAdapter(mAdapter);

        mChipsView = (ChipsView) v.findViewById(R.id.people_chips_view);

        mChipsView.setChipsListener(this);

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

    @Override
    public void onUserSelect(SocialUser user) {
        Log.d(TAG, "onUserSelect() called with: " + "user = [" + user + "]");
        mChipsView.addChip(user.getDisplayName(), user.getAvatarUrl(), user);
    }

    @Override
    public void onUserDeselect(SocialUser user) {
        mChipsView.removeChipBy(user);
    }

    @Override
    public void onChipAdded(ChipsView.Chip chip) {
        //TODO not needed
    }

    @Override
    public void onChipDeleted(ChipsView.Chip chip) {
        Log.d(TAG, "onChipDeleted() called with: " + "chip = [" + chip + "]");
        mAdapter.deselectUser((SocialUser) chip.getData());
    }

    @Override
    public void onTextChanged(CharSequence charSequence) {
        mAdapter.filter(charSequence.toString());
    }

    public interface PeopleChooserListener {
        void onPeopleChosen(List<SocialUser> users);
    }

    private class PeopleListAdapter extends FilterRecyclerViewAdapter<PeopleListAdapter.ViewHolder, SocialUser> implements View.OnClickListener {
        private List<SocialUser> mSelectedUsers;
        private UserSelectListener mUserSelectListener;

        public void setSelectedUsers(List<SocialUser> selectedUsers) {
            mSelectedUsers = selectedUsers;
        }

        public List<SocialUser> getSelectedUsers() {
            return mSelectedUsers;
        }

        public void deselectUser(SocialUser user){
            Log.d(TAG, "deselectUser() called with: " + "user = [" + user + "]");
            mSelectedUsers.remove(user);
            notifyItemChanged(getFilteredData().indexOf(user)); //TODO not optimal. should update view manually
        }

        public void setUserSelectListener(UserSelectListener userSelectListener){
            mUserSelectListener = userSelectListener;
        }


        @Override
        protected List<SocialUser> filterData(String query) {
            ArrayList<SocialUser> filtered = new ArrayList<>();
            if (!TextUtils.isEmpty(query)) {
                query = query.toLowerCase();
                for (SocialUser user : getFullData()) {
                    if (user.getDisplayName() != null && user.getDisplayName().toLowerCase().contains(query))
                        filtered.add(user);
                }
            } else {
                filtered.addAll(getFullData());
            }
            return filtered;
        }

        @Override
        public PeopleListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            CircleUserView v = new CircleUserView(getContext());
            v.setOnClickListener(this);
            return new PeopleListAdapter.ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(PeopleListAdapter.ViewHolder holder, int position) {
            holder.userView.setUser(getFilteredData().get(position));
            if (mSelectedUsers.contains(getFilteredData().get(position))) {
                holder.userView.setChecked(true);
            }
        }

        @Override
        public void onClick(View v) {
            CircleUserView userView = (CircleUserView) v;
            if (userView.isChecked()) {
                userView.setChecked(false);
                mSelectedUsers.remove(userView.getUser());
                mUserSelectListener.onUserDeselect(userView.getUser());
            } else {
                userView.setChecked(true);
                mSelectedUsers.add(userView.getUser());
                mUserSelectListener.onUserSelect(userView.getUser());
            }
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            CircleUserView userView;

            public ViewHolder(CircleUserView itemView) {
                super(itemView);
                userView = itemView;
            }
        }
    }
}
