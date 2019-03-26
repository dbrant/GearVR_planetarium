/* Copyright 2015-2019 Dmitry Brant
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
import org.gearvrf.GVRMesh;

public class RingMesh {

    /**
     * Create a ring mesh in the X-Y plane.
     * @param context Context to work with.
     * @param innerRadius What it says.
     * @param outerRadius What it says.
     * @param nSections Number of segments in the ring.
     * @return The mesh.
     */
    public static GVRMesh createRingMesh(GVRContext context, float innerRadius, float outerRadius, int nSections) {
        float twopi =  2f * (float) Math.PI;
        GVRMesh mesh = new GVRMesh(context);
        float[] vertices = new float[(nSections + 1) * 3 * 6];
        float[] normals = new float[(nSections + 1) * 3 * 6];
        float[] texCoords = new float[(nSections + 1) * 2 * 6];
        char[] indices = new char[(nSections + 1) * 6];
        int vIndex = 0;
        int tIndex = 0;
        int nIndex = 0;
        int iIndex = 0;
        int triangleIndex = 0;
        for (int i = 0; i < nSections; i++)
        {
            float t = (float) i / (float) nSections;
            float theta = t * twopi;
            float s = (float) Math.sin(theta);
            float c = (float) Math.cos(theta);
            float t1 = (float) (i + 1) / (float) nSections;
            float theta1 = t1 * twopi;
            float s1 = (float) Math.sin(theta1);
            float c1 = (float) Math.cos(theta1);

            texCoords[tIndex++] = 0f;
            texCoords[tIndex++] = 0f;
            texCoords[tIndex++] = 1f;
            texCoords[tIndex++] = 0f;
            texCoords[tIndex++] = 1f;
            texCoords[tIndex++] = 1f;

            vertices[vIndex++] = c * innerRadius;
            vertices[vIndex++] = s * innerRadius;
            vertices[vIndex++] = 0f;
            vertices[vIndex++] = c * outerRadius;
            vertices[vIndex++] = s * outerRadius;
            vertices[vIndex++] = 0f;
            vertices[vIndex++] = c1 * outerRadius;
            vertices[vIndex++] = s1 * outerRadius;
            vertices[vIndex++] = 0f;

            normals[nIndex++] = 0f;
            normals[nIndex++] = 0f;
            normals[nIndex++] = 1f;
            normals[nIndex++] = 0f;
            normals[nIndex++] = 0f;
            normals[nIndex++] = 1f;
            normals[nIndex++] = 0f;
            normals[nIndex++] = 0f;
            normals[nIndex++] = 1f;

            indices[iIndex++] = (char) (triangleIndex);
            indices[iIndex++] = (char) (triangleIndex + 1);
            indices[iIndex++] = (char) (triangleIndex + 2);
            triangleIndex += 3;

            texCoords[tIndex++] = 0f;
            texCoords[tIndex++] = 0f;
            texCoords[tIndex++] = 0f;
            texCoords[tIndex++] = 1f;
            texCoords[tIndex++] = 1f;
            texCoords[tIndex++] = 1f;

            vertices[vIndex++] = c * innerRadius;
            vertices[vIndex++] = s * innerRadius;
            vertices[vIndex++] = 0f;
            vertices[vIndex++] = c1 * innerRadius;
            vertices[vIndex++] = s1 * innerRadius;
            vertices[vIndex++] = 0f;
            vertices[vIndex++] = c1 * outerRadius;
            vertices[vIndex++] = s1 * outerRadius;
            vertices[vIndex++] = 0f;

            normals[nIndex++] = 0f;
            normals[nIndex++] = 0f;
            normals[nIndex++] = 1f;
            normals[nIndex++] = 0f;
            normals[nIndex++] = 0f;
            normals[nIndex++] = 1f;
            normals[nIndex++] = 0f;
            normals[nIndex++] = 0f;
            normals[nIndex++] = 1f;

            indices[iIndex++] = (char) (triangleIndex + 1);
            indices[iIndex++] = (char) (triangleIndex);
            indices[iIndex++] = (char) (triangleIndex + 2);
            triangleIndex += 3;
        }
        mesh.setVertices(vertices);
        mesh.setTexCoords(texCoords);
        mesh.setNormals(normals);
        mesh.setIndices(indices);
        return mesh;
    }
}
