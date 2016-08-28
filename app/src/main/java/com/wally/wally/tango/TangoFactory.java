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
        TangoSupport.initialize();
    }

    public Tango getTangoForLearning(final RunnableWithError r) {
        mTango = new Tango(mContext, new Runnable() {
            @Override
            public void run() {
                try {
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

    private void connectTangoWithUuid(Tango mTango, String uuid) {
        TangoConfig config = getBasicConfig(mTango);
        config.putString(TangoConfig.KEY_STRING_AREADESCRIPTION, uuid);
        mTango.connect(config);
    }

    private void connectTango(Tango mTango) {
        TangoConfig config = getBasicConfig(mTango);
        // config.putBoolean(TangoConfig.KEY_BOOLEAN_DRIFT_CORRECTION, true);
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
        return config;
    }


    public interface RunnableWithError {
        void run();

        void onError(Exception e);
    }
}
