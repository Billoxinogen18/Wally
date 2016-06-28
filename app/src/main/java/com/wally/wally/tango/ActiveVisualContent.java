package com.wally.wally.tango;

import android.util.Log;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.projecttango.rajawali.ContentPlane;
import com.projecttango.rajawali.Pose;
import com.wally.wally.datacontroller.content.Content;
import com.wally.wally.datacontroller.content.TangoData;

import org.rajawali3d.animation.Animation3D;
import org.rajawali3d.animation.EllipticalOrbitAnimation3D;
import org.rajawali3d.animation.SlerpAnimation3D;
import org.rajawali3d.animation.TranslateAnimation3D;
import org.rajawali3d.scene.RajawaliScene;

/**
 * Created by shota on 4/29/16.
 */
public class ActiveVisualContent extends VisualContent {
    private static final String TAG = ActiveVisualContent.class.getSimpleName();
    private static final int MOVE_ANUMATION_DURATION = 300;
    private Animation3D mMoveAnim = null;
    private SlerpAnimation3D mRotationAnim = null;
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

        // TODO make rotate animation too

        mVisual.setOrientation(mNewPose.getOrientation());
        mContent.getTangoData().updatePose(mNewPose);
        mNewPose = null;
    }

    private void moveAnimation(Animation3D anim, RajawaliScene scene){
        if (anim != null) {
            anim.pause();
            scene.unregisterAnimation(mMoveAnim);
            anim = null;
        }
        anim = new TranslateAnimation3D(mNewPose.getPosition());
        anim.setTransformable3D(mVisual);
        anim.setDurationMilliseconds(MOVE_ANUMATION_DURATION);
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
        return mVisual;
    }

    @Override
    public ActiveVisualContent cloneContent(){
        ActiveVisualContent res = new ActiveVisualContent(mContent);
        res.setStatus(getStatus());
        res.setNewPose(mNewPose);
        return res;
    }
}