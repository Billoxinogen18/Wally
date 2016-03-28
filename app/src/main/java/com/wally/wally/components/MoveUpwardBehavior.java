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
    // it must be member fields, to avoid creating same variables multiple times.
    private int[] mBoundCheckerXY, mDependencyXY;

    public MoveUpwardBehavior() {
        super();
    }

    public MoveUpwardBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
        mBoundCheckerXY = new int[2];
        mDependencyXY = new int[2];
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
        View boundChecker = child;
        // this is special case of floatingActionMenu, because is larger than it appears.
        // we only need to check floatingActionButton overlap inside this FAM.
        if (boundChecker instanceof FloatingActionMenu) {
            boundChecker = ((FloatingActionMenu) child).getChildAt(0);
        }
        // get absolute position on screen.
        boundChecker.getLocationOnScreen(mBoundCheckerXY);
        dependency.getLocationOnScreen(mDependencyXY);
        // if view doesn't overlap dependant view do not animate.
        if (mBoundCheckerXY[0] > mDependencyXY[0] + dependency.getWidth() ||
                mDependencyXY[0] > mBoundCheckerXY[0] + boundChecker.getWidth()) {
            return false;
        }
        float translationY = Math.min(0, dependency.getTranslationY() - dependency.getHeight());
        child.setTranslationY(translationY);
        return true;
    }

}
