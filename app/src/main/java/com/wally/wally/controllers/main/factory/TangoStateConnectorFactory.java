package com.wally.wally.controllers.main.factory;


import android.util.Log;

import com.wally.wally.tango.TangoUpdater;
import com.wally.wally.tango.states.TangoForCloudAdfs;
import com.wally.wally.tango.states.TangoForLearnedAdf;
import com.wally.wally.tango.states.TangoForLearning;
import com.wally.wally.tango.states.TangoForReadyState;
import com.wally.wally.tango.states.TangoForSavedAdf;
import com.wally.wally.tango.states.TangoState;
import com.wally.wally.tango.states.TangoStateConnector;

/**
 * Created by shota on 8/15/16.
 * Factory for Edges in the state graph.
 */
public class TangoStateConnectorFactory {
    private static final String TAG = TangoStateConnector.class.getSimpleName();


    private TangoState.StateChangeListener mStateChangeListener;
    protected TangoUpdater mTangoUpdater;



    public TangoStateConnectorFactory(TangoUpdater tangoUpdater, TangoState.StateChangeListener stateChangeListener){
        mStateChangeListener = stateChangeListener;
        mTangoUpdater = tangoUpdater;
    }


    public TangoStateConnector getConnectorFromCloudAdfsToReadyState(final TangoForCloudAdfs from, final TangoForReadyState to){
        return getConnectorToReadyState(from, to);
    }

    public TangoStateConnector getConnectorFromCloudAdfsToLearning(final TangoForCloudAdfs from, final TangoForLearning to){
        return getConnectorWithPauseAndResume(from, to);
    }

    public TangoStateConnector getConnectorFromLearnedAdfToReadyState(final TangoForLearnedAdf from, final TangoForReadyState to){
        return getConnectorToReadyState(from, to);
    }

    public TangoStateConnector getConnectorFromLearnedAdfToCloudAdf(final TangoForLearnedAdf from, final TangoForCloudAdfs to){
        return getConnectorWithPauseAndResume(from, to);
    }

    public TangoStateConnector getConnectorFromLearningToLearnedAdf(final TangoForLearning from, final TangoForLearnedAdf to){
        return new TangoStateConnector() {
            @Override
            public void toNextState() {
                Log.d(TAG, "toNextState: from = " + from + " - to = " + to);
                from.pause();
                to.withAdf(from.getAdf());
                changeState(from, to);
                to.resume();
            }
        };
    }

    public TangoStateConnector getConnectorFromLearningToLearningState(final TangoForLearning from, final TangoForLearning to){
        return getConnectorToResetState(from, to);
    }


    public TangoStateConnector getConnectorFromReadyToSavedAdfState(final TangoForReadyState from, final TangoForSavedAdf to){
        return new TangoStateConnector() {
            @Override
            public void toNextState() {
                Log.d(TAG, "toNextState: from = " + from + " - to = " + to);
                to.withAdf(from.getAdf());
                changeState(from, to);
            }
        };
    }

    public TangoStateConnector getConnectorFromSavedAdfToReadyState(final TangoForSavedAdf from, final TangoForReadyState to){
        return getConnectorToReadyState(from, to);
    }

    public TangoStateConnector getConnectorFromSavedAdfCloudAdfState(final TangoForSavedAdf from, final TangoForCloudAdfs to){
        return getConnectorWithPauseAndResume(from, to);
    }



    private TangoStateConnector getConnectorToResetState(final TangoState from, final TangoState to){
        return new TangoStateConnector() {
            @Override
            public void toNextState() {
                Log.d(TAG, "toNextState: from = " + from + " - to = " + to);
                from.pause();
                to.resume();
            }
        };
    }



    private TangoStateConnector getConnectorWithPauseAndResume(final TangoState from, final TangoState to){
        return new TangoStateConnector() {
            @Override
            public void toNextState() {
                Log.d(TAG, "toNextState: from = " + from + " - to = " + to);
                from.pause();
                changeState(from, to);
                to.resume();
            }
        };
    }

    private TangoStateConnector getConnectorToReadyState(final TangoState from, final TangoForReadyState to){
        return new TangoStateConnector() {
            @Override
            public void toNextState() {
                Log.d(TAG, "toNextState: from = " + from + " - to = " + to);
                to.withPreviousState(from, from.getAdf());
                mStateChangeListener.onStateChange(to);
                to.resume();
            }
        };
    }

    private void changeState(TangoState from, TangoState to) {
        mStateChangeListener.onStateChange(to);
        mTangoUpdater.removeTangoUpdaterListener(from);
        mTangoUpdater.addTangoUpdaterListener(to);
    }
}
