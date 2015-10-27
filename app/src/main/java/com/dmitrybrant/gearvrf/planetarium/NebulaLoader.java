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

import org.gearvrf.GVRContext;

import java.util.List;

public class NebulaLoader {
    private static final float DEFAULT_DISTANCE_NEBULA = 550f;

    public static void loadNebulae(GVRContext context, List<SkyObject> objectList) {
        addNebula(context, objectList, R.drawable.m1, Util.hmsToDec(5f, 34f, 31.94f), Util.dmsToDec(22f, 0f, 52.2f), 5f, R.string.m1);
        addNebula(context, objectList, R.drawable.m8, Util.hmsToDec(18f, 3f, 37f), Util.dmsToDec(-24f, 23f, 12f), 5f, R.string.m8);
        addNebula(context, objectList, R.drawable.m13, Util.hmsToDec(16f, 41f, 41.24f), Util.dmsToDec(36f, 27f, 35.5f), 5f, R.string.m13);
        addNebula(context, objectList, R.drawable.m16, Util.hmsToDec(18f, 18f, 48f), Util.dmsToDec(-13f, 49f, 0f), 5f, R.string.m16);
        addNebula(context, objectList, R.drawable.m27, Util.hmsToDec(19f, 59f, 36.34f), Util.dmsToDec(-22f, 43f, 16.09f), 5f, R.string.m27);
        addNebula(context, objectList, R.drawable.m31, Util.hmsToDec(0f, 41.8f, 0f), Util.dmsToDec(41f, 16f, 0f), 10f, R.string.m31);
        addNebula(context, objectList, R.drawable.m42, Util.hmsToDec(5f, 35f, 17.3f), Util.dmsToDec(-5f, 23f, 28f), 5f, R.string.m42);
        addNebula(context, objectList, R.drawable.m51, Util.hmsToDec(13f, 30f, 0f), Util.dmsToDec(47f, 11f, 0f), 5f, R.string.m51);
        addNebula(context, objectList, R.drawable.m57, Util.hmsToDec(18f, 53.6f, 0f), Util.dmsToDec(33f, 2f, 0f), 10f, R.string.m57);
        addNebula(context, objectList, R.drawable.m101, Util.hmsToDec(14f, 3.2f, 0f), Util.dmsToDec(54f, 21f, 0f), 5f, R.string.m101);
        addNebula(context, objectList, R.drawable.etacarinae, Util.hmsToDec(10f, 45f, 3.591f), Util.dmsToDec(-59f, 41f, 4.26f), 5f, R.string.eta_carinae);
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
