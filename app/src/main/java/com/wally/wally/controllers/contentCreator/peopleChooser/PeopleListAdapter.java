package com.wally.wally.controllers.contentCreator.peopleChooser;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import com.wally.wally.userManager.SocialUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Meravici on 8/2/2016. yea
 */
public class PeopleListAdapter extends FilterRecyclerViewAdapter<PeopleListAdapter.ViewHolder, SocialUser> implements View.OnClickListener {
    private Context mContext;
    private List<SocialUser> mSelectedUsers;
    private UserSelectListener mUserSelectListener;

    public PeopleListAdapter(Context context) {
        mContext = context;
    }

    public List<SocialUser> getSelectedUsers() {
        return mSelectedUsers;
    }

    public void setSelectedUsers(List<SocialUser> selectedUsers) {
        mSelectedUsers = selectedUsers;
    }

    public void deselectUser(SocialUser user) {
        mSelectedUsers.remove(user);
        notifyItemChanged(getFilteredData().indexOf(user));
    }

    public void setUserSelectListener(UserSelectListener userSelectListener) {
        mUserSelectListener = userSelectListener;
    }


    @Override
    protected List<SocialUser> filterData(String query) {
        ArrayList<SocialUser> filtered = new ArrayList<>();
        if (!TextUtils.isEmpty(query)) {
            query = query.toLowerCase(Locale.US);
            for (SocialUser user : getFullData()) {
                if (user.getDisplayName() != null &&
                        user.getDisplayName().toLowerCase(Locale.US).contains(query))
                    filtered.add(user);
            }
        } else {
            filtered.addAll(getFullData());
        }
        return filtered;
    }

    @Override
    public PeopleListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        CircleUserView v = new CircleUserView(mContext);
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

    public interface UserSelectListener {
        void onUserSelect(SocialUser user);

        void onUserDeselect(SocialUser user);
    }
}