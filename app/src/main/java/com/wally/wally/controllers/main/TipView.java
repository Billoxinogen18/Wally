package com.wally.wally.controllers.main;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wally.wally.R;
import com.wally.wally.Utils;

/**
 * Created by Meravici on 8/4/2016. yea
 */
public class TipView extends LinearLayout {
    private static final int BACKGROUND_COLOR = Color.GRAY;
    private static final int PADDING_DP = 8;

    private TextView titleTV;
    private TextView messageTV;
//    private ImageView iconIV;

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

        setBackgroundColor(BACKGROUND_COLOR);
        setPadding(
                Utils.dpToPx(getContext(), PADDING_DP),
                Utils.dpToPx(getContext(), PADDING_DP),
                Utils.dpToPx(getContext(), PADDING_DP),
                Utils.dpToPx(getContext(), PADDING_DP)
        );
        setVisibility(GONE);

        this.titleTV = (TextView) findViewById(R.id.title);
        this.messageTV = (TextView) findViewById(R.id.message);
//        this.iconIV = (ImageView) findViewById(R.id.icon);

        mainThreadHandler = new Handler(Looper.getMainLooper());

        hideRunnable = new Runnable() {
            @Override
            public void run() {
                setVisibility(GONE);
            }
        };
    }

    public void show(final String title, final String message, int durationMs) {
        mainThreadHandler.removeCallbacks(hideRunnable);

        mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                if(title != null && !title.isEmpty()){
                    titleTV.setText(title);
                }else{
                    titleTV.setVisibility(GONE);
                }

                if(message != null && !message.isEmpty()){
                    messageTV.setText(message);
                }else{
                    messageTV.setVisibility(GONE);
                }

                setVisibility(VISIBLE);
            }
        });

        if(durationMs > 0){
            mainThreadHandler.postDelayed(hideRunnable, durationMs);
        }
    }
}