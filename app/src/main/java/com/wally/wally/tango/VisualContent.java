package com.wally.wally.tango;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.util.Log;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.projecttango.rajawali.ContentPlane;
import com.projecttango.rajawali.Pose;
import com.projecttango.rajawali.TextureTranslateAnimation3D;
import com.wally.wally.R;
import com.wally.wally.datacontroller.content.Content;

import org.rajawali3d.Object3D;
import org.rajawali3d.animation.Animation;
import org.rajawali3d.animation.Animation3D;
//import org.rajawali3d.animation.ScaleAnimation3D;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.methods.DiffuseMethod;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.scene.RajawaliScene;

/**
 * Created by shota on 4/27/16.
 */
public class VisualContent {
    private static final String TAG = VisualContent.class.getSimpleName();

    protected static ContentPlane mBorder;
    private static TextureTranslateAnimation3D mBorderAnimation;
    private static Animation3D mHighlightAnimation;

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
        if (mContent.getTangoData() != null) {
            mContent3D.setScale(mContent.getTangoData().getScale());
        }
        return mContent3D;
    }

    private void initBorder(RajawaliScene scene){
        Material material = new Material();
        try {
            Texture t = new Texture("mContent3D", R.drawable.stripe);
            t.setWrapType(ATexture.WrapType.REPEAT);
            t.enableOffset(true);
            t.setRepeat(20, 20);
            material.addTexture(t);
        } catch (ATexture.TextureException e) {
            Log.e(TAG, "Exception generating mContent3D texture", e);
        }
        material.setColorInfluence(0);
        material.enableLighting(true);
        material.setDiffuseMethod(new DiffuseMethod.Lambert());

        mBorder = new ContentPlane(1, 1, 1, 1);
        mBorder.setMaterial(material);
        mBorder.setBlendingEnabled(true);
        mBorder.setBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        mBorderAnimation = new TextureTranslateAnimation3D();
        mBorderAnimation.setDurationMilliseconds(5000);
        mBorderAnimation.setDelayMilliseconds(1000);
        mBorderAnimation.setRepeatMode(Animation.RepeatMode.INFINITE);
        mBorderAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
        mBorderAnimation.setTransformable3D(mBorder);
        scene.registerAnimation(mBorderAnimation);
    }


    public void setBorder(RajawaliScene scene){
        ContentPlane c = (ContentPlane) mContent3D;
        if (mBorder == null) initBorder(scene);
        mBorder.setWidth((float) (c.getWidth() * mContent3D.getScale().x + .05f));
        mBorder.setHeight((float) (c.getHeight() * mContent3D.getScale().x + .05f));
        mBorder.setPosition(mContent3D.getPosition());
        mBorder.setOrientation(mContent3D.getOrientation());
        scene.addChild(mBorder);
        mBorderAnimation.play();

//        if (mHighlightAnimation != null) {
//            mHighlightAnimation.pause();
//            scene.unregisterAnimation(mHighlightAnimation);
//        }
//        mHighlightAnimation = new ScaleAnimation3D(new Vector3(1.04f, 1.04f, 1.04f));
//        mHighlightAnimation.setDurationMilliseconds(300);
//        mHighlightAnimation.setDelayMilliseconds(300);
//        mHighlightAnimation.setRepeatCount(1);
//        mHighlightAnimation.setRepeatMode(Animation.RepeatMode.NONE);
//        mHighlightAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
//        mHighlightAnimation.setTransformable3D(mContent3D);
//        scene.registerAnimation(mHighlightAnimation);
//        mHighlightAnimation.play();
    }

    public static void removeBorder(RajawaliScene scene){
        scene.removeChild(mBorder);
        mBorderAnimation.pause();
    }

    public double getScale(){
        return getObject3D().getScale().x;
    }

}
