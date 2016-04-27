package com.wally.wally.tango;

import android.graphics.Bitmap;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.projecttango.rajawali.Pose;
import com.wally.wally.datacontroller.content.Content;

import org.rajawali3d.Object3D;
import org.rajawali3d.animation.Animation;
import org.rajawali3d.animation.Animation3D;
import org.rajawali3d.animation.ScaleAnimation3D;
import org.rajawali3d.animation.TranslateAnimation3D;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.scene.RajawaliScene;

/**
 * Created by shota on 4/27/16.
 */
public class ActiveVisualContent extends VisualContent {
    private Animation3D mMoveAnim = null;
    private Animation3D mHighlightAnimation;
    private Animation3D mRotateAnim = null;
    private Pose mNewPose;
    private boolean isNotYetAddedOnTheScene;

    public ActiveVisualContent(Bitmap bitmap, Pose contentPose, Content content) {
        super(bitmap, contentPose, content);
        isNotYetAddedOnTheScene = true;
        mNewPose = null;
    }

    public boolean isNotYetAddedOnTheScene() {
        return isNotYetAddedOnTheScene;
    }

    public void setIsNotYetAddedOnTheScene(boolean isNotYetAddedOnTheScene) {
        this.isNotYetAddedOnTheScene = isNotYetAddedOnTheScene;
    }

    public void setNewPost(Pose newPose) {
        mNewPose = newPose;
    }

    public Pose getNewPose() {
        return mNewPose;
    }

    public void animate(RajawaliScene scene) {
        if (mMoveAnim != null) {
            mMoveAnim.pause();
            scene.unregisterAnimation(mMoveAnim);
            mMoveAnim = null;
        }
        if (mRotateAnim != null) {
            mRotateAnim.pause();
            scene.unregisterAnimation(mRotateAnim);
            mRotateAnim = null;
        }

        mMoveAnim = new TranslateAnimation3D(mNewPose.getPosition());
//            mRotateAnim = new RotateAnimation3D(mNewPose.getOrientation().x, mNewPose.getOrientation().y, mNewPose.getOrientation().z);

        mMoveAnim.setTransformable3D(mContent3D);
//            mRotateAnim.setTransformable3D(mContent3D);

        mMoveAnim.setDurationMilliseconds(500);
//            mRotateAnim.setDurationMilliseconds(500);

        mMoveAnim.setInterpolator(new AccelerateDecelerateInterpolator());
//            mRotateAnim.setInterpolator(new AccelerateDecelerateInterpolator());

        scene.registerAnimation(mMoveAnim);
//            scene.registerAnimation(mRotateAnim);

        mMoveAnim.play();
//            mRotateAnim.play();

        if (mHighlightAnimation == null) {
            mHighlightAnimation = new ScaleAnimation3D(new Vector3(1.1, 1.1, 1.1));
            mHighlightAnimation.setRepeatMode(Animation.RepeatMode.REVERSE_INFINITE);
            mHighlightAnimation.setDurationMilliseconds(400);
            mHighlightAnimation.setDelayMilliseconds(1200);
            mHighlightAnimation.setTransformable3D(mContent3D);
            mHighlightAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
            scene.registerAnimation(mHighlightAnimation);
            mHighlightAnimation.play();
        }
        // TODO make this with animation too
        mContent3D.setOrientation(mNewPose.getOrientation());


        mContentPose = mNewPose;
        mNewPose = null;
    }
}