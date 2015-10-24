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

import android.os.Environment;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRBitmapTexture;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMaterialShaderId;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRScript;
import org.gearvrf.GVRStockMaterialShaderId;
import org.gearvrf.GVRTransform;
import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.animation.GVRAnimationEngine;
import org.gearvrf.animation.GVRRepeatMode;
import org.gearvrf.animation.GVRRotationByAxisWithPivotAnimation;
import org.gearvrf.scene_objects.GVRCubeSceneObject;
import org.gearvrf.scene_objects.GVRSphereSceneObject;
import org.gearvrf.utility.Log;

public class PlanetariumViewManager extends GVRScript {

    @SuppressWarnings("unused")
    private static final String TAG = Log.tag(PlanetariumViewManager.class);

    private GVRAnimationEngine mAnimationEngine;
    private GVRScene mMainScene;

    private List<Star> starList;

    private GVRSceneObject asyncSceneObject(GVRContext context,
            String textureName) throws IOException {
        return new GVRSceneObject(context, //
                new GVRAndroidResource(context, "sphere.obj"), //
                new GVRAndroidResource(context, textureName));
    }

    @Override
    public void onInit(GVRContext gvrContext) throws IOException {
        mAnimationEngine = gvrContext.getAnimationEngine();

        starList = new ArrayList<>();
        loadStars(starList);

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

        mMainScene.getMainCameraRig().getLeftCamera()
                .setBackgroundColor(0.0f, 0.0f, 0.0f, 1.0f);
        mMainScene.getMainCameraRig().getRightCamera()
                .setBackgroundColor(0.0f, 0.0f, 0.0f, 1.0f);

        mMainScene.getMainCameraRig().getTransform()
                .setPosition(0.0f, 0.0f, 0.0f);

        GVRSceneObject solarSystemObject = new GVRSceneObject(gvrContext);
        mMainScene.addSceneObject(solarSystemObject);



        GVRMaterial gmat = new GVRMaterial(gvrContext);
        GVRBitmapTexture gtex = new GVRBitmapTexture(gvrContext, 3, 3, new byte[] {(byte)128,(byte)128,(byte)128,(byte)128,(byte)128,(byte)128,(byte)128,(byte)128,(byte)128});
        gmat.setMainTexture(gtex);
        gmat.setColor(0xffffff);

        for (Star s : starList) {
            if (s.mag > 5) {
                continue;
            }

            //GVRSphereSceneObject sobj = new GVRSphereSceneObject(gvrContext, 3, 3);
            //GVRCubeSceneObject sobj = new GVRCubeSceneObject(gvrContext);
            GVRSceneObject sobj = new GVRSceneObject(gvrContext, 3, 3, gtex);
            sobj.getTransform().setPosition(s.x, s.y, s.z);
            sobj.getTransform().setRotation(0, 0, 0, 0);


            


            //sobj.getRenderData().setMaterial(gmat);
            mMainScene.addSceneObject(sobj);
        }




        GVRSceneObject earthRevolutionObject = new GVRSceneObject(gvrContext);
        earthRevolutionObject.getTransform().setPosition(22.0f, 0.0f, 0.0f);
        solarSystemObject.addChildObject(earthRevolutionObject);

        GVRSceneObject earthRotationObject = new GVRSceneObject(gvrContext);
        earthRevolutionObject.addChildObject(earthRotationObject);

        GVRSceneObject earthMeshObject = asyncSceneObject(gvrContext,
                "earthmap1k.jpg");
        earthMeshObject.getTransform().setScale(1.0f, 1.0f, 1.0f);
        earthRotationObject.addChildObject(earthMeshObject);

        counterClockwise(earthRotationObject, 1.5f);









/*

        GVRSceneObject sunRotationObject = new GVRSceneObject(gvrContext);
        solarSystemObject.addChildObject(sunRotationObject);

        GVRSceneObject sunMeshObject = asyncSceneObject(gvrContext, "sunmap.astc");
        sunMeshObject.getTransform().setPosition(0.0f, 0.0f, 0.0f);
        sunMeshObject.getTransform().setScale(10.0f, 10.0f, 10.0f);
        sunRotationObject.addChildObject(sunMeshObject);


        GVRSceneObject mercuryRevolutionObject = new GVRSceneObject(gvrContext);
        mercuryRevolutionObject.getTransform().setPosition(14.0f, 0.0f, 0.0f);
        solarSystemObject.addChildObject(mercuryRevolutionObject);

        GVRSceneObject mercuryRotationObject = new GVRSceneObject(gvrContext);
        mercuryRevolutionObject.addChildObject(mercuryRotationObject);

        GVRSceneObject mercuryMeshObject = asyncSceneObject(gvrContext,
                "mercurymap.jpg");
        mercuryMeshObject.getTransform().setScale(0.3f, 0.3f, 0.3f);
        mercuryRotationObject.addChildObject(mercuryMeshObject);

        GVRSceneObject venusRevolutionObject = new GVRSceneObject(gvrContext);
        venusRevolutionObject.getTransform().setPosition(17.0f, 0.0f, 0.0f);
        solarSystemObject.addChildObject(venusRevolutionObject);

        GVRSceneObject venusRotationObject = new GVRSceneObject(gvrContext);
        venusRevolutionObject.addChildObject(venusRotationObject);

        GVRSceneObject venusMeshObject = asyncSceneObject(gvrContext,
                "venusmap.jpg");
        venusMeshObject.getTransform().setScale(0.8f, 0.8f, 0.8f);
        venusRotationObject.addChildObject(venusMeshObject);

        GVRSceneObject earthRevolutionObject = new GVRSceneObject(gvrContext);
        earthRevolutionObject.getTransform().setPosition(22.0f, 0.0f, 0.0f);
        solarSystemObject.addChildObject(earthRevolutionObject);

        GVRSceneObject earthRotationObject = new GVRSceneObject(gvrContext);
        earthRevolutionObject.addChildObject(earthRotationObject);

        GVRSceneObject earthMeshObject = asyncSceneObject(gvrContext,
                "earthmap1k.jpg");
        earthMeshObject.getTransform().setScale(1.0f, 1.0f, 1.0f);
        earthRotationObject.addChildObject(earthMeshObject);

        GVRSceneObject moonRevolutionObject = new GVRSceneObject(gvrContext);
        moonRevolutionObject.getTransform().setPosition(4.0f, 0.0f, 0.0f);
        earthRevolutionObject.addChildObject(moonRevolutionObject);
        moonRevolutionObject.addChildObject(mMainScene.getMainCameraRig());

        GVRSceneObject marsRevolutionObject = new GVRSceneObject(gvrContext);
        marsRevolutionObject.getTransform().setPosition(30.0f, 0.0f, 0.0f);
        solarSystemObject.addChildObject(marsRevolutionObject);

        GVRSceneObject marsRotationObject = new GVRSceneObject(gvrContext);
        marsRevolutionObject.addChildObject(marsRotationObject);

        GVRSceneObject marsMeshObject = asyncSceneObject(gvrContext,
                "mars_1k_color.jpg");
        marsMeshObject.getTransform().setScale(0.6f, 0.6f, 0.6f);
        marsRotationObject.addChildObject(marsMeshObject);

        counterClockwise(sunRotationObject, 50f);

        counterClockwise(mercuryRevolutionObject, 150f);
        counterClockwise(mercuryRotationObject, 100f);

        counterClockwise(venusRevolutionObject, 400f);
        clockwise(venusRotationObject, 400f);

        counterClockwise(earthRevolutionObject, 600f);
        counterClockwise(earthRotationObject, 1.5f);

        counterClockwise(moonRevolutionObject, 60f);

        //clockwise(mMainScene.getMainCameraRig().getTransform(), 60f);

        counterClockwise(marsRevolutionObject, 1200f);
        counterClockwise(marsRotationObject, 200f);
        */

    }

