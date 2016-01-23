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

import org.gearvrf.FutureWrapper;
import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRLight;
import org.gearvrf.GVRPicker;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRScreenshotCallback;
import org.gearvrf.GVRScript;
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

    private MainActivity mActivity;
    private GVRContext mContext;

    private GVRAnimationEngine mAnimationEngine;
    private GVRScene mMainScene;
    private GVRSceneObject rootObject;

    private List<SkyObject> skyObjectList = new ArrayList<>();
    AsterismLoader asterismLoader;

    private List<GVRAnimation> continuousAnimationList = new ArrayList<>();
    private List<GVRAnimation> unzoomAnimationList = new ArrayList<>();
    private boolean prevPickedNamedObject;

    private GVRTextViewSceneObject textView;
    private GVRWebViewSceneObject webViewObject;
    private boolean webViewVisible;
    private boolean webViewAdded;

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

        asterismLoader = new AsterismLoader();
        asterismLoader.loadAsterisms(mActivity, skyObjectList);

        StarLoader starLoader = new StarLoader();
        starLoader.loadStars(mActivity, skyObjectList);

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
        GVRLight mLight = new GVRLight(gvrContext);
        mLight.setAmbientIntensity(0.5f, 0.5f, 0.5f, 1.0f);
        mLight.setDiffuseIntensity(1.0f, 1.0f, 1.0f, 1.0f);

        for (int i = 0; i < skyObjectList.size(); i++) {
            SkyObject obj = skyObjectList.get(i);
            if (obj.type == SkyObject.TYPE_STAR) {

                asterismLoader.addStar(obj);
                if (obj.mag <= StarLoader.MAX_STAR_MAGNITUDE) {
                    GVRSceneObject sobj = starLoader.createSceneObject(gvrContext, obj, Integer.toString(i));
                    setObjectPosition(sobj, obj.ra, obj.dec, StarLoader.DEFAULT_DISTANCE_STAR);
                    sobj.getRenderData().setRenderingOrder(RENDER_ORDER_STAR);
                    rootObject.addChildObject(sobj);
                }

            } else if (obj.type == SkyObject.TYPE_NEBULA || obj.type == SkyObject.TYPE_OTHER) {

                GVRSceneObject sobj = NebulaLoader.createSceneObject(mContext, obj, Integer.toString(i));
                rootObject.addChildObject(sobj);
                sobj.getRenderData().setRenderingOrder(RENDER_ORDER_BACKGROUND);
                setObjectPosition(sobj, obj.ra, obj.dec, obj.dist);

            } else if (obj.type == SkyObject.TYPE_PLANET) {

                GVRSceneObject sobj = PlanetLoader.createSceneObject(gvrContext, obj, Integer.toString(i), RENDER_ORDER_PLANET - i, mLight);
                rootObject.addChildObject(sobj);
                setObjectPosition(sobj, obj.ra, obj.dec, PlanetLoader.DEFAULT_DISTANCE_PLANET);

                if (!obj.name.equals("Moon")) {
                    animateCounterClockwise(sobj.getChildByIndex(0), 10f);
                }

                if (obj.name.equals("Sun")) {
                    // let there be light
                    //mLight.setPosition(sobj.getTransform().getPositionX(), sobj.getTransform().getPositionY(), sobj.getTransform().getPositionZ());
                } else if (obj.name.equals("Saturn")) {
                    // put a ring on it
                    PlanetLoader.addRings(gvrContext, obj, 1.5f, 2.3f, -20f, R.drawable.saturn_rings, RENDER_ORDER_PLANET + 1);
                } else if (obj.name.equals("Uranus")) {
                    // put a ring on it
                    PlanetLoader.addRings(gvrContext, obj, 1.3f, 1.6f, 20f, R.drawable.uranus_rings, RENDER_ORDER_PLANET + 1);
                }
            }
        }

        for (Asterism asterism : asterismLoader.getAsterisms()) {
            GVRSceneObject asterismObj = asterism.createSceneObject(gvrContext);
            asterismObj.setName(Integer.toString(skyObjectList.indexOf(asterism.getSkyObject())));
            asterismObj.getRenderData().setRenderingOrder(RENDER_ORDER_ASTERISM);
            asterismObj.attachEyePointeeHolder();
            rootObject.addChildObject(asterismObj);

            GVRSceneObject labelObj = asterism.createLabelObject(gvrContext, mActivity);
            setObjectPosition(labelObj, asterism.getCenterRa(), asterism.getCenterDec(), StarLoader.DEFAULT_DISTANCE_STAR);
            labelObj.getRenderData().setRenderingOrder(RENDER_ORDER_ASTERISM + 1);
            rootObject.addChildObject(labelObj);
            asterism.setLabelObject(labelObj);
            asterism.setPassive();
        }

    }

    @Override
    public void onStep() {
        for (Asterism asterism : asterismLoader.getAsterisms()) {
            asterism.setPassive();
        }

        String text = "";
        boolean haveNamedObject = false;
        for (GVRPicker.GVRPickedObject pickedObject : GVRPicker.findObjects(mContext.getMainScene())) {
            SkyObject obj = skyObjectList.get(Integer.parseInt(pickedObject.getHitObject().getName()));

            if (obj.type == SkyObject.TYPE_ASTERISM) {
                for (Asterism asterism : asterismLoader.getAsterisms()) {
                    if (asterism.getSkyObject().equals(obj)) {
                        asterism.setActive();
                        break;
                    }
                }
                continue;
            }

            if (haveNamedObject) {
                continue;
            }
            haveNamedObject = true;
            text = obj.name;

            unzoomAll();

            if (obj.type == SkyObject.TYPE_PLANET) {
                GVRScaleAnimation anim = new GVRScaleAnimation(pickedObject.getHitObject(), 0.3f, obj.initialScale * 8f);
                anim.start(mAnimationEngine);
                GVRScaleAnimation unanim = new GVRScaleAnimation(pickedObject.getHitObject(), 0.3f, obj.initialScale);
                unzoomAnimationList.add(unanim);
            } else if (obj.type == SkyObject.TYPE_NEBULA || obj.type == SkyObject.TYPE_OTHER) {
                GVRScaleAnimation anim = new GVRScaleAnimation(pickedObject.getHitObject(), 0.3f, obj.initialScale * 4f);
                anim.start(mAnimationEngine);
                GVRScaleAnimation unanim = new GVRScaleAnimation(pickedObject.getHitObject(), 0.3f, obj.initialScale);
                unzoomAnimationList.add(unanim);
            }
        }

        if (prevPickedNamedObject && !haveNamedObject) {
            unzoomAll();
        }
        prevPickedNamedObject = haveNamedObject;

        if (text.length() > 0) {
            textView.setText(text);
        } else if (!haveNamedObject && textView.getText().length() > 0) {
            textView.setText("");
        }
    }

    private void unzoomAll() {
        if (unzoomAnimationList.size() > 0) {
            for (GVRAnimation anim : unzoomAnimationList) {
                anim.start(mAnimationEngine);
            }
            unzoomAnimationList.clear();
        }
    }

    public void onTap() {
        if (null == mMainScene) {
            return;
        }
        webViewVisible = false;
        for (GVRPicker.GVRPickedObject pickedObject : GVRPicker.findObjects(mContext.getMainScene())) {
            String objName = pickedObject.getHitObject().getName();
            SkyObject obj = skyObjectList.get(Integer.parseInt(objName));

            if (obj.type == SkyObject.TYPE_ASTERISM) {
                continue;
            }

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

    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP) {
            //takeScreenshot();
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

    private void takeScreenshot() {
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
}
