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
package com.wally.wally;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.google.atap.tangoservice.TangoCameraIntrinsics;
import com.google.atap.tangoservice.TangoPoseData;
import com.projecttango.rajawali.ContentPlane;
import com.projecttango.rajawali.DeviceExtrinsics;
import com.projecttango.rajawali.Pose;
import com.projecttango.rajawali.ScenePoseCalculator;
import com.projecttango.rajawali.TextureTranslateAnimation3D;
import com.wally.wally.tango.VisualContentManager;

import org.rajawali3d.Object3D;
import org.rajawali3d.animation.Animation;
import org.rajawali3d.lights.ALight;
import org.rajawali3d.lights.PointLight;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.methods.DiffuseMethod;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.StreamingTexture;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.math.Matrix4;
import org.rajawali3d.primitives.ScreenQuad;
import org.rajawali3d.renderer.RajawaliRenderer;
import org.rajawali3d.util.ObjectColorPicker;
import org.rajawali3d.util.OnObjectPickedListener;

import javax.microedition.khronos.opengles.GL10;

/**
 * Very simple example point to point renderer which displays a line fixed in place.
 * Whenever the user clicks on the screen, the line is re-rendered with an endpoint
 * placed at the point corresponding to the depth at the point of the click.
 */
public class WallyRenderer extends RajawaliRenderer implements ScaleGestureDetector.OnScaleGestureListener, OnObjectPickedListener {

    private static final String TAG = WallyRenderer.class.getSimpleName();

    private VisualContentManager mVisualContentManager;

    // Augmented reality related fields
    private ATexture mTangoCameraTexture;
    private boolean mSceneCameraConfigured;
    private ScaleGestureDetector mScaleDetector;

    //changed
    private ObjectColorPicker mPicker;

    private ContentPlane mBorder;
    TextureTranslateAnimation3D mBorderAnimation;

    public WallyRenderer(Context context, VisualContentManager visualContentManager) {
        super(context);
        mVisualContentManager = visualContentManager;
        mScaleDetector = new ScaleGestureDetector(context, this);
    }

    private void initBorder(){
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
        getCurrentScene().registerAnimation(mBorderAnimation);
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

        ALight mContent3DLight = new PointLight();
        mContent3DLight.setColor(1.0f, 1.0f, 1.0f);
        mContent3DLight.setPower(1);

        //changed
        mPicker = new ObjectColorPicker(this);
        mPicker.setOnObjectPickedListener(this);

        initBorder();
    }

    //changed
    @Override
    public void onObjectPicked(Object3D object) {
        ContentPlane c = (ContentPlane) object;
        Log.d(TAG, "onObjectPicked() called with: " + "object = [" + object + "]");
//        mBorder.set(c.getHeight() + .05f, c.getWidth() + .05f, c.getScaleZ() + .05f);
        mBorder.setWidth(c.getWidth() + .05f);
        mBorder.setHeight(c.getHeight() + .05f);
        mBorder.setPosition(object.getPosition());
        mBorder.setOrientation(object.getOrientation());
        getCurrentScene().addChild(mBorder);
        mBorderAnimation.play();
    }

    public void getObjectAt(float x, float y) {
        Log.d(TAG, "getObjectAt() called with: " + "x = [" + x + "], y = [" + y + "]");
        mPicker.getObjectAt(x, y);
    }

    private void renderStaticContent() {
        if (mVisualContentManager.hasStaticContent()){
            for (VisualContentManager.VisualContent vContent : mVisualContentManager.getStaticContent()) {
                if (vContent.isNotRendered()) {
                    getCurrentScene().addChild(vContent.getObject3D());
                    //changed
                    mPicker.registerObject(vContent.getObject3D());
                }
            }
        }
    }

    private void renderActiveContent() {
        VisualContentManager.ActiveVisualContent activeContent = mVisualContentManager.getActiveContent();
        if (activeContent == null) return;
        if (activeContent.isNotYetAddedOnTheScene()) {
            getCurrentScene().addChild(activeContent.getObject3D());
            activeContent.setIsNotYetAddedOnTheScene(false);
        } else if (activeContent.getNewPose() != null) {
            activeContent.animate(getCurrentScene());
        }
    }

    @Override
    protected void onRender(long elapsedRealTime, double deltaTime) {
        synchronized (this) {
            if (mVisualContentManager.isContentBeingAdded()) {
                renderActiveContent();
            }
            if (mVisualContentManager.getStaticContentShouldBeRendered()) {
                renderStaticContent();
            }
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
        mScaleDetector.onTouchEvent(event);
        if(event.getAction() == MotionEvent.ACTION_DOWN){
            Log.d(TAG, "onTouchEvent() called with: " + "event = [" + event + "]" + event.getX() + ":" + event.getY());
            getObjectAt(event.getX(), event.getY());
        }
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        Log.d(TAG, "onScale() called with: " + "detector = [" + detector + "]");
        float scale = detector.getScaleFactor() != 0 ? detector.getScaleFactor() : 1f;
        mVisualContentManager.scaleActiveContent(scale);
        return true;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {

    }

    public void removeContent(Object3D activeContent) {
        getCurrentScene().removeChild(activeContent);
    }
}
