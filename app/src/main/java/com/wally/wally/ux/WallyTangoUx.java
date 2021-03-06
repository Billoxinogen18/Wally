package com.wally.wally.ux;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.atap.tango.ux.TangoUx;
import com.google.atap.tango.ux.TangoUxLayout;
import com.google.atap.tango.ux.UxExceptionEvent;
import com.google.atap.tango.ux.UxExceptionEventListener;
import com.wally.wally.R;
import com.wally.wally.config.Config;
import com.wally.wally.config.TangoManagerConstants;
import com.wally.wally.events.WallyEvent;
import com.wally.wally.events.WallyEventListener;

public class WallyTangoUx extends TangoUx implements WallyEventListener {
    private static final String TAG = WallyTangoUx.class.getSimpleName();

    private Handler mMainThreadHandler;

    private TextView mTextView;
    private RelativeLayout mContainer;
    private RelativeLayout mOverlay;

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
        setUxExceptionEventListener(mUxExceptionListener);
    }

    public void setVisible(boolean isVisible) {
        mContainer.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    //    @Override
    public void onTangoReady() {
        showMessage(mConfig.getString(TangoManagerConstants.LOCALIZED), 1000);
    }

    //    @Override
    public void onLearningStart() {
        showMessage(mConfig.getString(TangoManagerConstants.LEARNING_AREA));
    }

    //    @Override
    public void onLearningFinish() {
        showMessage(mConfig.getString(TangoManagerConstants.NEW_ROOM_LEARNED), 500);
    }

    //    @Override
    public void onLocalizationStart() {
        showMessage(mConfig.getString(TangoManagerConstants.LOCALIZING_IN_KNOWN_AREA));
    }

    //    @Override
    public void onLocalizationStartAfterLearning() {
        showMessage(mConfig.getString(TangoManagerConstants.LOCALIZING_IN_NEW_AREA));
    }

    //    @Override
    public void onLocalizationFinishAfterLearning() {
        showMessage(mConfig.getString(TangoManagerConstants.LOCALIZED), 500);
    }

    public void onLocalizationFinishAfterSavedAdf() {
        showMessage(mConfig.getString(TangoManagerConstants.LOCALIZED), 500);
    }

    public void onLocalizationLost() {
        showMessage(mConfig.getString(TangoManagerConstants.LOCALIZATION_LOST), 500);
    }

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

        mOverlay = new RelativeLayout(mContext);
        mOverlay.setBackgroundColor(Color.WHITE);
        mOverlay.setGravity(Gravity.CENTER);

        RelativeLayout.LayoutParams overlayParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mContainer.addView(mOverlay, overlayParams);

        mTextView = new TextView(mContext);
        mTextView.setPadding(50, 50, 50, 50);
        mTextView.setGravity(Gravity.CENTER);
        mTextView.setBackgroundResource(R.color.uxOverlayBackgroundColor);
        mTextView.setTextColor(Color.WHITE);
        mTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 34);
        mTextView.setVisibility(View.GONE);

        RelativeLayout.LayoutParams containerParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        containerParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        containerParams.addRule(RelativeLayout.CENTER_VERTICAL);

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

    @Override
    public void onWallyEvent(WallyEvent event) {
        switch (event.getId()) {
            case WallyEvent.TANGO_READY:
                onTangoReady();
                break;
            case WallyEvent.LEARNING_START:
                onLearningStart();
                break;
            case WallyEvent.LEARNING_FINISH:
                onLearningFinish();
                break;
            case WallyEvent.TANGO_OUT_OF_DATE:
                onTangoOutOfDate();
                break;
            case WallyEvent.LOCALIZATION_START_ON_CLOUD_ADF:
            case WallyEvent.LOCALIZATION_START:
                onLocalizationStart();
                break;
            case WallyEvent.LOCALIZATION_START_AFTER_LEARNING:
                onLocalizationStartAfterLearning();
                break;
            case WallyEvent.LOCALIZATION_FINISH_AFTER_LEARNING:
                onLocalizationFinishAfterLearning();
                break;
            case WallyEvent.LOCALIZATION_FINISH_AFTER_CLOUD_ADF:
            case WallyEvent.LOCALIZATION_FINISH_AFTER_SAVED_ADF:
                onLocalizationFinishAfterSavedAdf();
                break;
            case WallyEvent.ON_LOCALIZATION_LOST:
                onLocalizationLost();
                break;
            case WallyEvent.ON_PAUSE:
                super.stop();
                mMainThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mOverlay.setVisibility(View.VISIBLE);
                    }
                });
                break;
            case WallyEvent.ON_RESUME:
                StartParams startParams = new StartParams();
                startParams.showConnectionScreen = false;
                super.start(startParams);
                break;
            default:
                break;
        }
    }

    public void hideOverlay() {
        mMainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                mOverlay.setVisibility(View.GONE);
            }
        });
    }


    private UxExceptionEventListener mUxExceptionListener = new UxExceptionEventListener() {

        @Override
        public void onUxExceptionEvent(UxExceptionEvent uxExceptionEvent) {
            if (uxExceptionEvent.getType() == UxExceptionEvent.TYPE_LYING_ON_SURFACE) {
                Log.i(TAG, "Device lying on surface ");
            }
            if (uxExceptionEvent.getType() == UxExceptionEvent.TYPE_FEW_DEPTH_POINTS) {
                Log.i(TAG, "Very few depth points in mPoint cloud ");
            }
            if (uxExceptionEvent.getType() == UxExceptionEvent.TYPE_FEW_FEATURES) {
                Log.i(TAG, "Invalid poses in MotionTracking ");
            }
            if (uxExceptionEvent.getType() == UxExceptionEvent.TYPE_INCOMPATIBLE_VM) {
                Log.i(TAG, "Device not running on ART");
            }
            if (uxExceptionEvent.getType() == UxExceptionEvent.TYPE_MOTION_TRACK_INVALID) {
                Log.i(TAG, "Invalid poses in MotionTracking ");
            }
            if (uxExceptionEvent.getType() == UxExceptionEvent.TYPE_MOVING_TOO_FAST) {
                Log.i(TAG, "Invalid poses in MotionTracking ");
            }
            if (uxExceptionEvent.getType() == UxExceptionEvent.TYPE_OVER_EXPOSED) {
                Log.i(TAG, "Camera Over Exposed");
            }
            if (uxExceptionEvent.getType() == UxExceptionEvent.TYPE_TANGO_SERVICE_NOT_RESPONDING) {
                Log.i(TAG, "TangoService is not responding ");
            }
            if (uxExceptionEvent.getType() == UxExceptionEvent.TYPE_UNDER_EXPOSED) {
                Log.i(TAG, "Camera Under Exposed ");
            }

        }
    };
}