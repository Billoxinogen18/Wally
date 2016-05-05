package com.wally.wally.tango;

import android.view.animation.AccelerateDecelerateInterpolator;

import com.projecttango.rajawali.Pose;
import com.wally.wally.datacontroller.content.Content;

import org.rajawali3d.animation.Animation3D;
import org.rajawali3d.animation.TranslateAnimation3D;
import org.rajawali3d.scene.RajawaliScene;

/**
 * Created by shota on 4/29/16.
 */
public class ActiveVisualContent extends VisualContent {
    private Animation3D mMoveAnim = null;
    private Pose mNewPose;

    public ActiveVisualContent(Content content, Pose pose) {
        super(content);
        setPosition(pose.getPosition());
        setRotation(pose.getOrientation());
        mNewPose = null;
    }

    @Override
    protected void setVisualContentScale() {
    }

    public boolean shouldAnimate(){
        return mNewPose != null;
    }


    public void setNewPose(Pose newPose) {
        mNewPose = newPose;
    }

    public void animate(RajawaliScene scene) {
        if (mMoveAnim != null) {

            mMoveAnim.pause();
            scene.unregisterAnimation(mMoveAnim);
            mMoveAnim = null;
        }

        mMoveAnim = new TranslateAnimation3D(mNewPose.getPosition());
        mMoveAnim.setTransformable3D(this);
        mMoveAnim.setDurationMilliseconds(500);
        mMoveAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        scene.registerAnimation(mMoveAnim);
        mMoveAnim.play();

        // TODO make rotate animation too
        setOrientation(mNewPose.getOrientation());
        mNewPose = null;
    }

    public void scaleContent(double byFactor){
        setScale(getScale().x * byFactor);
    }
}