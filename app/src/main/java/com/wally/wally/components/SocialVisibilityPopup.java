package com.wally.wally.components;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.wally.wally.R;
import com.wally.wally.datacontroller.content.Visibility;

/**
 * Social visibility picker popup
 * Created by ioane5 on 6/1/16.
 */
public class SocialVisibilityPopup implements View.OnClickListener {

    private PopupWindow mPopup;
    private SocialVisibilityListener mListener;

    public void show(View anchor, SocialVisibilityListener listener) {
        mListener = listener;
        Context context = anchor.getContext();
        View pickerLayout = LayoutInflater.from(context).inflate(R.layout.social_visibility_layout, null);
        bindListener(pickerLayout);

        mPopup = new PopupWindow(pickerLayout, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        mPopup.setOutsideTouchable(true);
        mPopup.setFocusable(true);

        // Removes default background.
        mPopup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mPopup.showAsDropDown(anchor, -anchor.getWidth() / 5, -anchor.getHeight());
    }

    private void bindListener(View pickerLayout) {
        pickerLayout.findViewById(R.id.public_visibility).setOnClickListener(this);
        pickerLayout.findViewById(R.id.private_visibility).setOnClickListener(this);
        pickerLayout.findViewById(R.id.people_visibility).setOnClickListener(this);
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
        mPopup.dismiss();
        mListener.onVisibilityChosen(sv);
    }

    public interface SocialVisibilityListener {
        void onVisibilityChosen(int socialVisibilityMode);
    }
}