    @Override
    public void onStep() {
    }

    void onTap() {
        if (null != mMainScene) {
            // toggle whether stats are displayed.
            boolean statsEnabled = mMainScene.getStatsEnabled();
            mMainScene.setStatsEnabled(!statsEnabled);
        }
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
        public float mag;
        public int index;
        public String type;
    }

    private void loadStars(List<Star> starList) {
        InputStream instream = null;
        try {
            Log.d(TAG, "Loading stars...");
            instream = new FileInputStream(Environment.getExternalStorageDirectory().getPath() + "/stars.txt");
            BufferedReader buffreader = new BufferedReader(new InputStreamReader(instream));
            String line;
            String[] lineArr;
            buffreader.readLine();
            while ((line = buffreader.readLine()) != null) {
                lineArr = line.split("\\s+");

                Star s = new Star();
                starList.add(s);
                s.index = Integer.parseInt(lineArr[0]);

                double ra = Double.parseDouble(lineArr[1]);
                double dec = Double.parseDouble(lineArr[2]);
                double dist = Double.parseDouble(lineArr[3]);

                // TEMP: set distance to fixed value
                dist = 1000;

                s.mag = Float.parseFloat(lineArr[4]);
                s.type = lineArr[5];

                s.x = (float) ((dist * Math.cos(dec)) * Math.cos(ra));
                s.y = (float) ((dist * Math.cos(dec)) * Math.sin(ra));
                s.z = (float) (dist * Math.sin(dec));
            };
        } catch (IOException e) {
            Log.e(TAG, "Failed to read star database.", e);
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
