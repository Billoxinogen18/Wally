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
    private static final float PLANE_WIDTH = 1f;

    protected Content mContent;


    public VisualContent2(Content content) {
        super();
        mContent = content;
        Bitmap bitmap = Utils.createBitmapFromContent(content);
        Pose pose = content.getTangoData().getPose();
        float ratio = (float) bitmap.getHeight() / bitmap.getWidth();
        setWidth(PLANE_WIDTH);
        setHeight(PLANE_WIDTH * ratio);
        setMaterial(createMaterial(bitmap));
        setPosition(pose.getPosition());
        setRotation(pose.getOrientation());
        setVisualContentScale();
    }

    protected void setVisualContentScale(){
        setScale(mContent.getTangoData().getScale());
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

}
