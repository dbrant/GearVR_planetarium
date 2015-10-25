/* Copyright 2015 Samsung Electronics Co., LTD
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

import android.graphics.Color;
import android.opengl.Matrix;
import android.os.Environment;
import android.view.Gravity;

import com.mhuss.AstroLib.AstroDate;
import com.mhuss.AstroLib.NoInitException;
import com.mhuss.AstroLib.ObsInfo;
import com.mhuss.AstroLib.PlanetData;
import com.mhuss.AstroLib.Planets;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Future;

import org.gearvrf.FutureWrapper;
import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRBitmapTexture;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMaterialShaderId;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRPicker;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRScript;
import org.gearvrf.GVRStockMaterialShaderId;
import org.gearvrf.GVRTexture;
import org.gearvrf.GVRTransform;
import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.animation.GVRAnimationEngine;
import org.gearvrf.animation.GVRRepeatMode;
import org.gearvrf.animation.GVRRotationByAxisWithPivotAnimation;
import org.gearvrf.animation.GVRScaleAnimation;
import org.gearvrf.scene_objects.GVRCubeSceneObject;
import org.gearvrf.scene_objects.GVRSphereSceneObject;
import org.gearvrf.scene_objects.GVRTextViewSceneObject;
import org.gearvrf.utility.Log;

public class PlanetariumViewManager extends GVRScript {

    @SuppressWarnings("unused")
    private static final String TAG = Log.tag(PlanetariumViewManager.class);

    private MainActivity mActivity;
    private GVRContext mContext;

    private GVRAnimationEngine mAnimationEngine;
    private GVRScene mMainScene;

    private List<Star> starList;
    private List<GVRAnimation> unzoomAnimationList = new ArrayList<>();

    GVRTextViewSceneObject textView;


    private GVRSceneObject asyncSceneObject(GVRContext context,
            String textureName) throws IOException {
        return new GVRSceneObject(context, //
                new GVRAndroidResource(context, "sphere.obj"), //
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
        loadStars(starList);

        /*
        Star s1 = new Star();
        starList.add(s1);
        s1.x = 10;
        s1.y = 10;
        s1.z = -10;

        s1 = new Star();
        starList.add(s1);
        s1.x = -10;
        s1.y = 10;
        s1.z = -10;

        s1 = new Star();
        starList.add(s1);
        s1.x = -10;
        s1.y = -10;
        s1.z = -10;

        s1 = new Star();
        starList.add(s1);
        s1.x = 10;
        s1.y = -10;
        s1.z = -10;


        s1 = new Star();
        starList.add(s1);
        s1.x = 10;
        s1.y = 10;
        s1.z = 10;

        s1 = new Star();
        starList.add(s1);
        s1.x = -10;
        s1.y = 10;
        s1.z = 10;

        s1 = new Star();
        starList.add(s1);
        s1.x = -10;
        s1.y = -10;
        s1.z = 10;

        s1 = new Star();
        starList.add(s1);
        s1.x = 10;
        s1.y = -10;
        s1.z = 10;
*/

        mMainScene = gvrContext.getNextMainScene(new Runnable() {
            @Override
            public void run() {
                for (GVRAnimation animation : mAnimations) {
                    animation.start(mAnimationEngine);
                }
                mAnimations = null;
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
        headTracker.getRenderData().setRenderingOrder(100000);
        mMainScene.getMainCameraRig().addChildObject(headTracker);

        textView = new GVRTextViewSceneObject(gvrContext, mActivity);
        textView.getTransform().setPosition(0.0f, -3.0f, -10.0f);
        textView.setTextSize(textView.getTextSize());
        textView.setText("Foooo!!!!");
        textView.setTextColor(Color.CYAN);
        textView.setGravity(Gravity.CENTER);
        textView.setRefreshFrequency(GVRTextViewSceneObject.IntervalFrequency.LOW);
        textView.getRenderData().setDepthTest(false);
        textView.getRenderData().setRenderingOrder(100000);
        mMainScene.getMainCameraRig().addChildObject(textView);


        GVRSceneObject solarSystemObject = new GVRSceneObject(gvrContext);
        mMainScene.addSceneObject(solarSystemObject);

        GVRMesh mesh = gvrContext.createQuad(10f, 10f);
        //Future<GVRMesh> futureMesh = new FutureWrapper<>(mesh);
        //GVRTexture gtex = new GVRBitmapTexture(gvrContext, "star2.png");
        Future<GVRTexture> futureTex = gvrContext.loadFutureTexture(new GVRAndroidResource(gvrContext, R.drawable.star2));

        GVRMaterial gmat = new GVRMaterial(gvrContext);
        gmat.setMainTexture(futureTex);


        for (Star star : starList) {
            if (star.mag > 3) {
                continue;
            }

            GVRSceneObject sobj = new GVRSceneObject(gvrContext, mesh);
            sobj.getRenderData().setMaterial(gmat);
            sobj.getRenderData().setDepthTest(false);
            float scale = 1 / (star.mag < 0.5f ? 0.5f : star.mag);
            sobj.getTransform().setScale(scale, scale, scale);
            setObjectPosition(sobj, star.ra, star.dec, star.dist);
            sobj.setName(star.name == null ? "" : star.name);
            sobj.attachEyePointeeHolder();

            mMainScene.addSceneObject(sobj);
        }

        try {
            Date now = new Date();

            // TODO: fix
            //AstroDate date = new AstroDate(now.getDay(), now.getMonth(), now.getYear());
            Log.d(TAG, ">>>>>>>>>>>>> year: " + now.getYear());
            Log.d(TAG, ">>>>>>>>>>>>> month: " + now.getMonth());
            Log.d(TAG, ">>>>>>>>>>>>> day: " + now.getYear());

            AstroDate date = new AstroDate(24, 10, 2015);
            ObsInfo obsInfo = new ObsInfo();
            double jd = date.jd();

            addPlanetObject(gvrContext, solarSystemObject, jd, obsInfo, Planets.SUN, "gstar.jpg");
            addPlanetObject(gvrContext, solarSystemObject, jd, obsInfo, Planets.LUNA, "moon.jpg");
            addPlanetObject(gvrContext, solarSystemObject, jd, obsInfo, Planets.MERCURY, "mercurymap.jpg");
            addPlanetObject(gvrContext, solarSystemObject, jd, obsInfo, Planets.VENUS, "venusmap.jpg");
            addPlanetObject(gvrContext, solarSystemObject, jd, obsInfo, Planets.MARS, "mars_1k_color.jpg");
            addPlanetObject(gvrContext, solarSystemObject, jd, obsInfo, Planets.JUPITER, "jupiter.jpg");
            addPlanetObject(gvrContext, solarSystemObject, jd, obsInfo, Planets.SATURN, "saturn.jpg");
            addPlanetObject(gvrContext, solarSystemObject, jd, obsInfo, Planets.URANUS, "uranus.jpg");
            addPlanetObject(gvrContext, solarSystemObject, jd, obsInfo, Planets.NEPTUNE, "neptune.jpg");
            addPlanetObject(gvrContext, solarSystemObject, jd, obsInfo, Planets.PLUTO, "pluto.jpg");
        }catch(Exception e) {
            //
        }
    }

    @Override
    public void onStep() {

        for (GVRPicker.GVRPickedObject pickedObject : GVRPicker.findObjects(mContext.getMainScene())) {
            //mMainScene.addStatMessage("Picked: " + pickedObject.getHitObject().getName());
            textView.setText(pickedObject.getHitObject().getName());


            if (unzoomAnimationList.size() > 0) {
                for (GVRAnimation anim : unzoomAnimationList) {
                    anim.start(mAnimationEngine);
                }
                unzoomAnimationList.clear();
            }

            GVRScaleAnimation anim = new GVRScaleAnimation(pickedObject.getHitObject(), 0.3f, 4.0f);
            anim.start(mAnimationEngine);

            GVRScaleAnimation unanim = new GVRScaleAnimation(pickedObject.getHitObject(), 0.3f, 0.7f);
            unzoomAnimationList.add(unanim);
        }

    }

    void onTap() {
        if (null != mMainScene) {
            // toggle whether stats are displayed.
            boolean statsEnabled = mMainScene.getStatsEnabled();
            mMainScene.setStatsEnabled(!statsEnabled);
        }
    }


    private void setObjectPosition(GVRSceneObject obj, double ra, double dec, float dist) {
        obj.getTransform().setPosition(0, 0, -dist);
        obj.getTransform().rotateByAxisWithPivot((float) ra, 0, 1, 0, 0, 0, 0);
        float x1 = 1, z1 = 0;
        float c = (float) Math.cos(Math.toRadians(ra));
        float s = (float) Math.sin(Math.toRadians(ra));
        float xnew = x1 * c - z1 * s;
        float znew = x1 * s + z1 * c;
        obj.getTransform().rotateByAxisWithPivot((float) -dec, xnew, 0, znew, 0, 0, 0);
    }

    private void addPlanetObject(GVRContext context, GVRSceneObject parentObj, double julianDate,
                                 ObsInfo obsInfo, int planetID,
                                 String planetTexture) throws IOException, NoInitException {
        PlanetData data = new PlanetData(planetID, julianDate, obsInfo);
        GVRSceneObject planetRevolutionObject = new GVRSceneObject(context);
        setObjectPosition(planetRevolutionObject, Math.toDegrees(data.getRightAscension()), Math.toDegrees(data.getDeclination()), 50);
        parentObj.addChildObject(planetRevolutionObject);

        GVRSceneObject planetRotationObject = new GVRSceneObject(context);
        planetRevolutionObject.addChildObject(planetRotationObject);
        GVRSceneObject planetMeshObject = asyncSceneObject(context, planetTexture);

        planetMeshObject.getTransform().setScale(1.0f, 1.0f, 1.0f);
        planetRotationObject.addChildObject(planetMeshObject);
        counterClockwise(planetRotationObject, 10f);
        planetMeshObject.attachEyePointeeHolder();
        planetMeshObject.setName(planetTexture);
    }


    private List<GVRAnimation> mAnimations = new ArrayList<>();

    private void setup(GVRAnimation animation) {
        animation.setRepeatMode(GVRRepeatMode.REPEATED).setRepeatCount(-1);
        mAnimations.add(animation);
    }

    private void counterClockwise(GVRSceneObject object, float duration) {
        setup(new GVRRotationByAxisWithPivotAnimation( //
                object, duration, 360.0f, //
                0.0f, 1.0f, 0.0f, //
                0.0f, 0.0f, 0.0f));
    }

    private void clockwise(GVRSceneObject object, float duration) {
        setup(new GVRRotationByAxisWithPivotAnimation( //
                object, duration, -360.0f, //
                0.0f, 1.0f, 0.0f, //
                0.0f, 0.0f, 0.0f));
    }

    private void clockwise(GVRTransform transform, float duration) {
        setup(new GVRRotationByAxisWithPivotAnimation( //
                transform, duration, -360.0f, //
                0.0f, 1.0f, 0.0f, //
                0.0f, 0.0f, 0.0f));
    }



    public static class Star {
        public float x;
        public float y;
        public float z;
        public double ra;
        public double dec;
        public float dist;
        public float mag;
        public int index;
        public String type;
        public String name;
    }

    private void loadStars(List<Star> starList) {
        InputStream instream = null;
        try {
            Log.d(TAG, "Loading stars...");
            instream = new FileInputStream(Environment.getExternalStorageDirectory().getPath() + "/stars2.txt");
            BufferedReader buffreader = new BufferedReader(new InputStreamReader(instream));
            String line;
            String[] lineArr;
            buffreader.readLine();
            while ((line = buffreader.readLine()) != null) {
                lineArr = line.split("\\s+");

                Star s = new Star();
                starList.add(s);
                s.index = Integer.parseInt(lineArr[0]);

                s.ra = Double.parseDouble(lineArr[1]);
                s.dec = Double.parseDouble(lineArr[2]);
                s.dist = (float)Double.parseDouble(lineArr[3]);

                // TEMP: make it a fixed distance for now
                s.dist = 500;

                s.mag = Float.parseFloat(lineArr[4]);
                s.type = lineArr[5];

                s.x = (float) ((s.dist * Math.cos(s.dec)) * Math.cos(s.ra));
                s.y = (float) ((s.dist * Math.cos(s.dec)) * Math.sin(s.ra));
                s.z = (float) (s.dist * Math.sin(s.dec));
            }
        } catch (IOException e) {
            Log.e(TAG, "Failed to read star database.", e);
        } finally {
            if (instream != null) {
                try { instream.close(); instream = null; }
                catch(Exception e) {
                    //
                }
            }
        }
        try {
            Log.d(TAG, "Loading star names...");
            instream = new FileInputStream(Environment.getExternalStorageDirectory().getPath() + "/starnames.dat");
            BufferedReader buffreader = new BufferedReader(new InputStreamReader(instream));
            String line;
            String[] lineArr;
            buffreader.readLine();
            while ((line = buffreader.readLine()) != null) {
                lineArr = line.split(":");
                int index = Integer.parseInt(lineArr[0]);

                for (Star star : starList) {
                    if (star.index == index) {
                        star.name = lineArr[1];
                    }
                }

            }
        } catch (IOException e) {
            Log.e(TAG, "Failed to read star names.", e);
        } finally {
            if (instream != null) {
                try { instream.close(); }
                catch(Exception e) {
                    //
                }
            }
        }
    }

}
