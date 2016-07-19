package com.wally.wally.fragments;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.view.WindowManager;
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

    @SuppressWarnings("unused")
    private static final String TAG = TiltDialogFragment.class.getSimpleName();

    private View mRootView;
    private long mLastAnimationTime;
    private int mOrientation;

    private static int numSign(float f) {
        return f >= 0 ? 1 : -1;
    }

    protected void setUpTiltingDialog(View root) {
        mRootView = root;
    }

    @Override
    public void onStart() {
        super.onStart();
        mOrientation = ((WindowManager) getContext().getApplicationContext()
                .getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mRootView == null) {
            return;
        }
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
        if (System.currentTimeMillis() - mLastAnimationTime < 400) {
            return;
        }
        float x;
        float y;
        float z;
        // Note that each orientation has it's own default coordinate system
        if (mOrientation == 1) {
            x = ev.values[0];
            y = -ev.values[1];
            z = ev.values[2];
        } else {
            x = ev.values[1];
            y = ev.values[0];
            z = ev.values[2];
        }
        if (Math.abs(x) > 0.4) {
            x = numSign(x) * 50;
        }
        if (Math.abs(y) > 0.4) {
            y = numSign(y) * 30;
        }
        if (Math.abs(z) > 1.4) {
            z = numSign(z) > 0 ? 1.05f : 0.95f;
        }
        //noinspection UnusedAssignment
        z = ((z / 16f) + 1f);
        // Log.d(TAG, String.format("x - [%f] ; y - [%f] ; z - [%f]", x, y, z));
        mRootView.animate()
                .translationX(x).translationY(y)
//                .scaleX(z)
//                .scaleY(z)
                .setDuration(400)
                .setInterpolator(new AccelerateDecelerateInterpolator()).start();

        mLastAnimationTime = System.currentTimeMillis();
    }
}
