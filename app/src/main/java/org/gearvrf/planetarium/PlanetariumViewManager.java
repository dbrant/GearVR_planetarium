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

package org.gearvrf.planetarium;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Environment;
import android.view.Gravity;
import android.view.KeyEvent;

import com.mhuss.AstroLib.AstroDate;
import com.mhuss.AstroLib.NoInitException;
import com.mhuss.AstroLib.ObsInfo;
import com.mhuss.AstroLib.PlanetData;
import com.mhuss.AstroLib.Planets;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Future;

import org.gearvrf.FutureWrapper;
import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRLight;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRPicker;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRScreenshotCallback;
import org.gearvrf.GVRScript;
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
    private static final int RENDER_ORDER_BACKGROUND = 0;
    private static final float MAX_STAR_MAGNITUDE = 4.0f;

    private static final float DEFAULT_DISTANCE_NEBULA = 550f;
    private static final float DEFAULT_DISTANCE_STAR = 500f;
    private static final float DEFAULT_DISTANCE_PLANET = 50f;

    private MainActivity mActivity;
    private GVRContext mContext;

    private GVRAnimationEngine mAnimationEngine;
    private GVRScene mMainScene;

    private List<StarReader.Star> starList;

    private List<GVRAnimation> continuousAnimationList = new ArrayList<>();
    private List<GVRAnimation> unzoomAnimationList = new ArrayList<>();

    private GVRMesh genericQuadMesh;
    private GVRTextViewSceneObject textView;

    private GVRWebViewSceneObject webViewObject;
    private boolean webViewVisible;

    private GVRLight mLight;


    private GVRSceneObject asyncSceneObject(GVRContext context,
                                            String textureName) throws IOException {
        return new GVRSceneObject(context, new GVRAndroidResource(context, "sphere.obj"),
                new GVRAndroidResource(context, textureName));
    }

    PlanetariumViewManager(MainActivity activity) {
        mActivity = activity;
    }

    @Override
    public void onInit(GVRContext gvrContext) throws IOException {
        mContext = gvrContext;
        mAnimationEngine = gvrContext.getAnimationEngine();

        starList = new ArrayList<>();
        StarReader.loadStars(mActivity, starList);

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

        // head-tracking pointer
        GVRSceneObject headTracker = new GVRSceneObject(gvrContext,
                new FutureWrapper<>(gvrContext.createQuad(1f, 1f)),
                gvrContext.loadFutureTexture(new GVRAndroidResource(
                        gvrContext, R.drawable.headtrack)));
        headTracker.getTransform().setPosition(0.0f, 0.0f, -50.0f);
        headTracker.getRenderData().setDepthTest(false);
        headTracker.getRenderData().setRenderingOrder(RENDER_ORDER_UI);
        mMainScene.getMainCameraRig().addChildObject(headTracker);

        textView = new GVRTextViewSceneObject(gvrContext, mActivity);
        textView.getTransform().setPosition(0.0f, -2.0f, -10.0f);
        textView.setTextSize(textView.getTextSize());
        textView.setText("");
        textView.setTextColor(Color.CYAN);
        textView.setGravity(Gravity.CENTER);
        textView.setRefreshFrequency(GVRTextViewSceneObject.IntervalFrequency.LOW);
        textView.getRenderData().setDepthTest(false);
        textView.getRenderData().setRenderingOrder(RENDER_ORDER_UI);
        mMainScene.getMainCameraRig().addChildObject(textView);

        webViewObject = new GVRWebViewSceneObject(gvrContext, 5f, 8f, mActivity.getWebView());
        webViewObject.getRenderData().getMaterial().setOpacity(1.0f);
        webViewObject.getTransform().setPosition(4.0f, 2.0f, -16.0f);
        webViewObject.getRenderData().setRenderingOrder(RENDER_ORDER_UI);

        GVRSceneObject solarSystemObject = new GVRSceneObject(gvrContext);
        mMainScene.addSceneObject(solarSystemObject);

        mLight = new GVRLight(gvrContext);
        mLight.setAmbientIntensity(0.5f, 0.5f, 0.5f, 1.0f);
        mLight.setDiffuseIntensity(1.0f, 1.0f, 1.0f, 1.0f);


        genericQuadMesh = gvrContext.createQuad(10f, 10f);

        Future<GVRTexture> futureTex = gvrContext.loadFutureTexture(new GVRAndroidResource(gvrContext, R.drawable.star2));
        GVRMaterial[] starMaterial = new GVRMaterial[10];
        float colorVal = 0f, colorInc = 0.9f / (float) starMaterial.length;
        for (int i = 0; i < starMaterial.length; i++) {
            starMaterial[i] = new GVRMaterial(gvrContext);
            starMaterial[i].setMainTexture(futureTex);
            float c = 0.1f + colorVal;
            colorVal += colorInc;
            starMaterial[i].setColor(c, c, c * 0.95f);
        }

        for (StarReader.Star star : starList) {
            if (star.mag > 4.0f) {
                continue;
            }

            int matIndex = (int) (starMaterial.length - star.mag * ((float) starMaterial.length / MAX_STAR_MAGNITUDE));
            if (matIndex < 0) { matIndex = 0; }
            if (matIndex >= starMaterial.length) { matIndex = starMaterial.length - 1; }

            GVRSceneObject sobj = new GVRSceneObject(gvrContext, genericQuadMesh);
            sobj.getRenderData().setMaterial(starMaterial[matIndex]);
            sobj.getRenderData().setDepthTest(false);

            float scale = 1.0f / (star.mag < 0.75f ? 0.75f : star.mag);
            if (scale < 0.5f) { scale = 0.5f; }
            sobj.getTransform().setScale(scale, scale, scale);
            setObjectPosition(sobj, star.ra, star.dec, DEFAULT_DISTANCE_STAR);
            sobj.setName(star.name == null ? "" : star.name);
            sobj.attachEyePointeeHolder();

            mMainScene.addSceneObject(sobj);
        }

        try {
            Calendar calendar = Calendar.getInstance();
            AstroDate date = new AstroDate(calendar.get(Calendar.DAY_OF_MONTH),
                    calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.YEAR));
            ObsInfo obsInfo = new ObsInfo();
            double jd = date.jd();

            GVRSceneObject sunObj = addPlanetObject(solarSystemObject, jd, obsInfo, Planets.SUN, "gstar.jpg", "Sun");

            // set the light position at the sun!
            mLight.setPosition(sunObj.getTransform().getPositionX(), sunObj.getTransform().getPositionY(), sunObj.getTransform().getPositionZ());

            addPlanetObject(solarSystemObject, jd, obsInfo, Planets.LUNA, "moon.jpg", "Moon");
            addPlanetObject(solarSystemObject, jd, obsInfo, Planets.MERCURY, "mercurymap.jpg", "Mercury");
            addPlanetObject(solarSystemObject, jd, obsInfo, Planets.VENUS, "venusmap.jpg", "Venus");
            addPlanetObject(solarSystemObject, jd, obsInfo, Planets.MARS, "mars_1k_color.jpg", "Mars");
            addPlanetObject(solarSystemObject, jd, obsInfo, Planets.JUPITER, "jupiter.jpg", "Jupiter");
            addPlanetObject(solarSystemObject, jd, obsInfo, Planets.SATURN, "saturn.jpg", "Saturn");
            addPlanetObject(solarSystemObject, jd, obsInfo, Planets.URANUS, "uranus.jpg", "Uranus");
            addPlanetObject(solarSystemObject, jd, obsInfo, Planets.NEPTUNE, "neptune.jpg", "Neptune");
            addPlanetObject(solarSystemObject, jd, obsInfo, Planets.PLUTO, "pluto.jpg", "Pluto");
        }catch(Exception e) {
            //
        }

        addNebulaObject(solarSystemObject, R.drawable.m1, Util.hmsToDec(5f, 34f, 31.94f), Util.dmsToDec(22f, 0f, 52.2f), 5f, "Crab Nebula");
        addNebulaObject(solarSystemObject, R.drawable.m13, Util.hmsToDec(16f, 41f, 41.24f), Util.dmsToDec(36f, 27f, 35.5f), 5f, "Hercules Cluster");
        addNebulaObject(solarSystemObject, R.drawable.m16, Util.hmsToDec(18f, 18f, 48f), Util.dmsToDec(-13f, 49f, 0f), 5f, "Eagle Nebula");
        addNebulaObject(solarSystemObject, R.drawable.m31, Util.hmsToDec(0f, 41.8f, 0f), Util.dmsToDec(41f, 16f, 0f), 10f, "Andromeda");
        addNebulaObject(solarSystemObject, R.drawable.m42, Util.hmsToDec(5f, 35f, 17.3f), Util.dmsToDec(-5f, 23f, 28f), 5f, "Orion Nebula");
        addNebulaObject(solarSystemObject, R.drawable.m51, Util.hmsToDec(13f, 30f, 0f), Util.dmsToDec(47f, 11f, 0f), 5f, "Whirlpool Galaxy");
        addNebulaObject(solarSystemObject, R.drawable.m57, Util.hmsToDec(18f, 53.6f, 0f), Util.dmsToDec(33f, 2f, 0f), 10f, "Ring Nebula");
        addNebulaObject(solarSystemObject, R.drawable.m101, Util.hmsToDec(14f, 3.2f, 0f), Util.dmsToDec(54f, 21f, 0f), 5f, "Pinwheel Galaxy");
        addNebulaObject(solarSystemObject, R.drawable.etacarinae, Util.hmsToDec(10f, 45f, 3.591f), Util.dmsToDec(-59f, 41f, 4.26f), 5f, "Eta Carinae");

    }

    @Override
    public void onStep() {
        for (GVRPicker.GVRPickedObject pickedObject : GVRPicker.findObjects(mContext.getMainScene())) {
            String objName = pickedObject.getHitObject().getName();
            boolean isZoomable = objName.contains("$");
            objName = objName.replace("$", "");

            textView.setText(objName);

            if (unzoomAnimationList.size() > 0) {
                for (GVRAnimation anim : unzoomAnimationList) {
                    anim.start(mAnimationEngine);
                }
                unzoomAnimationList.clear();
            }

            if (isZoomable) {
                GVRScaleAnimation anim = new GVRScaleAnimation(pickedObject.getHitObject(), 0.3f, 5f);
                anim.start(mAnimationEngine);

                GVRScaleAnimation unanim = new GVRScaleAnimation(pickedObject.getHitObject(), 0.3f, 1f);
                unzoomAnimationList.add(unanim);
            }

            // only care about the first picked object
            break;
        }
    }

    void onTap() {
        if (null == mMainScene) {
            return;
        }
        webViewVisible = false;
        for (GVRPicker.GVRPickedObject pickedObject : GVRPicker.findObjects(mContext.getMainScene())) {
            String objName = pickedObject.getHitObject().getName();
            boolean isZoomable = objName.contains("$");
            objName = objName.replace("$", "");

            mActivity.loadUrl("about:blank");
            mActivity.loadUrl("https://en.m.wikipedia.org/wiki/" + objName);
            webViewVisible = true;

            // only care about the first picked object
            break;
        }
        updateWebViewVisible();
    }

    private void updateWebViewVisible() {
        if (webViewVisible) {
            mMainScene.getMainCameraRig().addChildObject(webViewObject);
        } else {
            mMainScene.getMainCameraRig().removeChildObject(webViewObject);
        }
    }

    private void setObjectPosition(GVRSceneObject obj, double ra, double dec, float dist) {
        obj.getTransform().setPosition(0, 0, -dist);
        obj.getTransform().rotateByAxisWithPivot((float) ra, 0, 1, 0, 0, 0, 0);
        obj.getTransform().rotateByAxisWithPivot((float) dec, (float) Math.cos(Math.toRadians(ra)),
                0, (float) -Math.sin(Math.toRadians(ra)), 0, 0, 0);
    }

    private GVRSceneObject addPlanetObject(GVRSceneObject parentObj, double julianDate,
                                 ObsInfo obsInfo, int planetID, String planetTexture,
                                 String name) throws IOException, NoInitException {
        PlanetData data = new PlanetData(planetID, julianDate, obsInfo);
        GVRSceneObject planetRevolutionObject = new GVRSceneObject(mContext);
        setObjectPosition(planetRevolutionObject, Math.toDegrees(data.getRightAscension()),
                Math.toDegrees(data.getDeclination()), DEFAULT_DISTANCE_PLANET);
        parentObj.addChildObject(planetRevolutionObject);

        GVRSceneObject planetRotationObject = new GVRSceneObject(mContext);
        planetRevolutionObject.addChildObject(planetRotationObject);

        GVRSceneObject planetMeshObject = asyncSceneObject(mContext, planetTexture);
        planetRotationObject.addChildObject(planetMeshObject);
        planetMeshObject.getTransform().setScale(1.0f, 1.0f, 1.0f);
        planetMeshObject.getRenderData().setRenderingOrder(RENDER_ORDER_PLANET);
        planetMeshObject.getRenderData().setDepthTest(false);

        if (planetID != Planets.SUN) {
            planetMeshObject.getRenderData().getMaterial().setColor(0.5f, 0.5f, 0.5f);
            planetMeshObject.getRenderData().getMaterial().setOpacity(1.0f);
            planetMeshObject.getRenderData().getMaterial().setAmbientColor(0.1f, 0.1f, 0.1f, 1.0f);
            planetMeshObject.getRenderData().getMaterial().setDiffuseColor(1.0f, 1.0f, 1.0f, 1.0f);
            planetMeshObject.getRenderData().setLight(mLight);
            planetMeshObject.getRenderData().enableLight();
        }

        animateCounterClockwise(planetRotationObject, 10f);
        planetMeshObject.attachEyePointeeHolder();
        planetMeshObject.setName(name + "$");
        return planetRevolutionObject;
    }

    private GVRSceneObject addNebulaObject(GVRSceneObject parentObj, int textureResId, float ra, float dec, float scale, String name) throws IOException {
        GVRSceneObject sobj = new GVRSceneObject(mContext, genericQuadMesh,
                mContext.loadTexture(new GVRAndroidResource(mContext, textureResId)));
        parentObj.addChildObject(sobj);
        sobj.getRenderData().setRenderingOrder(RENDER_ORDER_BACKGROUND);
        sobj.getRenderData().setDepthTest(false);
        sobj.getTransform().setScale(scale, scale, scale);
        setObjectPosition(sobj, ra, dec, DEFAULT_DISTANCE_NEBULA);
        sobj.setName(name);
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
