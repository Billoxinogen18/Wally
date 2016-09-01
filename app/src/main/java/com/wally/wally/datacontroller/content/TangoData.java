package com.wally.wally.datacontroller.content;

import com.projecttango.rajawali.Pose;

import org.rajawali3d.math.Quaternion;
import org.rajawali3d.math.vector.Vector3;

import java.io.Serializable;

public class TangoData implements Serializable {
    private Double scale = 1.0;
    private Double[] rotation;
    private Double[] translation;

    public TangoData() {
    }

    public TangoData(Pose pose) {
        this.rotation = new Double[4];
        this.translation = new Double[3];
        updatePose(pose);
    }

    public void updatePose(Pose pose) {
        this.rotation[0] = pose.getOrientation().w;
        this.rotation[1] = pose.getOrientation().x;
        this.rotation[2] = pose.getOrientation().y;
        this.rotation[3] = pose.getOrientation().z;

        this.translation[0] = pose.getPosition().x;
        this.translation[1] = pose.getPosition().y;
        this.translation[2] = pose.getPosition().z;
    }

    public Double getScale() {
        return scale;
    }

    public TangoData withScale(Double scale) {
        this.scale = scale;
        return this;
    }

    public Double[] getRotation() {
        return rotation;
    }

    public TangoData withRotation(Double[] rotation) {
        this.rotation = rotation;
        return this;
    }

    public Double[] getTranslation() {
        return translation;
    }

    public TangoData withTranslation(Double[] translation) {
        this.translation = translation;
        return this;
    }

    public Pose getPose() {
        Pose result;
        Vector3 v = new Vector3(translation[0], translation[1], translation[2]);
        Quaternion q = new Quaternion(rotation[0], rotation[1], rotation[2], rotation[3]);
        result = new Pose(v, q);
        return result;
    }
}