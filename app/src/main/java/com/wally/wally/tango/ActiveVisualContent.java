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
    private Pose mCurrentPose;
    private Pose mNewPose;

    public ActiveVisualContent(Content content, Pose pose) {
        super(content);
        mCurrentPose = pose;
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
        mMoveAnim.setTransformable3D(mVisual);
        mMoveAnim.setDurationMilliseconds(500);
        mMoveAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        scene.registerAnimation(mMoveAnim);
        mMoveAnim.play();

        // TODO make rotate animation too
        mVisual.setOrientation(mNewPose.getOrientation());
        mCurrentPose = mNewPose;
        mNewPose = null;
    }

    public void scaleContent(double byFactor){
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
        mVisual.setPosition(mCurrentPose.getPosition());
        mVisual.setRotation(mCurrentPose.getOrientation());
        return mVisual;
    }
}