package com.wally.wally.fragments;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

/**
 * Abstract tilting dialog.
 * <p/>
 * This is dialog that is tilting when user tilts it's device.
 * <p/>
 * It gives pleasant floating effect to dialog.
 * Created by ioane5 on 7/15/16.
 */
public abstract class TiltDialogFragment extends DialogFragment implements SensorEventListener {

    private static final String TAG = TiltDialogFragment.class.getSimpleName();

    private View mRootView;
    private long mLastAnimationTime;

    private static int numSign(float f) {
        return f >= 0 ? 1 : -1;
    }

    protected void setUpTiltingDialog(View root) {
        mRootView = root;
    }

    @Override
    public void onResume() {
        super.onResume();

        SensorManager sm = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        Sensor la = sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        sm.registerListener(this, la, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    public void onPause() {
        super.onPause();

        SensorManager sm = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        sm.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void onSensorChanged(SensorEvent ev) {
        if (System.currentTimeMillis() - mLastAnimationTime < 250) {
            return;
        }
        float x = ev.values[0];
        float y = -ev.values[1];
        float z = ((ev.values[2] / 16f) + 1f);

        if (Math.abs(x) > 0.3) {
            x = numSign(x) * 50;
        }
        if (Math.abs(y) > 0.3) {
            y = numSign(y) * 30;
        }
        if (Math.abs(z) > 1.3) {
            z = numSign(z) > 0 ? 1.05f : 0.95f;
        }
        Log.d(TAG, String.format("x - [%f] ; y - [%f] ; z - [%f]", x, y, z));
        mRootView.clearAnimation();
        mRootView.animate()
                .x(x).y(y)
                .scaleX(z)
                .scaleY(z)
                .setDuration(300)
                .setInterpolator(new AccelerateDecelerateInterpolator()).start();

        mLastAnimationTime = System.currentTimeMillis();
    }
}
