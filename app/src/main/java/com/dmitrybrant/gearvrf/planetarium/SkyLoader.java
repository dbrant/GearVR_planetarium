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

import java.io.IOException;

public class SkyLoader {
    private static GVRMesh skyMesh;

    public static GVRSceneObject createSceneObject(GVRContext context, int renderOrder) throws IOException {
        GVRSceneObject sceneObj = new GVRSceneObject(context,
                getSkyMesh(context),
                context.loadTexture(new GVRAndroidResource(context, "skymap_8k.jpg")));

        sceneObj.getTransform().setScale(500, 500, 500);
        sceneObj.getTransform().setPosition(0, 0, 0);
        sceneObj.getTransform().setRotationByAxis(-90f, 0, 1, 0);
        sceneObj.getRenderData().setDepthTest(false);
        sceneObj.getRenderData().setRenderingOrder(renderOrder);
        sceneObj.setPickingEnabled(false);
        return sceneObj;
    }

    private static GVRMesh getSkyMesh(GVRContext context) throws IOException {
        if (skyMesh == null) {
            final float radius = 1f;
            final int numRings = 48;
            final int numSections = 48;
            float R = 1f / (float) (numRings - 1);
            float S = 1f / (float) (numSections - 1);
            float x, y, z;

            skyMesh = new GVRMesh(context);
            float[] vertices = new float[numRings * numSections * 3];
            float[] normals = new float[numRings * numSections * 3];
            float[] texCoords = new float[numRings * numSections * 2];
            char[] indices = new char[numRings * numSections * 3 * 2];

            int vIndex = 0;
            int nIndex = 0;
            int tIndex = 0;

            for (int r = 0; r < numRings; r++)
            {
                for (int s = 0; s < numSections; s++)
                {
                    y = (float) Math.sin(-(Math.PI / 2f) + Math.PI * r * R);
                    x = (float) (Math.cos(2*Math.PI * s * S) * Math.sin(Math.PI * r * R));
                    z = (float) (Math.sin(2*Math.PI * s * S) * Math.sin(Math.PI * r * R));

                    texCoords[tIndex++] = s * S;
                    texCoords[tIndex++] = r * R;

                    vertices[vIndex++] = x * radius;
                    vertices[vIndex++] = y * radius;
                    vertices[vIndex++] = z * radius;

                    normals[nIndex++] = x;
                    normals[nIndex++] = y;
                    normals[nIndex++] = z;
                }
            }

            int iIndex = 0;
            for (int r = 0; r < numRings - 1; r++)
            {
                for (int s = 0; s < numSections - 1; s++)
                {
                    int curRow = r * numSections;
                    int nextRow = (r+1) * numSections;
                    indices[iIndex++] = (char) (nextRow + (s + 1));
                    indices[iIndex++] = (char) (nextRow + s);
                    indices[iIndex++] = (char) (curRow + s);
                    indices[iIndex++] = (char) (curRow + (s + 1));
                    indices[iIndex++] = (char) (nextRow + (s + 1));
                    indices[iIndex++] = (char) (curRow + s);
                }
            }

            skyMesh.setVertices(vertices);
            skyMesh.setTexCoords(texCoords);
            skyMesh.setNormals(normals);
            skyMesh.setIndices(indices);
        }
        return skyMesh;
    }
}
