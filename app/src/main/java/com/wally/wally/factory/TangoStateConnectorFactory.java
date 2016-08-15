package com.wally.wally.factory;

import com.wally.wally.tango.states.TangoStateConnector;

/**
 * Created by shota on 8/15/16.
 */
public class TangoStateConnectorFactory {
    public TangoStateConnector getConnectorFromCloudAdfsToReadyState(){
        return new TangoStateConnector() {
            @Override
            public void toNextState() {

            }
        };
    }
}
