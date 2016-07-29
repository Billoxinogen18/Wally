/*
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wally.wally.tango;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;

import com.google.atap.tangoservice.TangoCameraIntrinsics;
import com.google.atap.tangoservice.TangoPoseData;
import com.projecttango.rajawali.DeviceExtrinsics;
import com.projecttango.rajawali.Pose;
import com.projecttango.rajawali.ScenePoseCalculator;

import org.rajawali3d.Object3D;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.StreamingTexture;
import org.rajawali3d.math.Matrix4;
import org.rajawali3d.primitives.ScreenQuad;
import org.rajawali3d.renderer.RajawaliRenderer;
import org.rajawali3d.util.ObjectColorPicker;
import org.rajawali3d.util.OnObjectPickedListener;

import java.util.Iterator;

import javax.microedition.khronos.opengles.GL10;

/**
 * Very simple example point to point renderer which displays a line fixed in place.
 * Whenever the user clicks on the screen, the line is re-rendered with an endpoint
 * placed at the point corresponding to the depth at the point of the click.
 */
public class WallyRenderer extends RajawaliRenderer implements OnObjectPickedListener {

    private static final String TAG = WallyRenderer.class.getSimpleName();
    private static final int CONTENT_CHANGE_COUNT_ON_RENDER = 1;
    private long timeForARender;

    private VisualContentManager mVisualContentManager;

    // Augmented reality related fields
    private ATexture mTangoCameraTexture;
    private boolean mSceneCameraConfigured;
    private OnVisualContentSelectedListener mOnContentSelectedListener;
    private ObjectColorPicker mPicker;


    public WallyRenderer(Context context, VisualContentManager visualContentManager, OnVisualContentSelectedListener onContentSelectedListener) {
        super(context);
        mVisualContentManager = visualContentManager;
        mOnContentSelectedListener = onContentSelectedListener;
    }

    @Override
    protected void initScene() {
        // Create a quad covering the whole background and assign a texture to it where the
        // Tango color camera contents will be rendered.
        ScreenQuad backgroundQuad = new ScreenQuad();
        Material tangoCameraMaterial = new Material();
        tangoCameraMaterial.setColorInfluence(0);
        // We need to use Rajawali's {@code StreamingTexture} since it sets up the texture
        // for GL_TEXTURE_EXTERNAL_OES rendering
        mTangoCameraTexture =
                new StreamingTexture("camera", (StreamingTexture.ISurfaceListener) null);
        try {
            tangoCameraMaterial.addTexture(mTangoCameraTexture);
            backgroundQuad.setMaterial(tangoCameraMaterial);
        } catch (ATexture.TextureException e) {
            Log.e(TAG, "Exception creating texture for RGB camera contents", e);
        }
        getCurrentScene().addChildAt(backgroundQuad, 0);


        mPicker = new ObjectColorPicker(this);
        mPicker.setOnObjectPickedListener(this);

        mPicker.registerObject(backgroundQuad);

        timeForARender = (long)(500/getFrameRate());

    }

    @Override
    public void onObjectPicked(Object3D object) {
        VisualContent vc = mVisualContentManager.findContentByObject3D(object);
        Log.d(TAG, "onObjectPicked() called with: " + "object = [" + object + "]");

        if (vc != null) {
            if (mVisualContentManager.isSelectedContent(vc)) {
                addBorder();
            } else {
                mVisualContentManager.setSelectedContent(vc);
                removeBorder();
                VisualContentBorder.getInstance().updateBorderForContent(getCurrentScene(), vc);
                addBorder();
            }
        } else {
            removeBorder();
            Log.d(TAG, "Visual content is null");
        }
        mOnContentSelectedListener.onVisualContentSelected(vc);
    }

    public void getObjectAt(float x, float y) {
        mPicker.getObjectAt(x, y);
    }


    private void renderActiveContent() {
        ActiveVisualContent activeVisualContent = mVisualContentManager.getActiveContent();
        if (activeVisualContent == null) return;
        if (mVisualContentManager.shouldActiveContentRenderOnScreen()) {
            addActiveContent(activeVisualContent);
        } else if (mVisualContentManager.shouldActiveContentRemoveFromScreen()){
            removeActiveContent(activeVisualContent);
        } else {
            if (activeVisualContent.shouldAnimate()) {
                activeVisualContent.animate(getCurrentScene());
            }
        }
    }

    private void addActiveContent(ActiveVisualContent activeVisualContent){
        removeBorder();
        getCurrentScene().addChild(activeVisualContent.getVisual());
        mVisualContentManager.setActiveContentAdded();
        mPicker.registerObject(activeVisualContent.getVisual());
    }

    private void removeActiveContent(ActiveVisualContent activeVisualContent) {
        getCurrentScene().removeChild(activeVisualContent.getVisual());
        mVisualContentManager.setActiveContentRemoved();
        mOnContentSelectedListener.onVisualContentSelected(null);
    }

    private void renderStaticContent3(){
        Iterator<VisualContent> removeIt = mVisualContentManager.getStaticVisualContentToRemove();
        while(removeIt.hasNext()){
            VisualContent vc = removeIt.next();
            getCurrentScene().removeChild(vc.getVisual());
            mVisualContentManager.setStaticContentRemoved(vc);
            mOnContentSelectedListener.onVisualContentSelected(null);
            removeBorder();
        }

        Iterator<VisualContent> addIt = mVisualContentManager.getStaticVisualContentToAdd();
        while(addIt.hasNext()){
            VisualContent vc = addIt.next();
            getCurrentScene().addChild(vc.getVisual());
            mVisualContentManager.setStaticContentAdded(vc);
            mPicker.registerObject(vc.getVisual());
        }
    }

