package com.wally.wally.tango;

import android.content.Context;
import android.util.Log;

import com.google.atap.tangoservice.Tango;
import com.google.atap.tangoservice.TangoConfig;
import com.google.atap.tangoservice.TangoErrorException;
import com.projecttango.tangosupport.TangoSupport;

public class TangoFactory {
    private static final String TAG = TangoFactory.class.getSimpleName();

    private Context mContext;
    private Tango mTango;

    public TangoFactory(Context context) {
        mContext = context;
    }

    public Tango getTangoForLearning(final RunnableWithError r) {
        mTango = new Tango(mContext, new Runnable() {
            @Override
            public void run() {
                try {
                    TangoSupport.initialize();
                    connectTangoForLearning(mTango);
                    r.run();
                    Log.d(TAG + "Success", "for learning");
                } catch (TangoErrorException e) {
                    Log.e(TAG + "Fail", "for learning");
                    r.onError(e);
                }
            }
        });
        return mTango;
    }

    public Tango getTangoForCloudAdf(final RunnableWithError r, final String path) {
        mTango = new Tango(mContext, new Runnable() {
            @Override
            public void run() {
                try {
                    TangoSupport.initialize();
                    connectTango(mTango);
                    r.run();
                    mTango.experimentalLoadAreaDescriptionFromFile(path);
                    Log.d(TAG + "Success", "for cloud adf");
                } catch (TangoErrorException e) {
                    Log.e(TAG + "Fail", "for cloud adf");
                    r.onError(e);
                }
            }
        });
        return mTango;
    }

    public Tango getTangoForLocalAdf(final RunnableWithError r, final String uuid) {
        mTango = new Tango(mContext, new Runnable() {
            @Override
            public void run() {
                try {
                    TangoSupport.initialize();
                    connectTango(mTango);
                    r.run();
                    mTango.experimentalLoadAreaDescription(uuid);
                    Log.d(TAG + "Success", "for local adf");
                } catch (TangoErrorException e) {
                    Log.e(TAG + "Fail", "for local adf");
                    r.onError(e);
                }
            }
        });
        return mTango;
    }

    public Tango getTangoWithUuid(final RunnableWithError r, final String uuid) {
        mTango = new Tango(mContext, new Runnable() {
            @Override
            public void run() {
                try {
                    TangoSupport.initialize();
                    connectTangoWithUuid(mTango, uuid);
                    r.run();
                    Log.d(TAG + "Success", "with uuid");
                } catch (TangoErrorException e) {
                    Log.e(TAG + "Fail", "with uuid");
                    r.onError(e);
                }
            }
        });
        return mTango;
    }

    @SuppressWarnings("unused")
    public Tango getTangoForDriftCorrection(final RunnableWithError r){
        mTango = new Tango(mContext, new Runnable() {
            @Override
            public void run() {
                try {
                    TangoSupport.initialize();
                    getDriftCorrectionConfig(mTango);
                    r.run();
                    Log.d(TAG + "Success", "for learning");
                } catch (TangoErrorException e) {
                    Log.e(TAG + "Fail", "for learning");
                    r.onError(e);
                }
            }
        });
        return mTango;
    }

    private void connectTangoWithUuid(Tango mTango, String uuid) {
        TangoConfig config = getBasicConfig(mTango);
        config.putString(TangoConfig.KEY_STRING_AREADESCRIPTION, uuid);
        mTango.connect(config);
    }

    private void connectTango(Tango mTango) {
        TangoConfig config = getBasicConfig(mTango);
        mTango.connect(config);
    }

    private void connectTangoForLearning(Tango mTango) {
        TangoConfig config = getBasicConfig(mTango);
        config.putBoolean(TangoConfig.KEY_BOOLEAN_LEARNINGMODE, true);
        mTango.connect(config);
    }

    private TangoConfig getBasicConfig(Tango mTango) {
        TangoConfig config = mTango.getConfig(TangoConfig.CONFIG_TYPE_DEFAULT);
        config.putBoolean(TangoConfig.KEY_BOOLEAN_DEPTH, true);
        config.putBoolean(TangoConfig.KEY_BOOLEAN_COLORCAMERA, true);
        config.putBoolean(TangoConfig.KEY_BOOLEAN_AUTORECOVERY, true);
        config.putBoolean(TangoConfig.KEY_BOOLEAN_LOWLATENCYIMUINTEGRATION, true);
        config.putInt(TangoConfig.KEY_INT_DEPTH_MODE, TangoConfig.TANGO_DEPTH_MODE_POINT_CLOUD);
        return config;
    }

    private TangoConfig getDriftCorrectionConfig(Tango mTango){
        // Use default configuration for Tango Service, plus low latency
        // IMU integration.
        TangoConfig config = mTango.getConfig(TangoConfig.CONFIG_TYPE_DEFAULT);
        // NOTE: Low latency integration is necessary to achieve a precise alignment of
        // virtual objects with the RBG image and produce a good AR effect.
        config.putBoolean(TangoConfig.KEY_BOOLEAN_LOWLATENCYIMUINTEGRATION, true);
        config.putBoolean(TangoConfig.KEY_BOOLEAN_DEPTH, true);
        config.putInt(TangoConfig.KEY_INT_DEPTH_MODE, TangoConfig.TANGO_DEPTH_MODE_POINT_CLOUD);
        config.putBoolean(TangoConfig.KEY_BOOLEAN_COLORCAMERA, true);

        // Drift correction allows motion tracking to recover after it loses tracking.
        //
        // The drift corrected pose is is available through the frame pair with
        // base frame AREA_DESCRIPTION and target frame DEVICE.
        config.putBoolean(TangoConfig.KEY_BOOLEAN_DRIFT_CORRECTION, true);

        return config;
    }


    public interface RunnableWithError {
        void run();

        void onError(Exception e);
    }
}
