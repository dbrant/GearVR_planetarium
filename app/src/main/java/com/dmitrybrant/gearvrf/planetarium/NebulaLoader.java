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
import org.gearvrf.utility.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public class NebulaLoader {
    private static final String TAG = "NebulaLoader";
    private static final float DEFAULT_DISTANCE_NEBULA = 550f;
    private static final float DEFAULT_SCALE = 3f;

    private static GVRMesh nebulaMesh;

    public static GVRSceneObject createSceneObject(GVRContext context, SkyObject obj, String name) {
        if (nebulaMesh == null) {
            nebulaMesh = context.createQuad(10f, 10f);
        }
        GVRSceneObject sobj = new GVRSceneObject(context, nebulaMesh,
                context.getAssetLoader().loadTexture(new GVRAndroidResource(context, obj.texResId)));
        obj.sceneObj = sobj;
        sobj.getRenderData().setDepthTest(false);
        sobj.getTransform().setScale(obj.initialScale, obj.initialScale, obj.initialScale);
        sobj.setPickingEnabled(true);
        sobj.setName(name);
        return sobj;
    }

    public static void loadNebulae(GVRContext context, List<SkyObject> objectList) {

        InputStream instream = null;
        try {
            Log.d(TAG, "Loading nebulae...");
            instream = context.getContext().getAssets().open("nebulae.txt");
            BufferedReader buffreader = new BufferedReader(new InputStreamReader(instream));
            String line;
            String[] lineArr;
            buffreader.readLine();
            while ((line = buffreader.readLine()) != null) {
                if (line.startsWith("#")) {
                    continue;
                }
                lineArr = line.split(",");
                if (lineArr.length < 8) {
                    continue;
                }

                addNebula(objectList,
                        context.getContext().getResources().getIdentifier(lineArr[0].trim(), "drawable", context.getContext().getPackageName()),
                        Util.hmsToDec(Float.parseFloat(lineArr[1]), Float.parseFloat(lineArr[2]), Float.parseFloat(lineArr[3])),
                        Util.dmsToDec(Float.parseFloat(lineArr[4]), Float.parseFloat(lineArr[5]), Float.parseFloat(lineArr[6])),
                        DEFAULT_SCALE,
                        lineArr[7].trim());
            }
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

    private static void addNebula(List<SkyObject> objectList, int texResId, float ra, float dec, float initialScale, String name) {
        SkyObject obj = new SkyObject();
        objectList.add(obj);
        obj.type = SkyObject.TYPE_NEBULA;
        obj.dist = DEFAULT_DISTANCE_NEBULA;
        obj.initialScale = initialScale;
        obj.name = name;
        obj.texResId = texResId;
        obj.ra = ra;
        obj.dec = dec;
    }
}
