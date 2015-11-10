/* Copyright 2015 Dmitry Brant
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dmitrybrant.gearvrf.planetarium;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.opengl.GLES20;
import android.os.Environment;
import android.view.Gravity;
import android.view.KeyEvent;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Future;

import org.gearvrf.FutureWrapper;
import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRLight;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMaterialShaderId;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRPicker;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRScreenshotCallback;
import org.gearvrf.GVRScript;
import org.gearvrf.GVRStockMaterialShaderId;
import org.gearvrf.GVRTexture;
import org.gearvrf.GVRTransform;
import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.animation.GVRAnimationEngine;
import org.gearvrf.animation.GVRRepeatMode;
import org.gearvrf.animation.GVRRotationByAxisWithPivotAnimation;
import org.gearvrf.animation.GVRScaleAnimation;
import org.gearvrf.scene_objects.GVRTextViewSceneObject;
import org.gearvrf.scene_objects.GVRWebViewSceneObject;
import org.gearvrf.utility.Log;

public class PlanetariumViewManager extends GVRScript {
    private static final String TAG = Log.tag(PlanetariumViewManager.class);
    private static final int RENDER_ORDER_UI = 100000;
    private static final int RENDER_ORDER_PLANET = 99900;
    private static final int RENDER_ORDER_STAR = 2000;
    private static final int RENDER_ORDER_ASTERISM = 1000;
    private static final int RENDER_ORDER_BACKGROUND = 0;

    private static final float MAX_STAR_MAGNITUDE = 4.5f;
    private static final float DEFAULT_DISTANCE_STAR = 500f;

    private MainActivity mActivity;
    private GVRContext mContext;

    private GVRAnimationEngine mAnimationEngine;
    private GVRScene mMainScene;
    private GVRSceneObject rootObject;

    private List<SkyObject> skyObjectList = new ArrayList<>();

    private List<GVRAnimation> continuousAnimationList = new ArrayList<>();
    private List<GVRAnimation> unzoomAnimationList = new ArrayList<>();

    private GVRMesh genericQuadMesh;
    private GVRTextViewSceneObject textView;

    private GVRWebViewSceneObject webViewObject;
    private boolean webViewVisible;
    private boolean webViewAdded;

    private GVRLight mLight;

    PlanetariumViewManager(MainActivity activity) {
        mActivity = activity;
    }

    @Override
    public void onInit(GVRContext gvrContext) throws IOException {
        mContext = gvrContext;
        mAnimationEngine = gvrContext.getAnimationEngine();

        List<SkyObject> planetObjectList = new ArrayList<>();
        PlanetLoader.loadPlanets(mContext, planetObjectList);
        Collections.sort(planetObjectList, new Comparator<SkyObject>() {
            @Override
            public int compare(SkyObject lhs, SkyObject rhs) {
                return lhs.dist < rhs.dist ? -1 : lhs.dist > rhs.dist ? 1 : 0;
            }
        });
        skyObjectList.addAll(planetObjectList);
        NebulaLoader.loadNebulae(mContext, skyObjectList);
        OtherObjLoader.loadObjects(skyObjectList);
        Asterisms.loadAsterisms(mActivity);
        StarReader.loadStars(mActivity, skyObjectList);

        mMainScene = gvrContext.getNextMainScene(new Runnable() {
            @Override
            public void run() {
                for (GVRAnimation animation : continuousAnimationList) {
                    animation.start(mAnimationEngine);
                }
                continuousAnimationList = null;
            }
        });

        mMainScene.setFrustumCulling(true);
        mMainScene.getMainCameraRig().getLeftCamera().setBackgroundColor(0.0f, 0.0f, 0.0f, 1.0f);
        mMainScene.getMainCameraRig().getRightCamera().setBackgroundColor(0.0f, 0.0f, 0.0f, 1.0f);
        mMainScene.getMainCameraRig().getTransform().setPosition(0.0f, 0.0f, 0.0f);

        rootObject = new GVRSceneObject(gvrContext);
        mMainScene.addSceneObject(rootObject);

        // head-tracking pointer
        GVRSceneObject headTracker = new GVRSceneObject(gvrContext,
                new FutureWrapper<>(gvrContext.createQuad(1f, 1f)),
                gvrContext.loadFutureTexture(new GVRAndroidResource(
                        gvrContext, R.drawable.headtrack)));
        headTracker.getTransform().setPosition(0.0f, 0.0f, -50.0f);
        headTracker.getRenderData().setDepthTest(false);
        headTracker.getRenderData().setRenderingOrder(RENDER_ORDER_UI);
        mMainScene.getMainCameraRig().addChildObject(headTracker);

        // text view...
        textView = new GVRTextViewSceneObject(gvrContext, mActivity, 4f, 2f, "");
        textView.getTransform().setPosition(0.0f, 1.5f, -10.0f);
        textView.setTextSize(textView.getTextSize());
        textView.setText("");
        textView.setTextColor(Color.CYAN);
        textView.setGravity(Gravity.CENTER);
        textView.setRefreshFrequency(GVRTextViewSceneObject.IntervalFrequency.LOW);
        textView.getRenderData().setDepthTest(false);
        textView.getRenderData().setRenderingOrder(RENDER_ORDER_UI + 1);
        mMainScene.getMainCameraRig().addChildObject(textView);

        // web view...
        webViewObject = new GVRWebViewSceneObject(gvrContext, 5f, 8f, mActivity.getWebView());
        webViewObject.getRenderData().getMaterial().setOpacity(1.0f);
        webViewObject.getTransform().setPosition(0.0f, -5.0f, -12.0f);
        webViewObject.getRenderData().setRenderingOrder(RENDER_ORDER_UI);

        // light!
        mLight = new GVRLight(gvrContext);
        mLight.setAmbientIntensity(0.5f, 0.5f, 0.5f, 1.0f);
        mLight.setDiffuseIntensity(1.0f, 1.0f, 1.0f, 1.0f);


        genericQuadMesh = gvrContext.createQuad(10f, 10f);

        GVRMesh starMesh = new GVRMesh(gvrContext);
        starMesh.setVertices(new float[]{ -5f, -4.5f, 0f, 0f, 5.5f, 0f, 5f, -4.5f, 0f });
        starMesh.setNormals(new float[]{0f, 0f, 1f, 0f, 0f, 1f, 0f, 0f, 1f});
        starMesh.setTexCoords(new float[]{0f, 0f, 0.5f, 0.8f, 1f, 0f});
        starMesh.setTriangles(new char[]{ 0, 2, 1 });

        Future<GVRTexture> starTex = gvrContext.loadFutureTexture(new GVRAndroidResource(gvrContext, R.drawable.star4));
        GVRMaterial[] starMaterial = new GVRMaterial[10];
        float colorVal = 0f, colorInc = 0.9f / (float) starMaterial.length;
        for (int i = 0; i < starMaterial.length; i++) {
            starMaterial[i] = new GVRMaterial(gvrContext);
            starMaterial[i].setMainTexture(starTex);
            float c = 0.1f + colorVal;
            colorVal += colorInc;
            starMaterial[i].setColor(c, c, c * 0.90f);
        }

        for (int i = 0; i < skyObjectList.size(); i++) {
            SkyObject obj = skyObjectList.get(i);
            if (obj.type == SkyObject.TYPE_STAR) {

                Asterisms.addStar(obj);

                if (obj.mag <= MAX_STAR_MAGNITUDE) {
                    int matIndex = (int) (starMaterial.length - obj.mag * ((float) starMaterial.length / MAX_STAR_MAGNITUDE));
                    if (matIndex < 0) {
                        matIndex = 0;
                    }
                    if (matIndex >= starMaterial.length) {
                        matIndex = starMaterial.length - 1;
                    }

                    GVRSceneObject sobj = new GVRSceneObject(gvrContext, starMesh);
                    obj.sceneObj = sobj;
                    sobj.getRenderData().setMaterial(starMaterial[matIndex]);
                    sobj.getRenderData().setDepthTest(false);
                    sobj.getRenderData().setRenderingOrder(RENDER_ORDER_STAR);

                    float scale = 1.0f / (obj.mag < 0.75f ? 0.75f : obj.mag);
                    if (scale < 1f) {
                        scale = 1f;
                    }
                    obj.initialScale = scale;
                    sobj.getTransform().setScale(scale, scale, scale);
                    setObjectPosition(sobj, obj.ra, obj.dec, DEFAULT_DISTANCE_STAR);
                    sobj.setName(Integer.toString(i));
                    sobj.attachEyePointeeHolder();
                    rootObject.addChildObject(sobj);
                }

            } else if (obj.type == SkyObject.TYPE_NEBULA || obj.type == SkyObject.TYPE_OTHER) {

                GVRSceneObject sobj = addNebulaObject(rootObject, obj);
                obj.sceneObj = sobj;
                sobj.setName(Integer.toString(i));

            } else if (obj.type == SkyObject.TYPE_PLANET) {

                GVRSceneObject sobj = addPlanetObject(rootObject, obj, i);
                obj.sceneObj = sobj;
                if (obj.name.equals("Sun")) {
                    // let there be light
                    //mLight.setPosition(sobj.getTransform().getPositionX(), sobj.getTransform().getPositionY(), sobj.getTransform().getPositionZ());
                } else if (obj.name.equals("Saturn")) {
                    // put a ring on it
                    GVRMesh ringMesh = RingMesh.createRingMesh(gvrContext, 1.5f, 2.3f, 32);
                    GVRSceneObject ringObj = new GVRSceneObject(gvrContext, new FutureWrapper<>(ringMesh),
                            gvrContext.loadFutureTexture(new GVRAndroidResource(mContext, R.drawable.saturn_rings)));

                    ringObj.getTransform().rotateByAxis(-90f, 1f, 0f, 0f);
                    ringObj.getRenderData().setDepthTest(true);
                    ringObj.getRenderData().setRenderingOrder(RENDER_ORDER_PLANET + 1);
                    sobj.getChildByIndex(0).getChildByIndex(0).getRenderData().setDepthTest(true);
                    sobj.getChildByIndex(0).getChildByIndex(0).addChildObject(ringObj);
                    sobj.getTransform().rotateByAxis(-20f, 1f, 0f, 0f);
                } else if (obj.name.equals("Uranus")) {
                    // put a ring on it
                    GVRMesh ringMesh = RingMesh.createRingMesh(gvrContext, 1.3f, 1.6f, 32);
                    GVRSceneObject ringObj = new GVRSceneObject(gvrContext, new FutureWrapper<>(ringMesh),
                            gvrContext.loadFutureTexture(new GVRAndroidResource(mContext, R.drawable.uranus_rings)));

                    ringObj.getTransform().rotateByAxis(-90f, 1f, 0f, 0f);
                    ringObj.getRenderData().setDepthTest(true);
                    ringObj.getRenderData().setRenderingOrder(RENDER_ORDER_PLANET + 1);
                    sobj.getChildByIndex(0).getChildByIndex(0).getRenderData().setDepthTest(true);
                    sobj.getChildByIndex(0).getChildByIndex(0).addChildObject(ringObj);
                    sobj.getTransform().rotateByAxis(20f, 1f, 0f, 0f);
                }

            }
        }



        SolidColorShader shader = new SolidColorShader(gvrContext);
        GVRMaterial asterismMat = new GVRMaterial(gvrContext, shader.getShaderId());
        asterismMat.setVec4(SolidColorShader.COLOR_KEY, 0.0f, 0.1f, 0.15f, 1.0f);

        for (Asterisms.Asterism asterism : Asterisms.getAsterisms()) {
            GVRMesh mesh = asterism.createMesh(gvrContext);
            GVRSceneObject asterismObj = new GVRSceneObject(gvrContext, mesh);
            asterismObj.getRenderData().setMaterial(asterismMat);
            asterismObj.getRenderData().setDepthTest(false);
            asterismObj.getRenderData().setRenderingOrder(RENDER_ORDER_ASTERISM);
            asterismObj.getRenderData().setDrawMode(GLES20.GL_LINES);
            rootObject.addChildObject(asterismObj);
        }



    }

    @Override
    public void onStep() {
        boolean havePicked = false;

        for (GVRPicker.GVRPickedObject pickedObject : GVRPicker.findObjects(mContext.getMainScene())) {
            try {
                SkyObject obj = skyObjectList.get(Integer.parseInt(pickedObject.getHitObject().getName()));

                textView.setText(obj.name);

                if (unzoomAnimationList.size() > 0) {
                    for (GVRAnimation anim : unzoomAnimationList) {
                        anim.start(mAnimationEngine);
                    }
                    unzoomAnimationList.clear();
                }

                if (obj.type == SkyObject.TYPE_PLANET) {
                    GVRScaleAnimation anim = new GVRScaleAnimation(pickedObject.getHitObject(), 0.3f, obj.initialScale * 5f);
                    anim.start(mAnimationEngine);
                    GVRScaleAnimation unanim = new GVRScaleAnimation(pickedObject.getHitObject(), 0.3f, obj.initialScale);
                    unzoomAnimationList.add(unanim);
                } else if (obj.type == SkyObject.TYPE_NEBULA || obj.type == SkyObject.TYPE_OTHER) {
                    GVRScaleAnimation anim = new GVRScaleAnimation(pickedObject.getHitObject(), 0.3f, obj.initialScale * 2f);
                    anim.start(mAnimationEngine);
                    GVRScaleAnimation unanim = new GVRScaleAnimation(pickedObject.getHitObject(), 0.3f, obj.initialScale);
                    unzoomAnimationList.add(unanim);
                }
            }catch(Exception ex) {
                mActivity.createVrToast("error: " + ex.getMessage() + ", " + pickedObject.getHitObject().getName());
            }
            // only care about the first picked object
            havePicked = true;
            break;
        }
        if (!havePicked && textView.getText().length() > 0) {
            textView.setText("");
        }
    }

    void onTap() {
        if (null == mMainScene) {
            return;
        }
        webViewVisible = false;
        for (GVRPicker.GVRPickedObject pickedObject : GVRPicker.findObjects(mContext.getMainScene())) {
            String objName = pickedObject.getHitObject().getName();
            SkyObject obj = skyObjectList.get(Integer.parseInt(objName));

            mActivity.loadWebPageForObject(obj);
            webViewVisible = true;

            webViewObject.getTransform().setPosition(0f, 0f, 0f);
            webViewObject.getTransform().setRotationByAxis(0f, 0f, 0f, 1f);
            webViewObject.getTransform().setRotationByAxis(0f, 0f, 1f, 0f);
            webViewObject.getTransform().setRotationByAxis(0f, 1f, 0f, 0f);
            setObjectPosition(webViewObject, obj.ra - 20f, obj.dec, 12f);

            // only care about the first picked object
            break;
        }
        updateWebViewVisible();
    }

    private void updateWebViewVisible() {
        if (webViewVisible) {
            if (!webViewAdded) {
                rootObject.addChildObject(webViewObject);
                webViewAdded = true;
            }
        } else {
            rootObject.removeChildObject(webViewObject);
            webViewAdded = false;
        }
    }

    private void setObjectPosition(GVRSceneObject obj, double ra, double dec, float dist) {
        obj.getTransform().setPosition(0, 0, -dist);
        obj.getTransform().rotateByAxisWithPivot((float) ra, 0, 1, 0, 0, 0, 0);
        obj.getTransform().rotateByAxisWithPivot((float) dec, (float) Math.cos(Math.toRadians(ra)),
                0, (float) -Math.sin(Math.toRadians(ra)), 0, 0, 0);
    }

    private GVRSceneObject addPlanetObject(GVRSceneObject parentObj, SkyObject obj, int index) throws IOException {
        GVRSceneObject planetRevolutionObject = new GVRSceneObject(mContext);
        setObjectPosition(planetRevolutionObject, obj.ra, obj.dec, PlanetLoader.DEFAULT_DISTANCE_PLANET);
        parentObj.addChildObject(planetRevolutionObject);

        GVRSceneObject planetRotationObject = new GVRSceneObject(mContext);
        planetRevolutionObject.addChildObject(planetRotationObject);

        GVRSceneObject planetMeshObject = new GVRSceneObject(mContext,
                new GVRAndroidResource(mContext, "sphere.obj"),
                new GVRAndroidResource(mContext, obj.texName));

        planetRotationObject.addChildObject(planetMeshObject);
        planetMeshObject.getTransform().setScale(obj.initialScale, obj.initialScale, obj.initialScale);
        planetMeshObject.getRenderData().setRenderingOrder(RENDER_ORDER_PLANET - index);
        planetMeshObject.getRenderData().setDepthTest(false);

        if (!obj.name.equals("Sun")) {
            planetMeshObject.getRenderData().getMaterial().setColor(0.5f, 0.5f, 0.5f);
            planetMeshObject.getRenderData().getMaterial().setOpacity(1.0f);
            planetMeshObject.getRenderData().getMaterial().setAmbientColor(0.1f, 0.1f, 0.1f, 1.0f);
            planetMeshObject.getRenderData().getMaterial().setDiffuseColor(1.0f, 1.0f, 1.0f, 1.0f);
            planetMeshObject.getRenderData().setLight(mLight);
            planetMeshObject.getRenderData().enableLight();
        }

        if (obj.name.equals("Moon")) {
            planetMeshObject.getTransform().rotateByAxis(70f, 0f, 1f, 0f);
        } else {
            animateCounterClockwise(planetRotationObject, 10f);
        }

        planetMeshObject.attachEyePointeeHolder();
        planetMeshObject.setName(Integer.toString(index));
        return planetRevolutionObject;
    }

    private GVRSceneObject addNebulaObject(GVRSceneObject parentObj, SkyObject obj) throws IOException {
        GVRSceneObject sobj = new GVRSceneObject(mContext, genericQuadMesh,
                mContext.loadTexture(new GVRAndroidResource(mContext, obj.texResId)));
        parentObj.addChildObject(sobj);
        sobj.getRenderData().setRenderingOrder(RENDER_ORDER_BACKGROUND);
        sobj.getRenderData().setDepthTest(false);
        sobj.getTransform().setScale(obj.initialScale, obj.initialScale, obj.initialScale);
        setObjectPosition(sobj, obj.ra, obj.dec, obj.dist);
        sobj.attachEyePointeeHolder();
        return sobj;
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP) {
            mContext.captureScreenLeft(new GVRScreenshotCallback() {
                @Override
                public void onScreenCaptured(Bitmap bitmap) {
                    try {
                        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bytes);
                        int imageIndex = 0;
                        File f;
                        String fileName;
                        do {
                            fileName = Integer.toString(imageIndex) + ".png";
                            imageIndex++;
                        } while ((f = new File(Environment.getExternalStorageDirectory().getPath() + "/" + fileName)).exists());
                        FileOutputStream fo = new FileOutputStream(f);
                        fo.write(bytes.toByteArray());
                        fo.close();
                        mActivity.createVrToastOnUiThread("Saved screen to " + fileName);
                    } catch (Exception e) {
                        mActivity.createVrToastOnUiThread("Screenshot error: " + e.getMessage());
                    }
                }
            });
        }

        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            if (webViewVisible) {
                webViewVisible = false;
                updateWebViewVisible();
                return true;
            }
        }
        return false;
    }

    public void onScroll(float scrollX, float scrollY) {
        if (Math.abs(scrollX) > Math.abs(scrollY)) {
            // horizontal scroll
            if (rootObject != null) {
                rootObject.getTransform().rotateByAxis(scrollX / 5f, 0f, 1f, 0f);
            }
        }
    }


    private void setupAnimation(GVRAnimation animation) {
        animation.setRepeatMode(GVRRepeatMode.REPEATED).setRepeatCount(-1);
        continuousAnimationList.add(animation);
    }

    private void animateCounterClockwise(GVRSceneObject object, float duration) {
        setupAnimation(new GVRRotationByAxisWithPivotAnimation(
                object, duration, 360.0f,
                0.0f, 1.0f, 0.0f,
                0.0f, 0.0f, 0.0f));
    }

    private void animateClockwise(GVRSceneObject object, float duration) {
        setupAnimation(new GVRRotationByAxisWithPivotAnimation(
                object, duration, -360.0f,
                0.0f, 1.0f, 0.0f,
                0.0f, 0.0f, 0.0f));
    }

    private void animateClockwise(GVRTransform transform, float duration) {
        setupAnimation(new GVRRotationByAxisWithPivotAnimation(
                transform, duration, -360.0f,
                0.0f, 1.0f, 0.0f,
                0.0f, 0.0f, 0.0f));
    }

}