    private void renderStaticContent2(){
        Iterator<VisualContent> removeIt = mVisualContentManager.getStaticVisualContentToRemove();
        for (int i=0; i<CONTENT_CHANGE_COUNT_ON_RENDER; i++){
            if (removeIt.hasNext()){
                VisualContent vc = removeIt.next();
                getCurrentScene().removeChild(vc.getVisual());
                mVisualContentManager.setStaticContentRemoved(vc);
                mOnContentSelectedListener.onVisualContentSelected(null);
                removeBorder();
            } else {
                break;
            }
        }

        Iterator<VisualContent> addIt = mVisualContentManager.getStaticVisualContentToAdd();
        for (int i=0; i<CONTENT_CHANGE_COUNT_ON_RENDER; i++){
            if (addIt.hasNext()){
                VisualContent vc = addIt.next();
                getCurrentScene().addChild(vc.getVisual());
                mVisualContentManager.setStaticContentAdded(vc);
                mPicker.registerObject(vc.getVisual());
            } else {
                break;
            }
        }
    }

    private void renderStaticContent(){
        long startTime = System.currentTimeMillis();
        Iterator<VisualContent> removeIt = mVisualContentManager.getStaticVisualContentToRemove();
        Iterator<VisualContent> addIt = mVisualContentManager.getStaticVisualContentToAdd();
        boolean next = true;
        while(next) {
            next = false;
            if (removeIt.hasNext()) {
                VisualContent vc = removeIt.next();
                getCurrentScene().removeChild(vc.getVisual());
                Log.d(TAG, "renderStaticContent() definetlly removed " + vc);
                mVisualContentManager.setStaticContentRemoved(vc);
                mOnContentSelectedListener.onVisualContentSelected(null);
                removeBorder();
                next = true;
            }

            if (addIt.hasNext()) {
                VisualContent vc = addIt.next();
                getCurrentScene().addChild(vc.getVisual());
                Log.d(TAG, "renderStaticContent() added: " + vc);
                mVisualContentManager.setStaticContentAdded(vc);
                mPicker.registerObject(vc.getVisual());
                next = true;
            }
            long iterTime = System.currentTimeMillis();
            if (iterTime - startTime > timeForARender) return;
        }
    }


    @Override
    protected void onRender(long elapsedRealTime, double deltaTime) {
        synchronized (this) {
            renderActiveContent();
            renderStaticContent();
        }
        super.onRender(elapsedRealTime, deltaTime);
    }


    /**
     * Update the scene camera based on the provided pose in Tango start of service frame.
     * The device pose should match the pose of the device at the time the last rendered RGB
     * frame, which can be retrieved with this.getTimestamp();
     * <p/>
     * NOTE: This must be called from the OpenGL render thread - it is not thread safe.
     */
    public void updateRenderCameraPose(TangoPoseData devicePose, DeviceExtrinsics extrinsics) {
        Pose cameraPose = ScenePoseCalculator.toOpenGlCameraPose(devicePose, extrinsics);
        getCurrentCamera().setRotation(cameraPose.getOrientation());
        getCurrentCamera().setPosition(cameraPose.getPosition());
    }

    /**
     * It returns the ID currently assigned to the texture where the Tango color camera contents
     * should be rendered.
     * NOTE: This must be called from the OpenGL render thread - it is not thread safe.
     */
    public int getTextureId() {
        return mTangoCameraTexture == null ? -1 : mTangoCameraTexture.getTextureId();
    }

    /**
     * We need to override this method to mark the camera for re-configuration (set proper
     * projection matrix) since it will be reset by Rajawali on surface changes.
     */
    @Override
    public void onRenderSurfaceSizeChanged(GL10 gl, int width, int height) {
        super.onRenderSurfaceSizeChanged(gl, width, height);
        mSceneCameraConfigured = false;
    }

    public boolean isSceneCameraConfigured() {
        return mSceneCameraConfigured;
    }

    /**
     * Sets the projection matrix for the scen camera to match the parameters of the color camera,
     * provided by the {@code TangoCameraIntrinsics}.
     */
    public void setProjectionMatrix(TangoCameraIntrinsics intrinsics) {
        Matrix4 projectionMatrix = ScenePoseCalculator.calculateProjectionMatrix(
                intrinsics.width, intrinsics.height,
                intrinsics.fx, intrinsics.fy, intrinsics.cx, intrinsics.cy);
        getCurrentCamera().setProjectionMatrix(projectionMatrix);
    }

    @Override
    public void onOffsetsChanged(float xOffset, float yOffset,
                                 float xOffsetStep, float yOffsetStep,
                                 int xPixelOffset, int yPixelOffset) {
    }

    @Override
    public void onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN && !mVisualContentManager.isActiveContent()) {
            getObjectAt(event.getX(), event.getY());
        }
    }

    private void removeBorder() {
        if (mVisualContentManager.isBorderOnScreen()) {
            getCurrentScene().removeChild(VisualContentBorder.getInstance());
            mVisualContentManager.setBorderOnScreen(false);
        }
    }

    private void addBorder() {
        if (!mVisualContentManager.isBorderOnScreen()) {
            getCurrentScene().addChild(VisualContentBorder.getInstance());
            mVisualContentManager.setBorderOnScreen(true);
        }
    }
}
