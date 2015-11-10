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

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRMesh;
import org.gearvrf.utility.Log;

public class Asterisms {
    public static final String TAG = "Asterisms";

    public static class Asterism {
        private final String name;
        public String getName() {
            return name;
        }

        private List<AsterismNode> segments = new ArrayList<>();
        public List<AsterismNode> getSegments() {
            return segments;
        }

        public Asterism(String line) throws IOException {
            String[] lineArr = line.split("\\s+");
            name = lineArr[0];
            for (int i = 2; i < lineArr.length; i++) {
                int hipNum = Integer.parseInt(lineArr[i]);
                AsterismNode node = new AsterismNode(hipNum);
                segments.add(node);
            }
        }

        public GVRMesh createMesh(GVRContext context) {
            float[] vertices = new float[segments.size() * 3];
            char[] indices = new char[segments.size()];
            int vertexPos = 0;
            GVRMesh mesh = new GVRMesh(context);
            for(AsterismNode node : segments) {
                if (node.getStar() == null) {
                    Log.w("foo", ">>>>>>>>>>>>>>>>>> orphan asterism node: " + node.getHipNum());
                    continue;
                }
                vertices[vertexPos++] = node.getStar().sceneObj.getTransform().getPositionX();
                vertices[vertexPos++] = node.getStar().sceneObj.getTransform().getPositionY();
                vertices[vertexPos++] = node.getStar().sceneObj.getTransform().getPositionZ();
            }
            for (int i = 0; i < indices.length; i++) {
                indices[i] = (char) i;
            }
            mesh.setVertices(vertices);
            mesh.setIndices(indices);
            return mesh;
        }
    }

    public static class AsterismNode {
        private int hipNum;
        public int getHipNum() {
            return hipNum;
        }

        private SkyObject star;
        public SkyObject getStar() {
            return star;
        }
        public void setStar(SkyObject star) {
            this.star = star;
        }

        public AsterismNode(int hipNum) {
            this.hipNum = hipNum;
        }
    }

    private static List<Asterism> asterisms = new ArrayList<>();
    public static List<Asterism> getAsterisms() {
        return asterisms;
    }

    public static void addStar(SkyObject star) {
        for (Asterism asterism : asterisms) {
            for (AsterismNode node : asterism.segments) {
                if (star.hipNum == (node.getHipNum())) {
                    node.setStar(star);
                    // TODO: make this better
                    // ensure that this star is displayed by artificially boosting its magnitude
                    star.mag = 4f;
                }
            }
        }
    }

    public static void loadAsterisms(Context context) {
        InputStream instream = null;
        try {
            Log.d(TAG, "Loading asterisms...");
            instream = context.getAssets().open("asterisms.txt");
            BufferedReader buffreader = new BufferedReader(new InputStreamReader(instream));
            String line;
            while ((line = buffreader.readLine()) != null) {
                if (line.trim().length() == 0) {
                    continue;
                }
                asterisms.add(new Asterism(line.trim()));
            }
        } catch (IOException e) {
            Log.e(TAG, "Failed to read asterisms.", e);
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
