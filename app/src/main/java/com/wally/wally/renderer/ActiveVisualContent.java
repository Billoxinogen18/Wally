package com.wally.wally.renderer;

import android.util.Log;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.projecttango.rajawali.ContentPlane;
import com.projecttango.rajawali.Pose;
import com.wally.wally.objects.content.Content;
import com.wally.wally.objects.content.TangoData;

import org.rajawali3d.animation.Animation3D;
import org.rajawali3d.animation.TranslateAnimation3D;
import org.rajawali3d.math.Quaternion;
import org.rajawali3d.scene.RajawaliScene;

/**
 * Created by shota on 4/29/16.
 */
public class ActiveVisualContent extends VisualContent {
    private static final String TAG = ActiveVisualContent.class.getSimpleName();
    private static final int MOVE_ANIMATION_DURATION = 400;
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

        moveAnimation(mMoveAnim, scene);

        Quaternion currRot = mVisual.getOrientation();
        Quaternion finalRot = mNewPose.getOrientation();
        mContent.getTangoData().updatePose(mNewPose);
        if (!currRot.equals(finalRot, 0.1f)) {
            Quaternion rot = Quaternion.slerpAndCreate(currRot, finalRot, 0.1);
            mVisual.setOrientation(rot);
        } else {
            mNewPose = null;
        }
    }

    // TODO change move animation
    private void moveAnimation(Animation3D anim, RajawaliScene scene) {
        if (anim != null) {
            anim.pause();
            scene.unregisterAnimation(anim);
        }
        anim = new TranslateAnimation3D(mNewPose.getPosition());
        anim.setTransformable3D(mVisual);
        anim.setDurationMilliseconds(MOVE_ANIMATION_DURATION);
        anim.setInterpolator(new AccelerateDecelerateInterpolator());
        scene.registerAnimation(anim);
        anim.play();
    }

    public void scaleContent(double byFactor) {
        TangoData tangoData = mContent.getTangoData();
        if (tangoData != null) {
            tangoData.withScale(tangoData.getScale() * byFactor);
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
        //mVisual.setTransparent(true);
        return mVisual;
    }

    @Override
    public ActiveVisualContent cloneContent() {
        ActiveVisualContent res = new ActiveVisualContent(mContent);
        res.setStatus(getStatus());
        res.setNewPose(mNewPose);
        return res;
    }
}