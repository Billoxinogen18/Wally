package com.wally.wally.tango;

import com.google.atap.tangoservice.Tango;
import com.google.atap.tangoservice.TangoCoordinateFramePair;
import com.google.atap.tangoservice.TangoPoseData;
import com.projecttango.rajawali.DeviceExtrinsics;


public class TangoUtils {
    public static boolean isAdfImported(Tango tango, String uuid) {
        if (uuid == null) return false;
        for (String id : tango.listAreaDescriptions()) {
            if (id.equals(uuid)) return true;
        }
        return false;
    }

    /**
     * Calculates and stores the fixed transformations between the device and
     * the various sensors to be used later for transformations between frames.
     */
    public static DeviceExtrinsics getDeviceExtrinsics(Tango tango) {
        // Create camera to IMU transform.
        TangoCoordinateFramePair framePair = new TangoCoordinateFramePair();
        framePair.baseFrame = TangoPoseData.COORDINATE_FRAME_IMU;
        framePair.targetFrame = TangoPoseData.COORDINATE_FRAME_CAMERA_COLOR;
        TangoPoseData rgbPose = tango.getPoseAtTime(0.0, framePair);

        // Create device to IMU transform.
        framePair.targetFrame = TangoPoseData.COORDINATE_FRAME_DEVICE;
        TangoPoseData devicePose = tango.getPoseAtTime(0.0, framePair);

        // Create depth camera to IMU transform.
        framePair.targetFrame = TangoPoseData.COORDINATE_FRAME_CAMERA_DEPTH;
        TangoPoseData depthPose = tango.getPoseAtTime(0.0, framePair);

        return new DeviceExtrinsics(devicePose, rgbPose, depthPose);
    }
}
