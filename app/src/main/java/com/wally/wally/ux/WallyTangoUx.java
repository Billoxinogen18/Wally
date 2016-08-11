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
import com.wally.wally.tango.EventListener;

/**
 * Created by Meravici on 5/26/2016. yea
 */
public class WallyTangoUx extends TangoUx implements EventListener {
    private Handler mMainThreadHandler;
    private TextView mTextView;
    private RelativeLayout mContainer;
    private Context mContext;

    private Runnable hideMessageRunnable;

    public WallyTangoUx(Context context) {
        super(context);
        mContext = context;
        mMainThreadHandler = new Handler(Looper.getMainLooper());
        hideMessageRunnable = new Runnable() {
            @Override
            public void run() {
                hideMessage();
            }
        };
    }

    public void setVisible(boolean isVisible){
        mContainer.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onTangoReady() {

    }

    @Override
    public void onLearningStart() {
        showMessage("Learning start", 5000);
    }

    @Override
    public void onLearningFinish() {
        showMessage("Learning finish", 5000);
    }

    @Override
    public void onLocalizationStart() {

    }

    @Override
    public void onLocalizationStartAfterLearning() {

    }

    @Override
    public void onLocalizationFinishAfterLearning() {

    }

    @Override
    public void onLocalizationFinishAfterSavedAdf() {

    }

    @Override
    public void onTangoOutOfDate() {

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
        mTextView.setPadding(50,50,50,50);
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

    private void showMessage(final String message, long time){
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

    private void showMessage(final String message){
        mMainThreadHandler.removeCallbacks(hideMessageRunnable);
        mMainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                mTextView.setText(message);
                mTextView.setVisibility(View.VISIBLE);
            }
        });
    }

    private void hideMessage(){
        if(mTextView.getVisibility() != View.GONE)
            mMainThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    mTextView.setVisibility(View.GONE);
                }
            });
    }
}