package com.projecttango.rajawali;


import org.rajawali3d.Object3D;
import org.rajawali3d.animation.Animation3D;
import org.rajawali3d.materials.textures.ATexture;


/**
 * Created by Xato on 4/27/2016.
 */
public class TextureTranslateAnimation3D extends Animation3D {

    protected ATexture texture;
    private float index = 0;

    public TextureTranslateAnimation3D() {
        super();

    }

    @Override
    protected void eventStart() {
        if (isFirstStart()) {
            index = 0;
        }

        super.eventStart();
    }

    public void setTransformable3D(Object3D object3D){
        super.setTransformable3D(object3D);
        texture = object3D.getMaterial().getTextureList().get(0);
    }

    @Override
    protected void applyTransformation() {
        index = (index + 0.001f) % 1;
        texture.setOffset(index, index);
    }
}
