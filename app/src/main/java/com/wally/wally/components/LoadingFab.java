package com.wally.wally.components;

import android.animation.LayoutTransition;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Animatable;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.wally.wally.R;


public class LoadingFab extends FrameLayout implements Runnable {

    private static final String TAG = LoadingFab.class.getSimpleName();

    private CircularProgressView mProgressBar;
    private FloatingActionButton mFab;

    public LoadingFab(Context context) {
        super(context);
    }

    public LoadingFab(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        init();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measureSpec = MeasureSpec.makeMeasureSpec(dpToPx(68), MeasureSpec.EXACTLY);
        super.onMeasure(measureSpec, measureSpec);
    }

    private void init() {
        setLayoutTransition(new LayoutTransition());
        mProgressBar = new CircularProgressView(getContext());

        LayoutParams lpProgress = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mProgressBar.setLayoutParams(lpProgress);
        mProgressBar.setIndeterminate(false);
        mProgressBar.setProgress(0);
        mProgressBar.setMaxProgress(100);

        LayoutParams lpFab = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lpFab.gravity = Gravity.CENTER;
        mFab = new FloatingActionButton(getContext());
        mFab.setLayoutParams(lpFab);
        mFab.setImageResource(R.drawable.ic_animated_timer);
        mFab.animate().rotationBy(180).setDuration(600).withEndAction(this).start();

        addView(mProgressBar);
        addView(mFab);
    }

    public int getProgress() {
        return (int) mProgressBar.getProgress();
    }

    public void setProgress(int progress) {
        mProgressBar.setProgress(progress);
        setVisibility(VISIBLE);
    }

    @Override
    public void run() {
        mFab.setRotation(0);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            mFab.setImageResource(R.drawable.ic_animated_timer);
        }
        if (mProgressBar.getProgress() < 100) {
            if (mFab.getDrawable() instanceof Animatable) {
                final Animatable anim = (Animatable) mFab.getDrawable();
                anim.start();
            }
            mFab.animate().setStartDelay(900).setDuration(600).rotationBy(180).withEndAction(this).start();
        } else {
            mFab.setImageResource(R.drawable.ic_add_white_24dp);
            mProgressBar.setVisibility(INVISIBLE);
        }
    }
}
