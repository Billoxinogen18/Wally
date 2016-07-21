package com.wally.wally.components;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.util.Pair;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.atap.tango.ux.TangoUx;
import com.google.atap.tango.ux.TangoUxLayout;
import com.wally.wally.R;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by Meravici on 5/26/2016. yea
 */
public class WallyTangoUx extends TangoUx {
    public static final int SHORT = 2000;
    public static final int LONG = 5000;


    private Handler mMainThreadHandler;
    private TextView mTextView;
    private Context mContext;
    private Queue<Pair<Integer, String>> mMessageQueue;
    private Runnable onMessageTimeout = new Runnable() {
        @Override
        public void run() {
            mTextView.setText("");
            mTextView.setVisibility(View.GONE);
            mMessageQueue.poll();
            if(!mMessageQueue.isEmpty()){
                showNextMessage();
            }
        }
    };

    public WallyTangoUx(Context context) {
        super(context);
        mContext = context;
        mMainThreadHandler = new Handler(Looper.getMainLooper());
        mMessageQueue = new ConcurrentLinkedQueue<>();

    }


    @Override
    public void setLayout(TangoUxLayout tangoUxLayout) {
        super.setLayout(tangoUxLayout);
        addTextView(tangoUxLayout);

    }

    public void showCustomMessage(final String message, int durationMs){
        mMessageQueue.add(new Pair<>(durationMs, message));

        if(mMessageQueue.isEmpty()){
            showNextMessage();
        }
    }
//
//    public void hideCustomMessage(){
//        if(mTextView.getVisibility() != View.GONE)
//            mMainThreadHandler.post(new Runnable() {
//                @Override
//                public void run() {
//                    mTextView.setVisibility(View.GONE);
//                }
//            });
//    }


    private void showNextMessage(){
        final Pair<Integer, String> pair = mMessageQueue.peek();
        if(!mTextView.getText().equals(pair.second)){
            mMainThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    mTextView.setText(pair.second);
                    mTextView.setVisibility(View.VISIBLE);
                }
            });
        }

        waitFor(pair.first);
    }

    private void waitFor(int duration){
        mMainThreadHandler.postDelayed(onMessageTimeout, duration);
    }

    private void addTextView(TangoUxLayout tangoUxLayout) {
        mTextView = new TextView(mContext);
        mTextView.setGravity(Gravity.CENTER);
        mTextView.setBackgroundResource(R.color.uxOverlayBackgroundColor);
        mTextView.setTextColor(Color.BLACK);
        mTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 34);
        mTextView.setVisibility(View.GONE);
        ViewGroup parent = (ViewGroup)tangoUxLayout.getParent();
        parent.addView(mTextView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }


    public void showCustomMessage(String message) {
        showCustomMessage(message, SHORT);
    }
}
