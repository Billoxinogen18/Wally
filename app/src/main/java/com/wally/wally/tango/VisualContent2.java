package com.wally.wally.tango;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.util.Log;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.projecttango.rajawali.ContentPlane;
import com.projecttango.rajawali.Pose;
import com.projecttango.rajawali.TextureTranslateAnimation3D;
import com.wally.wally.R;
import com.wally.wally.Utils;
import com.wally.wally.datacontroller.content.Content;

import org.rajawali3d.Object3D;
import org.rajawali3d.animation.Animation;
import org.rajawali3d.animation.Animation3D;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.methods.DiffuseMethod;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.scene.RajawaliScene;

/**
 * Created by shota on 4/29/16.
 */
public class VisualContent2 extends ContentPlane {
    private static final String TAG = VisualContent.class.getSimpleName();

    protected Content mContent;
    private boolean mSelected;

    private static final float PLANE_WIDTH = 1f;

    public VisualContent2(Content content, float ratio) {
        super(PLANE_WIDTH, PLANE_WIDTH * getRatio(content), 1, 1); //TODO refactor we create bitmap twise!!!
        mContent = content;
        Bitmap bitmap = Utils.createBitmapFromContent(content);
        Pose pose = content.getTangoData().getPose();

        setMaterial(createMaterial(bitmap));
        setPosition(pose.getPosition());
        setRotation(pose.getOrientation());
        if (mContent.getTangoData() != null) {
            setScale(mContent.getTangoData().getScale());
        }

    }

    public static float getRatio(Content content) {
        Bitmap bitmap = Utils.createBitmapFromContent(content);
        return (float) bitmap.getHeight() / bitmap.getWidth();

    }

    private Material createMaterial(Bitmap bitmap) {
        Material material = new Material();
        try {
            Texture t = new Texture("mContent3D", bitmap);  //TODO what is the mContent3D?
            material.addTexture(t);
        } catch (ATexture.TextureException e) {
            Log.e(TAG, "Exception generating mContent3D texture", e);
        }
        material.setColorInfluence(0);
        material.enableLighting(true);
        material.setDiffuseMethod(new DiffuseMethod.Lambert());
        return material;
    }

    public Content getContent() {
        return mContent;
    }

    public boolean isSelected() {
        return mSelected;
    }

    public void setSelected(boolean mIsSelected) {
        this.mSelected = mIsSelected;
    }



}
