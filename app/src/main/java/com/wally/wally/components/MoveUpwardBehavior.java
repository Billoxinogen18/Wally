package com.wally.wally.components;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.util.AttributeSet;
import android.view.View;

/**
 * Layout behavior to move view when snackBar enters.
 * Created by ioane5 on 3/24/16.
 */
public class MoveUpwardBehavior extends CoordinatorLayout.Behavior<View> {
    private float mInitialY = 0;
    private float mMaxYTranslation = 0;

    public MoveUpwardBehavior() {
        super();
    }

    public MoveUpwardBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
        return dependency instanceof Snackbar.SnackbarLayout;
    }

    @Override
    public void onDependentViewRemoved(CoordinatorLayout parent, View child, View dependency) {
        super.onDependentViewRemoved(parent, child, dependency);
        child.animate()
                .translationY(0)
                .setDuration(400)
                .setInterpolator(new FastOutSlowInInterpolator())
                .start();
    }

    public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {
        float translationY = Math.min(0, dependency.getTranslationY() - dependency.getHeight());
        child.setTranslationY(translationY);
        return true;
    }

}
