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

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRSceneObject;

import java.util.List;

public class NebulaLoader {
    private static final float DEFAULT_DISTANCE_NEBULA = 550f;
    private static final float DEFAULT_SCALE = 3f;

    private static GVRMesh nebulaMesh;

    public static GVRSceneObject createSceneObject(GVRContext context, SkyObject obj, String name) {
        if (nebulaMesh == null) {
            nebulaMesh = context.createQuad(10f, 10f);
        }
        GVRSceneObject sobj = new GVRSceneObject(context, nebulaMesh,
                context.loadTexture(new GVRAndroidResource(context, obj.texResId)));
        obj.sceneObj = sobj;
        sobj.getRenderData().setDepthTest(false);
        sobj.getTransform().setScale(obj.initialScale, obj.initialScale, obj.initialScale);
        sobj.attachEyePointeeHolder();
        sobj.setName(name);
        return sobj;
    }

    public static void loadNebulae(GVRContext context, List<SkyObject> objectList) {
        addNebula(context, objectList, R.drawable.m1, Util.hmsToDec(5f, 34f, 31.94f), Util.dmsToDec(22f, 0f, 52.2f), DEFAULT_SCALE, R.string.m1);
        addNebula(context, objectList, R.drawable.m8, Util.hmsToDec(18f, 3f, 37f), Util.dmsToDec(-24f, 23f, 12f), DEFAULT_SCALE, R.string.m8);
        addNebula(context, objectList, R.drawable.m13, Util.hmsToDec(16f, 41f, 41.24f), Util.dmsToDec(36f, 27f, 35.5f), DEFAULT_SCALE, R.string.m13);
        addNebula(context, objectList, R.drawable.m16, Util.hmsToDec(18f, 18f, 48f), Util.dmsToDec(-13f, 49f, 0f), DEFAULT_SCALE, R.string.m16);
        addNebula(context, objectList, R.drawable.m27, Util.hmsToDec(19f, 59f, 36.34f), Util.dmsToDec(-22f, 43f, 16.09f), DEFAULT_SCALE, R.string.m27);
        addNebula(context, objectList, R.drawable.m31, Util.hmsToDec(0f, 41.8f, 0f), Util.dmsToDec(41f, 16f, 0f), DEFAULT_SCALE, R.string.m31);
        addNebula(context, objectList, R.drawable.m42, Util.hmsToDec(5f, 35f, 17.3f), Util.dmsToDec(-5f, 23f, 28f), DEFAULT_SCALE, R.string.m42);
        addNebula(context, objectList, R.drawable.m51, Util.hmsToDec(13f, 30f, 0f), Util.dmsToDec(47f, 11f, 0f), DEFAULT_SCALE, R.string.m51);
        addNebula(context, objectList, R.drawable.m57, Util.hmsToDec(18f, 53.6f, 0f), Util.dmsToDec(33f, 2f, 0f), DEFAULT_SCALE, R.string.m57);
        addNebula(context, objectList, R.drawable.m63, Util.hmsToDec(13f, 15f, 49.3f), Util.dmsToDec(42f, 1f, 45f), DEFAULT_SCALE, R.string.m63);
        addNebula(context, objectList, R.drawable.m101, Util.hmsToDec(14f, 3.2f, 0f), Util.dmsToDec(54f, 21f, 0f), DEFAULT_SCALE, R.string.m101);
        addNebula(context, objectList, R.drawable.m104, Util.hmsToDec(12f, 39f, 59.4f), Util.dmsToDec(-11f, 37f, 23f), DEFAULT_SCALE, R.string.m104);
        addNebula(context, objectList, R.drawable.ic443, Util.hmsToDec(6f, 17f, 13f), Util.dmsToDec(22f, 31f, 5f), DEFAULT_SCALE, R.string.ic443);
        addNebula(context, objectList, R.drawable.etacarinae, Util.hmsToDec(10f, 45f, 3.591f), Util.dmsToDec(-59f, 41f, 4.26f), DEFAULT_SCALE, R.string.eta_carinae);
    }

    private static void addNebula(GVRContext context, List<SkyObject> objectList, int texResId, float ra, float dec, float initialScale, int nameResId) {
        SkyObject obj = new SkyObject();
        objectList.add(obj);
        obj.type = SkyObject.TYPE_NEBULA;
        obj.dist = DEFAULT_DISTANCE_NEBULA;
        obj.initialScale = initialScale;
        obj.name = context.getContext().getString(nameResId);
        obj.texResId = texResId;
        obj.ra = ra;
        obj.dec = dec;
    }
}
