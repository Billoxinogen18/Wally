package com.wally.wally.tango;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.atap.tango.ux.TangoUx;
import com.google.atap.tango.ux.TangoUxLayout;
import com.wally.wally.R;

/**
 * Created by Meravici on 5/26/2016. yea
 */
public class WallyTangoUx extends TangoUx {
    private Handler mMainThreadHandler;
    private TextView mTextView;
    private Context mContext;

    private Runnable hideMessageRunnable;

    public WallyTangoUx(Context context) {
        super(context);
        mContext = context;
        mMainThreadHandler = new Handler(Looper.getMainLooper());
        hideMessageRunnable = new Runnable() {
            @Override
            public void run() {
                hideCustomMessage();
            }
        };
    }


    @Override
    public void setLayout(TangoUxLayout tangoUxLayout) {
        super.setLayout(tangoUxLayout);
        addTextView(tangoUxLayout);

    }

    public void showCustomMessage(final String message, long time){
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

    public void showCustomMessage(final String message){
        mMainThreadHandler.removeCallbacks(hideMessageRunnable);
        mMainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                mTextView.setText(message);
                mTextView.setVisibility(View.VISIBLE);
            }
        });
    }

    public void hideCustomMessage(){
        if(mTextView.getVisibility() != View.GONE)
            mMainThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    mTextView.setVisibility(View.GONE);
                }
            });
    }

    private void addTextView(TangoUxLayout tangoUxLayout) {
        mTextView = new TextView(mContext);
        mTextView.setPadding(50,50,50,50);
        mTextView.setGravity(Gravity.CENTER);
        mTextView.setBackgroundResource(R.color.uxOverlayBackgroundColor);
        mTextView.setTextColor(Color.WHITE);
        mTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 34);
        mTextView.setVisibility(View.GONE);


        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;

        tangoUxLayout.addView(mTextView, params);
    }


}