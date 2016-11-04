package com.wally.wally.controllers.contentCreator;

import android.support.v7.widget.AppCompatCheckedTextView;
import android.view.View;

import com.wally.wally.R;
import com.wally.wally.components.RevealPopup;
import com.wally.wally.objects.content.Visibility;

/**
 * Social visibility picker popup
 * Created by ioane5 on 6/1/16.
 */
public class SocialVisibilityPopup extends RevealPopup implements View.OnClickListener {

    private SocialVisibilityListener mListener;

    public void show(View anchor, Visibility.SocialVisibility checkedVisibility, SocialVisibilityListener listener) {
        setUp(anchor, R.layout.social_visibility_layout);
        mListener = listener;
        initViews(mContentLayout, checkedVisibility);
    }

    private void initViews(View pickerLayout, Visibility.SocialVisibility checkedVisibility) {
        pickerLayout.findViewById(R.id.public_visibility).setOnClickListener(this);
        pickerLayout.findViewById(R.id.private_visibility).setOnClickListener(this);
        pickerLayout.findViewById(R.id.people_visibility).setOnClickListener(this);

        int viewId = 0;
        switch (checkedVisibility.getMode()) {
            case Visibility.SocialVisibility.PRIVATE:
                viewId = R.id.private_visibility;
                break;
            case Visibility.SocialVisibility.PUBLIC:
                viewId = R.id.public_visibility;
                break;
            case Visibility.SocialVisibility.PEOPLE:
                viewId = R.id.people_visibility;
                break;
        }
        AppCompatCheckedTextView tv = (AppCompatCheckedTextView) pickerLayout.findViewById(viewId);
        tv.setChecked(true);
    }

    @Override
    protected void onDismiss() {
        mListener = null;
    }

    @Override
    public void onClick(View v) {
        int sv;
        switch (v.getId()) {
            case R.id.public_visibility:
                sv = Visibility.SocialVisibility.PUBLIC;
                break;
            case R.id.private_visibility:
                sv = Visibility.SocialVisibility.PRIVATE;
                break;
            case R.id.people_visibility:
                sv = Visibility.SocialVisibility.PEOPLE;
                break;
            default:
                throw new IllegalArgumentException("Unknown visibility " + v);
        }
        mListener.onVisibilityChosen(sv);
        dismissPopup();
    }

    public interface SocialVisibilityListener {
        void onVisibilityChosen(int socialVisibilityMode);
    }
}
