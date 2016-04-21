package com.wally.wally.tango;

import com.google.atap.tango.ux.UxExceptionEvent;
import com.google.atap.tango.ux.UxExceptionEventListener;

/**
 * Created by shota on 4/21/16.
 */
public class UxExceptionEvents implements UxExceptionEventListener {
    private static UxExceptionEvents ourInstance = new UxExceptionEvents();

    public static UxExceptionEvents getInstance() {
        return ourInstance;
    }

    private UxExceptionEvents() {
    }

    @Override
    public void onUxExceptionEvent(UxExceptionEvent uxExceptionEvent) {
        switch (uxExceptionEvent.getType()) {
            case UxExceptionEvent.TYPE_OVER_EXPOSED:
                break;
            case UxExceptionEvent.TYPE_UNDER_EXPOSED:
                break;
            case UxExceptionEvent.TYPE_MOVING_TOO_FAST:
                break;
            case UxExceptionEvent.TYPE_FEW_FEATURES:
                break;
            case UxExceptionEvent.TYPE_FEW_DEPTH_POINTS:
                break;
            case UxExceptionEvent.TYPE_LYING_ON_SURFACE:
                break;
            case UxExceptionEvent.TYPE_MOTION_TRACK_INVALID:
                break;
            case UxExceptionEvent.TYPE_TANGO_SERVICE_NOT_RESPONDING:
                break;
            case UxExceptionEvent.TYPE_INCOMPATIBLE_VM:
                break;
        }
    }
}
