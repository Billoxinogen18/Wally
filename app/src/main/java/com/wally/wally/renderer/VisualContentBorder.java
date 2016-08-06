package com.wally.wally.renderer;

import android.opengl.GLES20;
import android.util.Log;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.projecttango.rajawali.ContentPlane;
import com.projecttango.rajawali.TextureTranslateAnimation3D;
import com.wally.wally.R;

import org.rajawali3d.animation.Animation;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.methods.DiffuseMethod;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.scene.RajawaliScene;

/**
 * Created by shota on 4/29/16.
 */
public class VisualContentBorder extends ContentPlane {
    private static final String TAG = VisualContentBorder.class.getSimpleName();

    private static VisualContentBorder instance = new VisualContentBorder();
    private TextureTranslateAnimation3D mBorderAnimation;

    private boolean mBorderSet;
    private ContentPlane mContentForABorder;

    private boolean mAnimationWorking;


    private VisualContentBorder() {
        super(1, 1, 1, 1);

        setMaterial(createMaterial());
        setBlendingEnabled(true);
        setBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        mBorderAnimation = createAnimation();

        mBorderSet = false;
        mContentForABorder = null;
        mAnimationWorking = false;
    }

    public static VisualContentBorder getInstance() {
        if (instance == null) {
            instance = new VisualContentBorder();
        }
        return instance;
    }

    private Material createMaterial() {
        Material material = new Material();
        try {
            Texture t = new Texture("mContent3D", R.drawable.stripe);   //TODO what is the mContent3D?
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
        return material;
    }

    private TextureTranslateAnimation3D createAnimation() {
        TextureTranslateAnimation3D mBorderAnimation = new TextureTranslateAnimation3D();
        mBorderAnimation.setDurationMilliseconds(5000);
        mBorderAnimation.setDelayMilliseconds(1000);
        mBorderAnimation.setRepeatMode(Animation.RepeatMode.INFINITE);
        mBorderAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
        mBorderAnimation.setTransformable3D(this);
        return mBorderAnimation;
    }

    private void playAnimation(RajawaliScene scene) {
        if (mBorderSet && !mAnimationWorking) {
            scene.registerAnimation(mBorderAnimation);
            mBorderAnimation.play();
            mAnimationWorking = true;
        } else {
            Log.e(TAG, "playAnimation(): Inconsistent logic");
        }
    }

    private void stopAnimation(RajawaliScene scene) {
        if (mBorderSet && mAnimationWorking) {
            mBorderAnimation.pause(); //TODO check if this stops anumation
            scene.unregisterAnimation(mBorderAnimation);
            mAnimationWorking = false;
        } else {
            Log.e(TAG, "playAnimation(): Inconsistent logic");
        }
    }

    public void updateBorderForContent(RajawaliScene scene, VisualContent content) {
        mContentForABorder = content.getVisual();
        setWidth((float) (mContentForABorder.getWidth() * mContentForABorder.getScale().x + .05f)); //TODO must check if multiplication on scale is redundant or not
        setHeight((float) (mContentForABorder.getHeight() * mContentForABorder.getScale().x + .05f));
        setPosition(mContentForABorder.getPosition());
        setOrientation(mContentForABorder.getOrientation());
        mBorderSet = true;
        playAnimation(scene);
    }

}
