package com.wally.wally.tip;

import android.animation.Animator;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wally.wally.R;

/**
 * Created by Meravici on 8/4/2016. yea
 */
public class TipView extends LinearLayout implements View.OnTouchListener {
    private View toolbar;
    private TextView messageTV;
    private CheckBox neverShowAgain;

    private String id;
    private DismissListener dismissListener;
    private Handler mainThreadHandler;

    private Runnable hideRunnable;

    public TipView(Context context) {
        super(context);
        init();
    }

    public TipView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TipView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.tip, this);
        setVisibility(GONE);

        findViewById(R.id.tip).setOnTouchListener(this);

        messageTV = (TextView) findViewById(R.id.message);
        toolbar = findViewById(R.id.toolbar);

        neverShowAgain = (CheckBox) findViewById(R.id.never_show_again);
        neverShowAgain.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    if (dismissListener != null) {
                        dismissListener.onDismiss(id);
                    }
                    mainThreadHandler.postDelayed(hideRunnable, 500);
                }
            }
        });


        mainThreadHandler = new Handler(Looper.getMainLooper());

        hideRunnable = new Runnable() {
            @Override
            public void run() {
                animate().scaleX(0).scaleY(0).setInterpolator(new AccelerateDecelerateInterpolator())
                        .withEndAction(new Runnable() {
                            @Override
                            public void run() {
                                reset();
                            }
                        });
            }
        };
    }

    public void setDismissListener(DismissListener dismissListener){
        this.dismissListener = dismissListener;
    }

    public void show(final String message, String id, int durationMs) {
        reset();
        this.id = id;

        mainThreadHandler.removeCallbacks(hideRunnable);

        mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                if (message != null && !message.isEmpty()) {
                    messageTV.setText(message);
                }

                setScaleX(0);
                setScaleY(0);
                setVisibility(VISIBLE);
                animate().scaleX(1).scaleY(1).setInterpolator(new OvershootInterpolator());
            }
        });

        if (durationMs > 0) {
            mainThreadHandler.postDelayed(hideRunnable, durationMs);
        }
    }


    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (toolbar.getVisibility() == INVISIBLE) {

                toolbar.setVisibility(VISIBLE);
                float finalRadius = (float) Math.hypot(toolbar.getWidth(), toolbar.getHeight());

                Animator anim = ViewAnimationUtils.createCircularReveal(
                        toolbar,
                        (int) motionEvent.getX(),
                        (int) motionEvent.getY(),
                        5, finalRadius);
                anim.setInterpolator(new AccelerateDecelerateInterpolator());
                anim.start();
            } else {
                toolbar.setVisibility(INVISIBLE);
            }
        } else {
            toolbar.setVisibility(VISIBLE);
        }
        return false;
    }

    private void reset() {
        mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                id = null;
                messageTV.setText("");
                neverShowAgain.setChecked(false);
                toolbar.setVisibility(INVISIBLE);
                setVisibility(GONE);
                setScaleX(1);
                setScaleY(1);
            }
        });
    }


    interface DismissListener {
        void onDismiss(String id);
    }
}