package com.wally.wally.controllers.contentCreator.peopleChooser;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.doodle.android.chips.ChipsView;
import com.wally.wally.App;
import com.wally.wally.R;
import com.wally.wally.components.CircleTransform;
import com.wally.wally.components.RevealPopup;
import com.wally.wally.userManager.SocialUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * People chooser dialog for selecting social users.
 * <p>
 * Created by Meravici on 5/31/16.
 */
public class PeopleChooserPopup extends RevealPopup implements
        View.OnClickListener,
        ChipsView.ChipsListener {

    public static final String TAG = PeopleChooserPopup.class.getSimpleName();

    private PeopleListAdapter mAdapter;
    private ChipsView mChipsView;
    private PeopleChooserListener mListener;
    private Context mContext;

    public void show(View anchor, List<SocialUser> sharedWith, PeopleChooserListener listener) {
        setUp(anchor, R.layout.people_chooser_dialog);
        mListener = listener;
        mContext = anchor.getContext();
        initViews(mContentLayout);

        mAdapter.setSelectedUsers(sharedWith);
        for (SocialUser user : sharedWith) {
            mChipsView.addChip(user.getDisplayName(), user.getAvatarUrl(), user);
        }
    }

    private void initViews(View v) {
        //  v.findViewById(R.id.btn_dismiss).setOnClickListener(this);
        v.findViewById(R.id.btn_done).setOnClickListener(this);

        mAdapter = new PeopleListAdapter();
        mAdapter.setData(App.getInstance().getUserManager().getUser().getFriends());

        RecyclerView mRecycler = (RecyclerView) v.findViewById(R.id.recyclerview_people);
        mRecycler.setLayoutManager(new LinearLayoutManager(mContext));
        mRecycler.setAdapter(mAdapter);

        mChipsView = (ChipsView) v.findViewById(R.id.people_chips_view);
        mChipsView.setChipsListener(this);
    }

    @Override
    protected void onDismiss() {
        mListener = null;
    }

    private void finishWithData(List<SocialUser> users) {
        mListener.onPeopleChosen(users);
        dismissPopup();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
//            case R.id.btn_dismiss:
//                finishWithData(null);
//                break;
            case R.id.btn_done:
                finishWithData(mAdapter.getSelectedUsers());
                break;
        }
    }

    public void onUserSelect(SocialUser user) {
        mChipsView.addChip("", user.getAvatarUrl(), user);
    }

    public void onUserDeselect(SocialUser user) {
        mChipsView.removeChipBy(user);
    }

    @Override
    public void onChipAdded(ChipsView.Chip chip) {
        //not needed
    }

    @Override
    public void onChipDeleted(ChipsView.Chip chip) {
        mAdapter.deselectUser((SocialUser) chip.getData());
    }

    @Override
    public void onTextChanged(CharSequence charSequence) {
        mAdapter.filter(charSequence.toString());
    }

    public interface PeopleChooserListener {
        void onPeopleChosen(List<SocialUser> users);
    }

    private class PeopleListAdapter extends FilterRecyclerViewAdapter<PeopleListAdapter.ViewHolder, SocialUser> {
        private List<SocialUser> mSelectedUsers;

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
            View v = LayoutInflater.from(mContext).inflate(R.layout.circle_user_view, parent, false);
            return new PeopleListAdapter.ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(PeopleListAdapter.ViewHolder vh, int position) {
            SocialUser su = getFilteredData().get(position);
            boolean itemUpdated = vh.ticker.getTag() == su;
            boolean itemChecked = mSelectedUsers.contains(getFilteredData().get(position));

            vh.ticker.setTag(su);
            if (itemUpdated) {
                if (itemChecked) {
                    vh.ticker.setScaleX(0);
                    vh.ticker.setScaleY(0);
                    vh.ticker.animate().scaleX(1).scaleY(1).setInterpolator(new BounceInterpolator());
                } else {
                    vh.ticker.setScaleX(1);
                    vh.ticker.setScaleY(1);
                    vh.ticker.animate().scaleX(0).scaleY(0).setInterpolator(new AccelerateDecelerateInterpolator());
                }
            } else {
                if (itemChecked) {
                    vh.ticker.setScaleX(1);
                    vh.ticker.setScaleY(1);
                } else {
                    vh.ticker.setScaleX(0);
                    vh.ticker.setScaleY(0);
                }
            }
            Glide.with(mContext)
                    .load(su.getAvatarUrl())
                    .transform(new CircleTransform(mContext))
                    .into(vh.avatar);

            vh.name.setText(su.getDisplayName());
        }

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            private View ticker;
            private ImageView avatar;
            private TextView name;

            public ViewHolder(View itemView) {
                super(itemView);
                ticker = itemView.findViewById(R.id.tick_view);
                avatar = (ImageView) itemView.findViewById(R.id.avatar_image_view);
                name = (TextView) itemView.findViewById(R.id.name_text_view);

                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View view) {
                SocialUser su = getFilteredData().get(getAdapterPosition());
                if (mSelectedUsers.contains(su)) {
                    mSelectedUsers.remove(su);
                    onUserDeselect(su);
                } else {
                    mSelectedUsers.add(su);
                    onUserSelect(su);
                }
                notifyItemChanged(getAdapterPosition());
            }
        }
    }
}
