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

import java.util.List;

public class OtherObjLoader {
    public static final float DEFAULT_DISTANCE_OTHER = 550f;

    public static void loadObjects(List<SkyObject> objectList) {

        addObject(objectList, 0f, -89f, R.drawable.info, "About...");

    }

    private static void addObject(List<SkyObject> objectList, float ra, float dec, int texResId, String name) {
        SkyObject obj = new SkyObject();
        objectList.add(obj);
        obj.type = SkyObject.TYPE_OTHER;
        obj.dist = DEFAULT_DISTANCE_OTHER;
        obj.initialScale = 2f;
        obj.name = name;
        obj.texResId = texResId;
        obj.ra = ra;
        obj.dec = dec;
    }
}
