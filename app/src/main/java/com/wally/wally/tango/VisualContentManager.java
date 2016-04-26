package com.wally.wally.tango;

import android.graphics.Bitmap;
import android.util.Log;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.projecttango.rajawali.ContentPlane;
import com.projecttango.rajawali.Pose;
import com.wally.wally.Utils;
import com.wally.wally.datacontroller.content.Content;

import org.rajawali3d.Object3D;
import org.rajawali3d.animation.Animation3D;
import org.rajawali3d.animation.TranslateAnimation3D;
import org.rajawali3d.lights.ALight;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.methods.DiffuseMethod;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.primitives.Plane;
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

    public VisualContentManager() {
        staticContent = new ArrayList<>();
    }

    public boolean isContentBeingAdded() {
        return activeContent != null;
    }

    public void activeContentAddingFinished() {
        addStaticContent(activeContent);
        activeContent = null;
    }

    public boolean hasStaticContent() {
        return staticContent != null && staticContent.size() > 0;
    }

    public void addStaticContent(VisualContent visualContent) {
        staticContent.add(visualContent);
    }

    public List<VisualContent> getStaticContent() {
        return staticContent;
    }

    public synchronized void setActiveContent(ActiveVisualContent activeContent) {
        this.activeContent = activeContent;
    }

    public synchronized ActiveVisualContent getActiveContent() {
        return activeContent;
    }

    public static class VisualContent {
        protected Object3D mContent3D = null;
        private Bitmap mBitmap;
        protected Pose mContentPose;

        public VisualContent(Bitmap bitmap, Pose contentPose) {
            this.mBitmap = bitmap;
            this.mContentPose = contentPose;
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
        private Pose mNewPose;
        private boolean isNotYetAddedOnTheScene;

        public ActiveVisualContent(Bitmap bitmap, Pose contentPose) {
            super(bitmap, contentPose);
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
            mMoveAnim = new TranslateAnimation3D(mNewPose.getPosition());
            mMoveAnim.setTransformable3D(mContent3D);
            mMoveAnim.setDurationMilliseconds(500);
            mMoveAnim.setInterpolator(new AccelerateDecelerateInterpolator());
            scene.registerAnimation(mMoveAnim);
            mMoveAnim.play();

            // TODO make this with animation too
            mContent3D.setOrientation(mNewPose.getOrientation());


            mContentPose = mNewPose;
            mNewPose = null;
        }
    }

}
