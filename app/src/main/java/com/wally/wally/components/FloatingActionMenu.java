/*
 * Copyright (C) 2014 Jerzy Chalupski
 * Copyright (C) 2015 Thomas Robert Altstidl
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wally.wally.components;

/**
 * A floating action button menu build specifically for AppCompat Design Library FloatingActionButton
 */

import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.KeyEventCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;

import com.wally.wally.R;

@SuppressWarnings("unused")
@SuppressLint("NewApi")
@CoordinatorLayout.DefaultBehavior(FloatingActionMenu.Behavior.class)
public class FloatingActionMenu extends ViewGroup {
    public static final int EXPAND_UP = 0;
    public static final int EXPAND_DOWN = 1;
    public static final int EXPAND_LEFT = 2;
    public static final int EXPAND_RIGHT = 3;

    public static final int LABELS_ON_LEFT_SIDE = 0;
    public static final int LABELS_ON_RIGHT_SIDE = 1;

    // Platform dependent animator for menu animations
    private FloatingActionMenuAnimator mAnimator;

    // Preallocated Rect for retrieving child background padding
    private Rect childBackgroundPadding = new Rect();

    // Dimensions for layout
    private int mButtonSpacing;
    private int mLabelsMargin;
    private int mLabelsVerticalOffset;

    private int mExpandDirection;
    private boolean mExpanded;

    private FloatingActionButton mMainButton;
    private int mButtonsCount;
    private int mMaxButtonWidth;
    private int mMaxButtonHeight;

    // Label attributes
    private int mLabelsStyle;
    private int mLabelsPosition;

    // Icon attributes
    private Drawable mCloseDrawable;
    private float mCloseAngle;

    // View and color used for dimming
    private View mDimmingView;
    private int mDimmingColor;

    private OnFloatingActionsMenuUpdateListener mListener;

    public FloatingActionMenu(Context context) {
        this(context, null);
    }

    public FloatingActionMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public FloatingActionMenu(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attributeSet) {
        mAnimator = new FloatingActionMenuAnimatorHoneycomb();

        mButtonSpacing = getResources().getDimensionPixelSize(R.dimen.fam_spacing);
        mLabelsMargin = getResources().getDimensionPixelSize(R.dimen.fam_label_spacing);
        mLabelsVerticalOffset = 0;

        TypedArray attr = context.obtainStyledAttributes(attributeSet, R.styleable.FloatingActionMenu, 0, 0);
        mExpandDirection = attr.getInt(R.styleable.FloatingActionMenu_fabMenuExpandDirection, EXPAND_UP);
        mLabelsPosition = attr.getInt(R.styleable.FloatingActionMenu_fabMenuLabelPosition, LABELS_ON_LEFT_SIDE);
        mLabelsStyle = attr.getResourceId(R.styleable.FloatingActionMenu_fabMenuLabelStyle, 0);
        mCloseDrawable = attr.getDrawable(R.styleable.FloatingActionMenu_fabMenuCloseIconSrc);
        mCloseAngle = attr.getFloat(R.styleable.FloatingActionMenu_fabMenuCloseIconAngle, 0);
        mButtonSpacing = attr.getDimensionPixelSize(R.styleable.FloatingActionMenu_fabMenuSpacing, mButtonSpacing);
        attr.recycle();

        if (mLabelsStyle != 0 && expandsHorizontally()) {
            throw new IllegalStateException("Action labels in horizontal expand orientation is not supported.");
        }
        // So we can catch the back button
        setFocusableInTouchMode(true);
    }

    public void setOnFloatingActionsMenuUpdateListener(OnFloatingActionsMenuUpdateListener listener) {
        mListener = listener;
    }

    private boolean expandsHorizontally() {
        return mExpandDirection == EXPAND_LEFT || mExpandDirection == EXPAND_RIGHT;
    }

