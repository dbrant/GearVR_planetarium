/* Copyright 2016 Dmitry Brant
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

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRSphereCollider;
import org.gearvrf.utility.Log;

public class StarLoader {
    private static final String TAG = "StarLoader";
    public static final float MAX_STAR_MAGNITUDE = 5f;
    public static final float DEFAULT_DISTANCE_STAR = 500f;

    private static GVRMesh starMesh;

    public GVRSceneObject createSceneObject(GVRContext context, SkyObject obj, String name) {
        GVRSceneObject sobj = new GVRSceneObject(context, getStarMesh(context));
        final float scale = 6.0f;
        obj.sceneObj = sobj;
        obj.initialScale = scale;
        sobj.getTransform().setScale(scale, scale, scale);
        sobj.setName(name);

        GVRSphereCollider c = new GVRSphereCollider(context);
        c.setRadius(1f);
        sobj.attachComponent(c);
        return sobj;
    }

    public void loadStars(Context context, List<SkyObject> starList) {
        InputStream instream = null;
        try {
            Log.d(TAG, "Loading stars...");
            instream = context.getAssets().open("stars.txt");
            BufferedReader buffreader = new BufferedReader(new InputStreamReader(instream));
            String line;
            String[] lineArr;
            buffreader.readLine();
            while ((line = buffreader.readLine()) != null) {
                lineArr = line.split("\\s+");

                SkyObject s = new SkyObject();
                starList.add(s);
                s.type = SkyObject.TYPE_STAR;
                s.hipNum = Integer.parseInt(lineArr[0]);

                s.ra = Double.parseDouble(lineArr[1]);
                s.dec = Double.parseDouble(lineArr[2]);
                s.dist = (float)Double.parseDouble(lineArr[3]);

                s.mag = Float.parseFloat(lineArr[4]);
                s.className = lineArr[5];
                s.name = "HIP " + s.hipNum;

                // for converting to (x,y,z):
                //s.x = (float) ((s.dist * Math.cos(s.dec)) * Math.cos(s.ra));
                //s.y = (float) ((s.dist * Math.cos(s.dec)) * Math.sin(s.ra));
                //s.z = (float) (s.dist * Math.sin(s.dec));
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
            instream = context.getAssets().open("starnames.txt");
            BufferedReader buffreader = new BufferedReader(new InputStreamReader(instream));
            String line;
            String[] lineArr;
            buffreader.readLine();
            while ((line = buffreader.readLine()) != null) {
                lineArr = line.split(":");
                int index = Integer.parseInt(lineArr[0]);
                for (SkyObject star : starList) {
                    if (star.hipNum == index) {
                        star.name = Util.transformStarName(lineArr[1]);
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

    private GVRMesh getStarMesh(GVRContext context) {
        if (starMesh == null) {
            starMesh = new GVRMesh(context);
            starMesh.setVertices(new float[]{-1f, -1f, 0f, 1f, 1f, 0f});
        }
        return starMesh;
    }
}
