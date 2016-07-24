package com.wally.wally.components;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.AppCompatCheckedTextView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import com.wally.wally.R;
import com.wally.wally.Utils;
import com.wally.wally.datacontroller.callbacks.Callback;
import com.wally.wally.datacontroller.content.Visibility;

/**
 * Social visibility picker popup
 * Created by ioane5 on 6/1/16.
 */
public class SocialVisibilityPopup implements View.OnClickListener {

    private PopupWindow mPopup;
    private View mAnchor;
    private SocialVisibilityListener mListener;

    public void show(View anchor, Visibility.SocialVisibility checkedVisibility, SocialVisibilityListener listener) {
        mListener = listener;
        mAnchor = anchor;
        Context context = anchor.getContext();
        final View pickerLayout = LayoutInflater.from(context).inflate(R.layout.social_visibility_layout, null);
        initViews(pickerLayout, checkedVisibility);

        mPopup = new PopupWindow(pickerLayout,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        mPopup.setOutsideTouchable(true);
        mPopup.setFocusable(true);

        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
            mPopup.setAnimationStyle(android.R.style.Animation_Dialog);
        } else {
            pickerLayout.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right,
                                           int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    v.removeOnLayoutChangeListener(this);
                    Utils.addCircularReveal(mAnchor, pickerLayout, true, null);
                }
            });
        }
        // Removes default background.
        mPopup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mPopup.showAtLocation((View) anchor.getParent(), Gravity.FILL, 0, 0);
        pickerLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismissPopup();
            }
        });
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

    public void dismissPopup() {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
            mPopup.dismiss();
        } else {
            Utils.addCircularReveal(mAnchor, mPopup.getContentView(), false, new Callback<Void>() {
                @Override
                public void onResult(Void result) {
                    mPopup.dismiss();
                }

                @Override
                public void onError(Exception e) {

                }
            });
        }
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