    private void setupMainButton() {
        mMainButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                toggle();
            }
        });
        mAnimator.buildAnimationForDrawable(mMainButton, mCloseAngle, mCloseDrawable);
    }

    @SuppressWarnings("deprecation")
    private int getColor(@ColorRes int id) {
        return getResources().getColor(id);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        measureChildren(widthMeasureSpec, heightMeasureSpec);

        int width = 0;
        int height = 0;

        mMaxButtonWidth = 0;
        mMaxButtonHeight = 0;
        int maxLabelWidth = 0;

        for (int i = 0; i < mButtonsCount; i++) {
            View child = getChildAt(i);

            if (child.getVisibility() == GONE) {
                continue;
            }

            // Consider background padding in size measurement to account for compatibility shadow
            child.getBackground().getPadding(childBackgroundPadding);

            if (!expandsHorizontally()) {
                mMaxButtonWidth = Math.max(mMaxButtonWidth, child.getMeasuredWidth() - childBackgroundPadding.left - childBackgroundPadding.right);
                height += child.getMeasuredHeight() - childBackgroundPadding.top - childBackgroundPadding.bottom;
                TextView label = (TextView) child.getTag(R.id.fab_label);
                if (label != null) {
                    maxLabelWidth = Math.max(maxLabelWidth, label.getMeasuredWidth());
                }
            } else {
                width += child.getMeasuredWidth() - childBackgroundPadding.left - childBackgroundPadding.right;
                mMaxButtonHeight = Math.max(mMaxButtonHeight, child.getMeasuredHeight() - childBackgroundPadding.top - childBackgroundPadding.bottom);
            }
        }

        LayoutParams mainButtonParams = (LayoutParams) mMainButton.getLayoutParams();
        Log.i("FAB Menu", "Main Button Params: " + mainButtonParams.topMargin);
        if (!expandsHorizontally()) {
            width = mMaxButtonWidth + (maxLabelWidth > 0 ? maxLabelWidth + mLabelsMargin : 0);
            width += mainButtonParams.leftMargin + mainButtonParams.rightMargin;
            height += mButtonSpacing * (mButtonsCount - 1);
            height = adjustForOvershoot(height);
            height += (mExpandDirection == EXPAND_UP) ? mainButtonParams.bottomMargin + childBackgroundPadding.top
                    : mainButtonParams.topMargin + childBackgroundPadding.bottom;
        } else {
            height = mMaxButtonHeight;
            height += mainButtonParams.topMargin + mainButtonParams.rightMargin;
            width += mButtonSpacing * (mButtonsCount - 1);
            width = adjustForOvershoot(width);
            width += (mExpandDirection == EXPAND_LEFT) ? mainButtonParams.rightMargin + childBackgroundPadding.left
                    : mainButtonParams.leftMargin + childBackgroundPadding.right;
        }

        setMeasuredDimension(width, height);
    }

    private int adjustForOvershoot(int dimension) {
        return dimension * 12 / 10;
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        switch (mExpandDirection) {
            case EXPAND_UP:
            case EXPAND_DOWN:
                boolean expandUp = mExpandDirection == EXPAND_UP;

                // Consider margin and background padding to account for compatibility shadow
                mMainButton.getBackground().getPadding(childBackgroundPadding);
                LayoutParams mainButtonParamsHorizontal = (LayoutParams) mMainButton.getLayoutParams();

                int addButtonY = expandUp ? b - t - mMainButton.getMeasuredHeight() + childBackgroundPadding.top + childBackgroundPadding.bottom - mainButtonParamsHorizontal.bottomMargin : mainButtonParamsHorizontal.topMargin;
                // Ensure mMainButton is centered on the line where the buttons should be
                int buttonsHorizontalCenter = mLabelsPosition == LABELS_ON_LEFT_SIDE
                        ? r - l - mMaxButtonWidth / 2 - mainButtonParamsHorizontal.rightMargin
                        : mMaxButtonWidth / 2 + mainButtonParamsHorizontal.leftMargin;
                int addButtonLeft = buttonsHorizontalCenter - (mMainButton.getMeasuredWidth() - childBackgroundPadding.left - childBackgroundPadding.right) / 2;
                mMainButton.layout(addButtonLeft - childBackgroundPadding.left, addButtonY - childBackgroundPadding.top, addButtonLeft - childBackgroundPadding.left + mMainButton.getMeasuredWidth(), addButtonY - childBackgroundPadding.top + mMainButton.getMeasuredHeight());
                addButtonY -= childBackgroundPadding.top;

                int labelsOffset = mMaxButtonWidth / 2 + mLabelsMargin;
                int labelsXNearButton = mLabelsPosition == LABELS_ON_LEFT_SIDE
                        ? buttonsHorizontalCenter - labelsOffset
                        : buttonsHorizontalCenter + labelsOffset;

                int nextY = expandUp ?
                        addButtonY + childBackgroundPadding.top - mButtonSpacing :
                        addButtonY + mMainButton.getMeasuredHeight() - childBackgroundPadding.top - childBackgroundPadding.bottom + mButtonSpacing;

                for (int i = mButtonsCount - 1; i >= 0; i--) {
                    final View child = getChildAt(i);

                    if (child == mMainButton || child.getVisibility() == GONE) continue;

                    // Consider background padding to account for compatibility shadow
                    child.getBackground().getPadding(childBackgroundPadding);
                    int childX = buttonsHorizontalCenter - (child.getMeasuredWidth() - childBackgroundPadding.left - childBackgroundPadding.right) / 2;
                    int childY = expandUp ? nextY - child.getMeasuredHeight() + childBackgroundPadding.top + childBackgroundPadding.bottom : nextY;
                    child.layout(childX - childBackgroundPadding.left, childY - childBackgroundPadding.top, childX - childBackgroundPadding.left + child.getMeasuredWidth(), childY - childBackgroundPadding.top + child.getMeasuredHeight());
                    childY -= childBackgroundPadding.top;

                    float collapsedTranslation = addButtonY - childY;
                    float expandedTranslation = 0f;

                    mAnimator.prepareView(child, expandedTranslation, collapsedTranslation, mExpanded, false);

                    LayoutParams params = (LayoutParams) child.getLayoutParams();
                    if (!params.isAnimated()) {
                        mAnimator.buildAnimationForView(child, mExpandDirection, expandedTranslation, collapsedTranslation);
                        params.setAnimated(true);
                    }

                    View label = (View) child.getTag(R.id.fab_label);
                    if (label != null) {
                        int labelXAwayFromButton = mLabelsPosition == LABELS_ON_LEFT_SIDE
                                ? labelsXNearButton - label.getMeasuredWidth()
                                : labelsXNearButton + label.getMeasuredWidth();

                        int labelLeft = mLabelsPosition == LABELS_ON_LEFT_SIDE
                                ? labelXAwayFromButton
                                : labelsXNearButton;

                        int labelRight = mLabelsPosition == LABELS_ON_LEFT_SIDE
                                ? labelsXNearButton
                                : labelXAwayFromButton;

                        int labelTop = childY - mLabelsVerticalOffset + (child.getMeasuredHeight() - label.getMeasuredHeight()) / 2;

                        label.layout(labelLeft, labelTop, labelRight, labelTop + label.getMeasuredHeight());

                        label.setOnTouchListener(new PairedTouchListener(child));
                        child.setOnTouchListener(new PairedTouchListener(label));

                        mAnimator.prepareView(label, expandedTranslation, collapsedTranslation, mExpanded, false);

                        LayoutParams labelParams = (LayoutParams) label.getLayoutParams();
                        if (!labelParams.isAnimated()) {
                            mAnimator.buildAnimationForView(label, mExpandDirection, expandedTranslation, collapsedTranslation);
                            labelParams.setAnimated(true);
                        }
                    }

                    nextY = expandUp ?
                            childY + childBackgroundPadding.top - mButtonSpacing :
                            childY + child.getMeasuredHeight() - childBackgroundPadding.top - childBackgroundPadding.right + mButtonSpacing;
                }
                break;

            case EXPAND_LEFT:
            case EXPAND_RIGHT:
                boolean expandLeft = mExpandDirection == EXPAND_LEFT;

                // Consider margin and background padding to account for compatibility shadow
                mMainButton.getBackground().getPadding(childBackgroundPadding);
                LayoutParams mainButtonParamsVertical = (LayoutParams) mMainButton.getLayoutParams();

                int addButtonX = expandLeft ? r - l - mMainButton.getMeasuredWidth() + childBackgroundPadding.right - mainButtonParamsVertical.rightMargin : mainButtonParamsVertical.leftMargin - childBackgroundPadding.left;
                // Ensure mMainButton is centered on the line where the buttons should be
                int addButtonTop = b - t - mMaxButtonHeight + (mMaxButtonHeight - mMainButton.getMeasuredHeight() - childBackgroundPadding.top - childBackgroundPadding.bottom) / 2 - mainButtonParamsVertical.bottomMargin + childBackgroundPadding.bottom;
                mMainButton.layout(addButtonX, addButtonTop, addButtonX + mMainButton.getMeasuredWidth(), addButtonTop + mMainButton.getMeasuredHeight());

                int nextX = expandLeft ?
                        addButtonX + childBackgroundPadding.left - mButtonSpacing :
                        addButtonX + mMainButton.getMeasuredWidth() - childBackgroundPadding.left - childBackgroundPadding.right + mButtonSpacing;

                for (int i = mButtonsCount - 1; i >= 0; i--) {
                    final View child = getChildAt(i);

                    if (child == mMainButton || child.getVisibility() == GONE) continue;

                    // Consider background padding to account for compatibility shadow
                    child.getBackground().getPadding(childBackgroundPadding);
                    int childX = expandLeft ? nextX - child.getMeasuredWidth() + childBackgroundPadding.right : nextX - childBackgroundPadding.left;
                    int childY = addButtonTop + (mMainButton.getMeasuredHeight() - child.getMeasuredHeight()) / 2;
                    child.layout(childX, childY, childX + child.getMeasuredWidth(), childY + child.getMeasuredHeight());

                    float collapsedTranslation = addButtonX - childX;
                    float expandedTranslation = 0f;

                    mAnimator.prepareView(child, expandedTranslation, collapsedTranslation, mExpanded, true);

                    LayoutParams params = (LayoutParams) child.getLayoutParams();
                    if (!params.isAnimated()) {
                        mAnimator.buildAnimationForView(child, mExpandDirection, expandedTranslation, collapsedTranslation);
                        params.setAnimated(true);
                    }

                    nextX = expandLeft ?
                            childX + childBackgroundPadding.left - mButtonSpacing :
                            childX + child.getMeasuredWidth() - childBackgroundPadding.left - childBackgroundPadding.right + mButtonSpacing;
                }

                break;
        }
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override
    protected LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mMainButton = (FloatingActionButton) getChildAt(0);
        bringChildToFront(mMainButton);
        setupMainButton();

        mButtonsCount = getChildCount();

        if (mLabelsStyle != 0) {
            createLabels();
        }
    }

    private void createLabels() {
        Context context = new ContextThemeWrapper(getContext(), mLabelsStyle);

        for (int i = 0; i < mButtonsCount; i++) {
            FloatingActionButton button = (FloatingActionButton) getChildAt(i);
            CharSequence title = button.getContentDescription();

            if (button == mMainButton || title == null ||
                    button.getTag(R.id.fab_label) != null) continue;

            TextView label = new TextView(context);
            //noinspection deprecation
            label.setTextAppearance(getContext(), mLabelsStyle);
            label.setText(title);
            addView(label);

            button.setTag(R.id.fab_label, label);
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        mMainButton.setEnabled(enabled);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && mExpanded) {
            KeyEventCompat.startTracking(event);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && mExpanded) {
            collapse();
            return true;
        }
        return false;
    }

    /**
     * Method to easily setup a dimming for the specified view with the specified color
     *
     * @param dimmingView  the view to use for dimming (the background color will be animated)
     * @param dimmingColor the color to use for dimming (in expanded state)
     */
    public void setupWithDimmingView(View dimmingView, @ColorInt int dimmingColor) {
        mDimmingView = dimmingView;
        mDimmingColor = dimmingColor;
        mAnimator.buildAnimationForDimming(mDimmingView, mDimmingColor);
        // apply the appbar elevation so the dim gets rendered over it
        ViewCompat.setElevation(this, getContext().getResources().getDimensionPixelSize(R.dimen.fab_elevation));
        ViewCompat.setElevation(mDimmingView, getContext().getResources().getDimensionPixelSize(R.dimen.dim_elevation));
        // set click listener and disable clicks
        mDimmingView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                collapse();
            }
        });
        mDimmingView.setClickable(false);
    }

    /**
     * Collapse the FloatingActionMenu with an animation
     */
    public void collapse() {
        collapse(true);
    }

    /* Start Public API methods */

    /**
     * Collapse the FloatingActionMenu immediately without an animation
     */
    public void collapseImmediately() {
        collapse(false);
    }

    /**
     * Collapse the FloatingActionMenu
     *
     * @param animate whether it should be animated
     */
    public void collapse(boolean animate) {
        if (mExpanded) {
            mExpanded = false;
            mAnimator.startCollapseAnimation(animate);

            if (mListener != null) {
                mListener.onMenuCollapsed();
            }

            // So we don't catch the back button anymore
            clearFocus();
            if (mDimmingView != null) {
                mDimmingView.setClickable(false);
            }
        }
    }

    /**
     * Expand the FloatingActionMenu with an animation
     */
    public void expand() {
        expand(true);
    }

    /**
     * Expand the FloatingActionMenu immediately without an animation
     */
    public void expandImmediately() {
        expand(false);
    }

    /**
     * Expand the FloatingActionMenu
     *
     * @param animate whether it should be animated
     */
    public void expand(boolean animate) {
        if (!mExpanded) {
            mExpanded = true;
            mAnimator.startExpandAnimation(animate);

            if (mListener != null) {
                mListener.onMenuExpanded();
            }

            // So we can catch the back button
            requestFocus();
            if (mDimmingView != null) {
                mDimmingView.setClickable(true);
            }
        }
    }

    /**
     * Toggle the FloatingActionMenu
     * This will collapse it when it is currently expanded and expand it when it is currently collapsed.
     */
    public void toggle() {
        if (mExpanded) {
            collapse();
        } else {
            expand();
        }
    }

    /**
     * Check whether the FloatingActionMenu is expanded
     *
     * @return true if expanded, false if collapsed
     */
    public boolean isExpanded() {
        return mExpanded;
    }

    /**
     * Add a new FloatingActionButton to the FloatingActionMenu
     *
     * @param button the FloatingActionButton to add
     */
    public void addButton(FloatingActionButton button) {
        addView(button, mButtonsCount - 1);
        mButtonsCount++;

        if (mLabelsStyle != 0) {
            createLabels();
        }
    }

    /**
     * Remove an existing FloatingActionButton from the FloatingActionMenu
     *
     * @param button the FloatingActionButton to remove
     */
    public void removeButton(FloatingActionButton button) {
        removeView((View) button.getTag(R.id.fab_label));
        removeView(button);
        button.setTag(R.id.fab_label, null);
        mButtonsCount--;
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState savedState = new SavedState(superState);
        savedState.mExpanded = mExpanded;

        return savedState;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof SavedState) {
            SavedState savedState = (SavedState) state;
            mExpanded = savedState.mExpanded;

            mAnimator.prepareDrawable(mMainButton, mCloseAngle, mExpanded);
            mAnimator.prepareDimming(mDimmingView, mDimmingColor, mExpanded);

            super.onRestoreInstanceState(savedState.getSuperState());
        } else {
            super.onRestoreInstanceState(state);
        }
    }

    /* End Public API methods */

    public interface OnFloatingActionsMenuUpdateListener {
        void onMenuExpanded();

        void onMenuCollapsed();
    }

    /**
     * Base interface for the platform dependent animation implementations
     */
    public interface FloatingActionMenuAnimator {
        // Interpolators used for the animations
        Interpolator EXPAND_TRANSLATION_INTERPOLATOR = new OvershootInterpolator();
        Interpolator EXPAND_ALPHA_INTERPOLATOR = new DecelerateInterpolator();
        Interpolator COLLAPSE_INTERPOLATOR = new DecelerateInterpolator(3f);
        Interpolator DRAWABLE_INTERPOLATOR = new OvershootInterpolator();
        // Duration used for the animations
        int ANIMATION_DURATION = 300;

        /**
         * Called to start the expand animation implementation
         * Note: this should also cancel a running collapse animation
         *
         * @param animate whether the expansion should be animated or not
         */
        void startExpandAnimation(boolean animate);

        /**
         * Called to start the collapse animation implementation
         * Note: this should also cancel a running expand animation
         *
         * @param animate whether the collapse should be animated or not
         */
        void startCollapseAnimation(boolean animate);

        /**
         * Called to add an animation for a specific view (floating action button or label)
         *
         * @param view                 the view for which to build an animation
         * @param direction            the direction in which the view animates
         * @param expandedTranslation  the translation of the view in expanded state
         * @param collapsedTranslation the translation of the view in collapsed state
         */
        void buildAnimationForView(View view, int direction, float expandedTranslation, float collapsedTranslation);

        /**
         * Called to prepare a view (floating action button or label) during a layout pass
         *
         * @param view                 the view to prepare
         * @param expandedTranslation  the translation of the view in expanded state
         * @param collapsedTranslation the translation of the view in collapsed state
         * @param expanded             the state used in layout
         * @param expandsHorizontally  the orientation in which the view translates
         */
        void prepareView(View view, float expandedTranslation, float collapsedTranslation, boolean expanded, boolean expandsHorizontally);

        /**
         * Called to add an animation for a specific drawable
         *
         * @param button   the floating action button for which to animate the drawable
         * @param angle    the angle of the drawable in expanded state
         * @param drawable the (optional) drawable in expanded state
         */
        void buildAnimationForDrawable(FloatingActionButton button, float angle, Drawable drawable);

        /**
         * Called to prepare a drawable
         *
         * @param button   the floating action button with the drawable to prepare
         * @param angle    the angle of the drawable in expanded state
         * @param expanded the state used in layout
         */
        void prepareDrawable(FloatingActionButton button, float angle, boolean expanded);

        /**
         * Called to add an animation for dimming
         *
         * @param dimmingView  the view for which to animate the dimming
         * @param dimmingColor the color to use for dimming
         */
        void buildAnimationForDimming(View dimmingView, int dimmingColor);

        /**
         * Called to prepare the dimming
         *
         * @param dimmingView  the view for which to prepare the dimming
         * @param dimmingColor the color to use for dimming
         * @param expanded     the state used in layout
         */
        void prepareDimming(View dimmingView, int dimmingColor, boolean expanded);
    }

    public static class SavedState extends BaseSavedState {
        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {

            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        public boolean mExpanded;

        public SavedState(Parcelable parcel) {
            super(parcel);
        }

        private SavedState(Parcel in) {
            super(in);
            mExpanded = in.readInt() == 1;
        }

        @Override
        public void writeToParcel(@NonNull Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(mExpanded ? 1 : 0);
        }
    }

    /**
     * Behavior designed for use with {@link FloatingActionMenu} instances. It's main function
     * is to move all {@link FloatingActionButton}s views inside {@link FloatingActionMenu} so
     * that any displayed {@link Snackbar}s do not cover them.
     */
    public static class Behavior extends CoordinatorLayout.Behavior<FloatingActionMenu> {

        /**
         * Default constructor for instantiating Behaviors.
         */
        public Behavior() {
        }

        public Behavior(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        @Override
        public boolean layoutDependsOn(CoordinatorLayout parent, FloatingActionMenu child, View dependency) {
            return dependency instanceof Snackbar.SnackbarLayout;
        }

        @Override
        public boolean onDependentViewChanged(CoordinatorLayout parent, FloatingActionMenu child, View dependency) {
            float translationY = Math.min(0, dependency.getTranslationY() - dependency.getHeight());
            child.setTranslationY(translationY);
            return true;
        }
    }

    public static class PairedTouchListener implements View.OnTouchListener {
        // The view the OnTouchListener is paired to
        private View mPairedView;

        public PairedTouchListener(View pairedView) {
            mPairedView = pairedView;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            v.onTouchEvent(event);
            // put the location in the middle of the paired view
            event.setLocation(mPairedView.getWidth() / 2, mPairedView.getHeight() / 2);
            mPairedView.onTouchEvent(event);
            return true;
        }
    }

    public static class FloatingActionMenuAnimatorHoneycomb implements FloatingActionMenuAnimator {
        // AnimatorSets used for expand and collapse animation
        private AnimatorSet mExpandAnimation = new AnimatorSet().setDuration(ANIMATION_DURATION);
        private AnimatorSet mCollapseAnimation = new AnimatorSet().setDuration(ANIMATION_DURATION);

        @Override
        public void startExpandAnimation(boolean animate) {
            mCollapseAnimation.cancel();
            mExpandAnimation.setDuration(animate ? ANIMATION_DURATION : 0);
            mExpandAnimation.start();
        }

        @Override
        public void startCollapseAnimation(boolean animate) {
            mCollapseAnimation.setDuration(animate ? ANIMATION_DURATION : 0);
            mCollapseAnimation.start();
            mExpandAnimation.cancel();
        }

        @Override
        public void buildAnimationForView(View view, int direction, float expandedTranslation, float collapsedTranslation) {
            // Alpha animations
            ObjectAnimator mExpandAlpha = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
            mExpandAlpha.setInterpolator(EXPAND_ALPHA_INTERPOLATOR);
            ObjectAnimator mCollapseAlpha = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f);
            mCollapseAlpha.setInterpolator(COLLAPSE_INTERPOLATOR);

            // Translation animations
            ObjectAnimator mExpandTranslation = new ObjectAnimator();
            mExpandTranslation.setInterpolator(EXPAND_TRANSLATION_INTERPOLATOR);
            ObjectAnimator mCollapseTranslation = new ObjectAnimator();
            mCollapseTranslation.setInterpolator(COLLAPSE_INTERPOLATOR);
            switch (direction) {
                case FloatingActionMenu.EXPAND_UP:
                case FloatingActionMenu.EXPAND_DOWN:
                    mExpandTranslation.setPropertyName("translationY");
                    mCollapseTranslation.setPropertyName("translationY");
                    break;
                case FloatingActionMenu.EXPAND_LEFT:
                case FloatingActionMenu.EXPAND_RIGHT:
                    mExpandTranslation.setPropertyName("translationX");
                    mCollapseTranslation.setPropertyName("translationX");
                    break;
            }
            mExpandTranslation.setFloatValues(collapsedTranslation, expandedTranslation);
            mCollapseTranslation.setFloatValues(expandedTranslation, collapsedTranslation);
            mExpandTranslation.setTarget(view);
            mCollapseTranslation.setTarget(view);

            // Add animations to AnimationSet
            mExpandAnimation.play(mExpandAlpha);
            mExpandAnimation.play(mExpandTranslation);
            mCollapseAnimation.play(mCollapseAlpha);
            mCollapseAnimation.play(mCollapseTranslation);
        }

        @Override
        public void prepareView(View view, float expandedTranslation, float collapsedTranslation, boolean expanded, boolean expandsHorizontally) {
            if (expandsHorizontally) {
                view.setTranslationX(expanded ? expandedTranslation : collapsedTranslation);
                view.setAlpha(expanded ? 1f : 0f);
            } else {
                view.setTranslationY(expanded ? expandedTranslation : collapsedTranslation);
                view.setAlpha(expanded ? 1f : 0f);
            }
        }

        @Override
        public void buildAnimationForDrawable(FloatingActionButton button, float angle, Drawable drawable) {
            RotationTransitionDrawable mDrawable = new RotationTransitionDrawable(button.getDrawable(), drawable);
            mDrawable.setMaxRotation(angle);
            button.setImageDrawable(mDrawable);

            ObjectAnimator mExpandDrawableRotation = ObjectAnimator.ofFloat(mDrawable, "rotation", 0f, angle);
            mExpandDrawableRotation.setInterpolator(DRAWABLE_INTERPOLATOR);
            ObjectAnimator mCollapseDrawableRotation = ObjectAnimator.ofFloat(mDrawable, "rotation", angle, 0f);
            mCollapseDrawableRotation.setInterpolator(DRAWABLE_INTERPOLATOR);

            mExpandAnimation.play(mExpandDrawableRotation);
            mCollapseAnimation.play(mCollapseDrawableRotation);
        }

        @Override
        public void prepareDrawable(FloatingActionButton button, float angle, boolean expanded) {
            RotationTransitionDrawable mRotatingDrawable = (RotationTransitionDrawable) button.getDrawable();
            if (mRotatingDrawable != null) {
                mRotatingDrawable.setRotation(expanded ? angle : 0f);
            }
        }

        @Override
        public void buildAnimationForDimming(View dimmingView, int dimmingColor) {
            ObjectAnimator mExpandBackgroundColor = ObjectAnimator.ofInt(dimmingView, "backgroundColor", Color.TRANSPARENT, dimmingColor);
            mExpandBackgroundColor.setEvaluator(new ArgbEvaluator());
            ObjectAnimator mCollapseBackgroundColor = ObjectAnimator.ofInt(dimmingView, "backgroundColor", dimmingColor, Color.TRANSPARENT);
            mCollapseBackgroundColor.setEvaluator(new ArgbEvaluator());

            mExpandAnimation.play(mExpandBackgroundColor);
            mCollapseAnimation.play(mCollapseBackgroundColor);
        }

        @Override
        public void prepareDimming(View dimmingView, int dimmingColor, boolean expanded) {
            dimmingView.setBackgroundColor(expanded ? dimmingColor : Color.TRANSPARENT);
        }
    }

    /**
     * A drawable that animates the rotation and alpha value of the wrapped drawable
     */
    public static class RotationTransitionDrawable extends LayerDrawable {
        private float mRotation;
        private float mMaxRotation;

        private boolean mHasSecondDrawable;

        public RotationTransitionDrawable(Drawable drawable, Drawable closeDrawable) {
            super(closeDrawable == null ? new Drawable[]{drawable} : new Drawable[]{drawable, closeDrawable});
            mHasSecondDrawable = closeDrawable != null;
        }

        public float getRotation() {
            return mRotation;
        }

        public void setRotation(float rotation) {
            mRotation = rotation;
            invalidateSelf();
        }

        public float getMaxRotation() {
            return mMaxRotation;
        }

        public void setMaxRotation(float rotation) {
            mMaxRotation = rotation;
        }

        @Override
        public void draw(Canvas canvas) {
            canvas.save();
            if (mHasSecondDrawable) {
                int alpha = Math.min(Math.max(0, Math.round(mRotation / mMaxRotation * 255)), 255);
                canvas.rotate(mRotation, getBounds().centerX(), getBounds().centerY());
                getDrawable(0).setAlpha(255 - alpha);
                getDrawable(0).draw(canvas);
                canvas.rotate(-mMaxRotation, getBounds().centerX(), getBounds().centerY());
                getDrawable(1).setAlpha(alpha);
                getDrawable(1).draw(canvas);
            } else {
                canvas.rotate(mRotation, getBounds().centerX(), getBounds().centerY());
                super.draw(canvas);
            }
            canvas.restore();
        }
    }

    private class LayoutParams extends ViewGroup.MarginLayoutParams {
        // Tracker for efficient animation setting
        private boolean mAnimated;

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }

        public LayoutParams(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public boolean isAnimated() {
            return mAnimated;
        }

        public void setAnimated(boolean animated) {
            mAnimated = animated;
        }
    }


}
