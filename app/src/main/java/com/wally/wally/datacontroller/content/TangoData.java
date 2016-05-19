package com.wally.wally.datacontroller.content;

import com.projecttango.rajawali.Pose;

import org.rajawali3d.math.Quaternion;
import org.rajawali3d.math.vector.Vector3;

import java.io.Serializable;

public class TangoData implements Serializable {
    // TODO: fill in with tango data

    private double[] rotation;
    private double[] translation;
    private double scale = 1.0;

    public TangoData() {

    }

    @SuppressWarnings("unused")
    public TangoData(double[] rotation, double[] translation) {
        this.rotation = rotation;
        this.translation = translation;
    }

    public TangoData(Pose pose) {
        this.rotation = new double[4];
        this.translation = new double[3];
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

    public double getScale() {
        return scale;
    }

    public void setScale(double scale) {
        this.scale = scale;
    }

    public TangoData withScale(double scale) {
        this.scale = scale;
        return this;
    }

    public double[] getRotation() {
        return rotation;
    }

    public TangoData withRotation(double[] rotation) {
        this.rotation = rotation;
        return this;
    }

    public double[] getTranslation() {
        return translation;
    }

    public TangoData withTranslation(double[] translation) {
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
