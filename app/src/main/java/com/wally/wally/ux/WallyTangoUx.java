package com.wally.wally.ux;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.atap.tango.ux.TangoUx;
import com.google.atap.tango.ux.TangoUxLayout;
import com.wally.wally.R;
import com.wally.wally.config.Config;
import com.wally.wally.config.TangoManagerConstants;
import com.wally.wally.events.EventListener;

/**
 * Created by Meravici on 5/26/2016. yea
 */
public class WallyTangoUx extends TangoUx implements EventListener {
    private Handler mMainThreadHandler;
    private TextView mTextView;
    private RelativeLayout mContainer;
    private Context mContext;
    private Config mConfig;
    private Runnable hideMessageRunnable;

    public WallyTangoUx(Context context, Config config) {
        super(context);
        mContext = context;
        mConfig = config;
        mMainThreadHandler = new Handler(Looper.getMainLooper());
        hideMessageRunnable = new Runnable() {
            @Override
            public void run() {
                hideMessage();
            }
        };
    }

    public void setVisible(boolean isVisible) {
        mContainer.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onTangoReady() {
        showMessage(mConfig.getString(TangoManagerConstants.LOCALIZED), 1000);
    }

    @Override
    public void onLearningStart() {
        showMessage(mConfig.getString(TangoManagerConstants.LEARNING_AREA));
    }

    @Override
    public void onLearningFinish() {
        showMessage(mConfig.getString(TangoManagerConstants.NEW_ROOM_LEARNED), 500);
    }

    @Override
    public void onLocalizationStart() {
        showMessage(mConfig.getString(TangoManagerConstants.LOCALIZING_IN_KNOWN_AREA));
    }

    @Override
    public void onLocalizationStartAfterLearning() {
        showMessage(mConfig.getString(TangoManagerConstants.LOCALIZING_IN_NEW_AREA));
    }

    @Override
    public void onLocalizationFinishAfterLearning() {
        showMessage(mConfig.getString(TangoManagerConstants.LOCALIZED), 500);
    }

    @Override
    public void onLocalizationFinishAfterSavedAdf() {
        showMessage(mConfig.getString(TangoManagerConstants.LOCALIZED));
    }

    @Override
    public void onTangoOutOfDate() {
        showTangoOutOfDate();
    }

    @Override
    public void setLayout(TangoUxLayout tangoUxLayout) {
        super.setLayout(tangoUxLayout);
        addTextView(tangoUxLayout);
    }

    private void addTextView(TangoUxLayout tangoUxLayout) {
        mContainer = new RelativeLayout(mContext);
        mContainer.setGravity(Gravity.CENTER);

        mTextView = new TextView(mContext);
        mTextView.setPadding(50, 50, 50, 50);
        mTextView.setGravity(Gravity.CENTER);
        mTextView.setBackgroundResource(R.color.uxOverlayBackgroundColor);
        mTextView.setTextColor(Color.WHITE);
        mTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 34);
        mTextView.setVisibility(View.GONE);

        RelativeLayout.LayoutParams containerParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        containerParams.addRule(RelativeLayout.CENTER_HORIZONTAL);

        mContainer.addView(mTextView, containerParams);


        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;

        tangoUxLayout.addView(mContainer, params);
    }

    private void showMessage(final String message, long time) {
        mMainThreadHandler.removeCallbacks(hideMessageRunnable);
        mMainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                mTextView.setText(message);
                mTextView.setVisibility(View.VISIBLE);
            }
        });
        mMainThreadHandler.postDelayed(hideMessageRunnable, time);
    }

    private void showMessage(final String message) {
        mMainThreadHandler.removeCallbacks(hideMessageRunnable);
        mMainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                mTextView.setText(message);
                mTextView.setVisibility(View.VISIBLE);
            }
        });
    }

    private void hideMessage() {
        if (mTextView.getVisibility() != View.GONE)
            mMainThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    mTextView.setVisibility(View.GONE);
                }
            });
    }
}