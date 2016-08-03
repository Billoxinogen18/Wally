package com.wally.wally.controllers.contentCreator.peopleChooser;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.doodle.android.chips.ChipsView;
import com.wally.wally.App;
import com.wally.wally.R;
import com.wally.wally.components.RevealPopup;
import com.wally.wally.userManager.SocialUser;

import java.util.List;

/**
 * People chooser dialog for selecting social users.
 * <p>
 * Created by Meravici on 5/31/16.
 */
public class PeopleChooserPopup extends RevealPopup implements
        View.OnClickListener,
        PeopleListAdapter.UserSelectListener,
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
        v.findViewById(R.id.btn_done).setOnClickListener(this);

        mAdapter = new PeopleListAdapter(mContext);
        mAdapter.setUserSelectListener(this);
        mAdapter.setData(App.getInstance().getSocialUserManager().getUser().getFriends());

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
            case R.id.btn_done:
                finishWithData(mAdapter.getSelectedUsers());
                break;
        }
    }

    @Override
    public void onUserSelect(SocialUser user) {
        mChipsView.addChip("", user.getAvatarUrl(), user);
    }

    @Override
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
}