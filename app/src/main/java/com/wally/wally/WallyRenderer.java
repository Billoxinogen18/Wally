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

import com.google.atap.tangoservice.TangoCameraIntrinsics;
import com.google.atap.tangoservice.TangoPoseData;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;

import org.rajawali3d.Object3D;
import org.rajawali3d.lights.DirectionalLight;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.methods.DiffuseMethod;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.StreamingTexture;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.math.Matrix4;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.Plane;
import org.rajawali3d.primitives.ScreenQuad;
import org.rajawali3d.primitives.Sphere;
import org.rajawali3d.renderer.RajawaliRenderer;


import javax.microedition.khronos.opengles.GL10;

import com.projecttango.rajawali.DeviceExtrinsics;
import com.projecttango.rajawali.Pose;
import com.projecttango.rajawali.ScenePoseCalculator;

/**
 * Very simple example point to point renderer which displays a line fixed in place.
 * Whenever the user clicks on the screen, the line is re-rendered with an endpoint
 * placed at the point corresponding to the depth at the point of the click.
 */
public class WallyRenderer extends RajawaliRenderer {

    private static final String TAG = WallyRenderer.class.getSimpleName();
    private Object3D earth;
    private Pose mPoint;
    private boolean mLineUpdated = false;

    // Augmented reality related fields
    private ATexture mTangoCameraTexture;
    private boolean mSceneCameraConfigured;

    public WallyRenderer(Context context) {
        super(context);
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

        // Add a directional light in an arbitrary direction.
        DirectionalLight light = new DirectionalLight(1, 0.2, -1);
        light.setColor(1, 1, 1);
        light.setPower(0.8f);
        light.setPosition(3, 2, 4);
        getCurrentScene().addLight(light);
    }

    @Override
    protected void onRender(long elapsedRealTime, double deltaTime) {
        // Update the AR object if necessary
        // Synchronize against concurrent access with the setter below.
        synchronized (this) {
            if (mLineUpdated) {
                if (earth != null) {
                    getCurrentScene().removeChild(earth);
                }
                if (mPoint != null) {
                    Material material = new Material();
                    try {
                        Texture t = new Texture("earth", R.drawable.earth_diffuse);
                        material.addTexture(t);
                    } catch (ATexture.TextureException e) {
                        Log.e(TAG, "Exception generating earth texture", e);
                    }
                    material.setColorInfluence(0);
                    material.enableLighting(true);
                    material.setDiffuseMethod(new DiffuseMethod.Lambert());
//                    earth = new Sphere(0.5f, 20, 20);
                    earth = new Plane(1f,1.6f, 1, 1);
                    earth.setMaterial(material);
                    earth.setPosition(mPoint.getPosition());
                    earth.setRotation(mPoint.getOrientation());
                    getCurrentScene().addChild(earth);
                } else {
                    earth = null;
                }
                mLineUpdated = false;
            }

        }

        super.onRender(elapsedRealTime, deltaTime);
    }

    public synchronized void setLine(TangoPoseData point) {
        mPoint = ScenePoseCalculator.toOpenGLPose(point);
        mLineUpdated = true;
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

    }
}
