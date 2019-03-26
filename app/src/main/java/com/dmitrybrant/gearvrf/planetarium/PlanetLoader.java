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

import com.mhuss.AstroLib.AstroDate;
import com.mhuss.AstroLib.NoInitException;
import com.mhuss.AstroLib.ObsInfo;
import com.mhuss.AstroLib.PlanetData;
import com.mhuss.AstroLib.Planets;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRSceneObject;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;

public class PlanetLoader {
    public static final float DEFAULT_DISTANCE_PLANET = 50f;

    private static GVRMesh planetMesh;

    public static GVRSceneObject createSceneObject(GVRContext context, SkyObject obj, String name, int renderOrder) throws IOException {
        GVRSceneObject planetRevolutionObject = new GVRSceneObject(context);
        obj.sceneObj = planetRevolutionObject;

        GVRSceneObject planetRotationObject = new GVRSceneObject(context);
        planetRevolutionObject.addChildObject(planetRotationObject);

        GVRSceneObject planetMeshObject = new GVRSceneObject(context,
                getPlanetMesh(context),
                context.getAssetLoader().loadTexture(new GVRAndroidResource(context, obj.texName)));

        planetRotationObject.addChildObject(planetMeshObject);
        planetMeshObject.getTransform().setScale(obj.initialScale, obj.initialScale, obj.initialScale);
        planetMeshObject.getRenderData().setDepthTest(true);
        planetMeshObject.getRenderData().setRenderingOrder(renderOrder);
        planetMeshObject.setPickingEnabled(true);
        planetMeshObject.setName(name);

        // TODO: implement correct lighting
        if (!obj.name.equals("Sun")) {
            //planetMeshObject.getRenderData().enableLight();
            planetMeshObject.getRenderData().getMaterial().setColor(1f, 1f, 1f);
            planetMeshObject.getRenderData().getMaterial().setOpacity(1.0f);
            planetMeshObject.getRenderData().getMaterial().setAmbientColor(0.1f, 0.1f, 0.1f, 1.0f);
            planetMeshObject.getRenderData().getMaterial().setDiffuseColor(1.0f, 1.0f, 1.0f, 1.0f);
        }

        if (obj.name.equals("Moon")) {
            planetMeshObject.getTransform().rotateByAxis(70f, 0f, 1f, 0f);
        }
        return planetRevolutionObject;
    }

    private static GVRMesh getPlanetMesh(GVRContext context) throws IOException {
        if (planetMesh == null) {
            planetMesh = SphereMesh.createSphereMesh(context, 1f, 16, 16, false);
        }
        return planetMesh;
    }

    public static void addRings(GVRContext context, SkyObject obj, float innerRadius, float outerRadius, float rotation, int ringResId, int renderOrder) {
        GVRMesh ringMesh = RingMesh.createRingMesh(context, innerRadius, outerRadius, 32);
        GVRSceneObject ringObj = new GVRSceneObject(context, ringMesh, context.getAssetLoader().loadTexture(new GVRAndroidResource(context, ringResId)));

        ringObj.getTransform().rotateByAxis(-90f, 1f, 0f, 0f);
        ringObj.getRenderData().setDepthTest(true);
        ringObj.getRenderData().setRenderingOrder(renderOrder);

        ringObj.getRenderData().getMaterial().setColor(1f, 1f, 1f);
        ringObj.getRenderData().getMaterial().setOpacity(1.0f);

        obj.sceneObj.getChildByIndex(0).getChildByIndex(0).addChildObject(ringObj);
        obj.sceneObj.getChildByIndex(0).getChildByIndex(0).getTransform().rotateByAxis(rotation, 1f, 0f, 0f);
    }

    public static void loadPlanets(GVRContext context, List<SkyObject> objectList) {
        try {
            Calendar calendar = Calendar.getInstance();
            AstroDate date = new AstroDate(calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.HOUR), calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND));
            ObsInfo obsInfo = new ObsInfo();
            double jd = date.jd();

            addPlanet(context, objectList, jd, obsInfo, Planets.SUN, "gstar.jpg", R.string.sun);
            addPlanet(context, objectList, jd, obsInfo, Planets.LUNA, "moon.jpg", R.string.moon);
            addPlanet(context, objectList, jd, obsInfo, Planets.MERCURY, "mercurymap.jpg", R.string.mercury);
            addPlanet(context, objectList, jd, obsInfo, Planets.VENUS, "venus.jpg", R.string.venus);
            addPlanet(context, objectList, jd, obsInfo, Planets.MARS, "mars_1k_color.jpg", R.string.mars);
            addPlanet(context, objectList, jd, obsInfo, Planets.JUPITER, "jupiter.jpg", R.string.jupiter);
            addPlanet(context, objectList, jd, obsInfo, Planets.SATURN, "saturn.jpg", R.string.saturn);
            addPlanet(context, objectList, jd, obsInfo, Planets.URANUS, "uranus.jpg", R.string.uranus);
            addPlanet(context, objectList, jd, obsInfo, Planets.NEPTUNE, "neptune.jpg", R.string.neptune);
            addPlanet(context, objectList, jd, obsInfo, Planets.PLUTO, "pluto.jpg", R.string.pluto);

        } catch (Exception e) {
            //
        }
    }

    private static void addPlanet(GVRContext context, List<SkyObject> objectList, double julianDate,
                                  ObsInfo obsInfo, int planetID, String texName,
                                  int nameResId) throws NoInitException {
        PlanetData data = new PlanetData(planetID, julianDate, obsInfo);
        SkyObject obj = new SkyObject();
        objectList.add(obj);
        obj.type = SkyObject.TYPE_PLANET;
        obj.dist = (float)data.getEquatorialRadius();
        obj.initialScale = 1f;
        obj.name = context.getContext().getString(nameResId);
        obj.texName = texName;
        obj.ra = Math.toDegrees(data.getRightAscension());
        obj.dec = Math.toDegrees(data.getDeclination());
    }
}
