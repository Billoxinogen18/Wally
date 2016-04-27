package com.wally.wally.tango;

import android.graphics.Bitmap;
import android.util.Log;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.projecttango.rajawali.ContentPlane;
import com.projecttango.rajawali.Pose;
import com.wally.wally.datacontroller.content.Content;

import org.rajawali3d.Object3D;
import org.rajawali3d.animation.Animation;
import org.rajawali3d.animation.Animation3D;
import org.rajawali3d.animation.ScaleAnimation3D;
import org.rajawali3d.animation.TranslateAnimation3D;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.methods.DiffuseMethod;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.scene.RajawaliScene;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by shota on 4/25/16.
 */
public class VisualContentManager {
    private static final String TAG = VisualContentManager.class.getSimpleName();

    private List<VisualContent> staticContent = null;
    private ActiveVisualContent activeContent = null;
    private boolean mStaticContentShouldBeRendered = false;

    public VisualContentManager() {
        staticContent = new ArrayList<>();
    }

    public boolean isContentBeingAdded() {
        return activeContent != null;
    }

    public boolean getStaticContentShouldBeRendered() {
        return mStaticContentShouldBeRendered;
    }

    public void activeContentAddingFinished() {
        addStaticContent(activeContent);
        activeContent = null;
    }

    public boolean hasStaticContent() {
        return staticContent != null && staticContent.size() > 0;
    }

    public synchronized void addStaticContent(VisualContent visualContent) {
        staticContent.add(visualContent);
        mStaticContentShouldBeRendered = true;
    }

    public synchronized List<VisualContent> getStaticContent() {
        return staticContent;
    }

    public synchronized ActiveVisualContent getActiveContent() {
        return activeContent;
    }

    public synchronized void setActiveContent(ActiveVisualContent activeContent) {
        this.activeContent = activeContent;
    }

    public void scaleActiveContent(float scaleFactor) {
        if(activeContent != null) {
            activeContent.getObject3D().setScale(activeContent.getObject3D().getScale().x * scaleFactor);
        }
    }

    /**
     * Search in static visual contents with object and get content object.
     *
     * @param object visual object3D
     * @return visual content
     */
    public VisualContent findContentByObject3D(Object3D object) {
        // TODO make with hashmap to get better performance
        for (VisualContent vc : staticContent) {
            if (vc.getObject3D().equals(object)) {
                return vc;
            }
        }
        return null;
    }

    public static class VisualContent {
        protected Content mContent;
        protected Object3D mContent3D = null;
        protected Pose mContentPose;
        private Bitmap mBitmap;

        public VisualContent(Bitmap bitmap, Pose contentPose, Content content) {
            this.mBitmap = bitmap;
            this.mContentPose = contentPose;
            mContent = content;
        }

        public boolean isNotRendered() {
            return mContent3D == null;
        }

        public Content getContent() {
            return mContent;
        }

        public Object3D getObject3D() {
            if (mContent3D != null) return mContent3D;
            Material material = new Material();
            try {
                Texture t = new Texture("mContent3D", mBitmap);
                material.addTexture(t);
            } catch (ATexture.TextureException e) {
                Log.e(TAG, "Exception generating mContent3D texture", e);
            }
            material.setColorInfluence(0);
            material.enableLighting(true);
            material.setDiffuseMethod(new DiffuseMethod.Lambert());

            float bitmapRatio = (float) mBitmap.getHeight() / mBitmap.getWidth();
            float planeWidth = 1f;
            mContent3D = new ContentPlane(planeWidth, planeWidth * bitmapRatio, 1, 1);
            mContent3D.setMaterial(material);
            mContent3D.setPosition(mContentPose.getPosition());
            mContent3D.setRotation(mContentPose.getOrientation());
            return mContent3D;
        }
    }

    public static class ActiveVisualContent extends VisualContent {
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

}
