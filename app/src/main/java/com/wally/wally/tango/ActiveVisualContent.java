package com.wally.wally.tango;

import android.util.Log;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.projecttango.rajawali.ContentPlane;
import com.projecttango.rajawali.Pose;
import com.wally.wally.datacontroller.content.Content;
import com.wally.wally.datacontroller.content.TangoData;

import org.rajawali3d.animation.Animation3D;
import org.rajawali3d.animation.TranslateAnimation3D;
import org.rajawali3d.scene.RajawaliScene;

/**
 * Created by shota on 4/29/16.
 */
public class ActiveVisualContent extends VisualContent {
    private static final String TAG = ActiveVisualContent.class.getSimpleName();
    private Animation3D mMoveAnim = null;
    private Pose mNewPose;

    public ActiveVisualContent(Content content) {
        super(content);
    }

    public boolean shouldAnimate() {
        return mNewPose != null;
    }


    public void setNewPose(Pose newPose) {
        mNewPose = newPose;
    }

    public void animate(RajawaliScene scene) {
        if (mNewPose == null || mVisual == null)
            return;
        if (mMoveAnim != null) {

            mMoveAnim.pause();
            scene.unregisterAnimation(mMoveAnim);
            mMoveAnim = null;
        }

        mMoveAnim = new TranslateAnimation3D(mNewPose.getPosition());
        mMoveAnim.setTransformable3D(mVisual);
        mMoveAnim.setDurationMilliseconds(500);
        mMoveAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        scene.registerAnimation(mMoveAnim);
        mMoveAnim.play();

        // TODO make rotate animation too
        mVisual.setOrientation(mNewPose.getOrientation());
        mContent.getTangoData().updatePose(mNewPose);
        mNewPose = null;
    }

    public void scaleContent(double byFactor) {
        TangoData tangoData = mContent.getTangoData();
        if (tangoData != null) {
            tangoData.setScale(tangoData.getScale() * byFactor);
            refreshVisualScale();
        } else {
            Log.e(TAG, "scaleContent: tangoData was null");
        }
    }

    @Override
    public ContentPlane getVisual() {
        super.getVisual();
        Pose currentPose = mContent.getTangoData().getPose();
        mVisual.setPosition(currentPose.getPosition());
        mVisual.setRotation(currentPose.getOrientation());
        return mVisual;
    }
}