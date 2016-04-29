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
public class ActiveVisualContent2 extends VisualContent2 {
    private Animation3D mMoveAnim = null;
    private Pose mNewPose;
    private boolean isNotYetAddedOnTheScene;

    public ActiveVisualContent2(Content content) {
        super(content);
        isNotYetAddedOnTheScene = true;
        mNewPose = null;
    }

    public boolean isNotYetAddedOnTheScene() {
        return isNotYetAddedOnTheScene;
    }

    public void setIsNotYetAddedOnTheScene(boolean isNotYetAddedOnTheScene) {
        this.isNotYetAddedOnTheScene = isNotYetAddedOnTheScene;
    }

    public Pose getNewPose() {
        return mNewPose;
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
}