package com.wally.wally.components;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.LayoutRes;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import com.wally.wally.Utils;

/**
 * Abstract Popup Dialog class that manages reveal animations.
 * Created by ioane5 on 7/24/16.
 */
public abstract class RevealPopup {

    protected PopupWindow mPopup;
    protected View mContentLayout;
    private View mAnchor;

    protected void setUp(View anchor, @LayoutRes int layoutId) {
        mAnchor = anchor;
        Context context = anchor.getContext();
        mContentLayout = LayoutInflater.from(context).inflate(layoutId, null);

        mPopup = new PopupWindow(mContentLayout,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);

        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
            mPopup.setAnimationStyle(android.R.style.Animation_Dialog);
        } else {
            mContentLayout.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right,
                                           int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    v.removeOnLayoutChangeListener(this);
                    Utils.addCircularReveal(mAnchor, mContentLayout, true, null);
                }
            });
        }

        mPopup.setOutsideTouchable(true);
        mPopup.setFocusable(true);
        // Removes default background.
        mPopup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mPopup.showAtLocation((View) anchor.getParent(), Gravity.FILL, 0, 0);
        mContentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismissPopup();
            }
        });
    }

    protected void dismissPopup() {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
            mPopup.dismiss();
        } else {
            Utils.addCircularReveal(mAnchor, mPopup.getContentView(), false, new Utils.Callback<Void>() {
                @Override
                public void onResult(Void result) {
                    mPopup.dismiss();
                }

                @Override
                public void onError(Exception e) {

                }
            });
        }
        onDismiss();
    }

    protected abstract void onDismiss();
}
