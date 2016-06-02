package com.wally.wally.tango;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.util.Log;

import com.projecttango.rajawali.ContentPlane;
import com.projecttango.rajawali.Pose;
import com.wally.wally.Utils;
import com.wally.wally.datacontroller.content.Content;

import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.methods.DiffuseMethod;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.Texture;

/**
 * Created by shota on 4/29/16.
 */
public class VisualContent {
    public enum RenderStatus {None, PendingRender, PendingRemove, Rendered}

    private static final String TAG = VisualContent.class.getSimpleName();
    private static final float PLANE_WIDTH = 1f;
    private RenderStatus mStatus;
    protected ContentPlane mVisual;
    protected Content mContent;




    public VisualContent(@NonNull Content content) {
        mContent = content;
        mStatus = RenderStatus.None;
    }

    public VisualContent cloneContent(){
        VisualContent res = new VisualContent(mContent);
        res.setStatus(mStatus);
        //res.setVisual(mVisual);
        return res;
    }

    protected void refreshVisualScale() {
        if (mVisual != null && mContent.getTangoData() != null) {
            mVisual.setScale(mContent.getTangoData().getScale());
        }
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
        Log.d(TAG, "createMaterial() returned: " + material);
        return material;
    }

    //Should be called from rajawali thread
    public ContentPlane getVisual() {
        if (mVisual == null) {
            mVisual = new ContentPlane();

            Bitmap bitmap = Utils.createBitmapFromContent(mContent);
            Log.d(TAG, "getVisual() bitmap " + bitmap.getHeight() + "x" + bitmap.getWidth());
            float ratio = (float) bitmap.getHeight() / bitmap.getWidth();
            mVisual.setWidth(PLANE_WIDTH);
            mVisual.setHeight(PLANE_WIDTH * ratio);
            mVisual.setMaterial(createMaterial(bitmap));
            if (mContent.getTangoData() != null) {               //TODO fix bad design
                Pose pose = mContent.getTangoData().getPose();
                mVisual.setPosition(pose.getPosition());
                mVisual.setRotation(pose.getOrientation());
            }
            refreshVisualScale();
        }
        return mVisual;
    }

    public void setVisual(ContentPlane visual){
        mVisual = visual;
    }

    public Content getContent() {
        return mContent;
    }

    public RenderStatus getStatus(){
        return mStatus;
    }
    
    public void setStatus(RenderStatus status){
        this.mStatus = status;
    }

}
