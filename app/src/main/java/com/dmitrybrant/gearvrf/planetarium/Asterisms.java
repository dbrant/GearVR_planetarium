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
        private String name;
        public String getName() {
            return name;
        }

        private List<List<AsterismNode>> lines = new ArrayList<>();
        public List<List<AsterismNode>> getLines() {
            return lines;
        }

        public Asterism(String name, BufferedReader reader) throws IOException {
            this.name = name;
            String line;
            String[] lineArr;
            boolean hasBegun = false;
            while ((line = reader.readLine().trim()) != null) {
                if (line.equals("[")) {
                    hasBegun = true;
                    continue;
                }
                if (line.equals("]")) {
                    break;
                }
                if (!hasBegun) {
                    continue;
                }
                if (!line.contains("[") && !line.contains("]")) {
                    continue;
                }
                lineArr = line.replace("[", "").replace("]", "").split("\"");
                if (lineArr.length == 0) {
                    continue;
                }
                List<AsterismNode> nodeList = new ArrayList<>();
                for (String s : lineArr) {
                    if (s.trim().length() == 0) {
                        continue;
                    }
                    AsterismNode node = new AsterismNode(s.trim());
                    nodeList.add(node);
                }
                lines.add(nodeList);
            }
        }

        public GVRMesh createMesh(GVRContext context) {
            int numVertices = 0;
            int numIndices = 0;
            for (List<AsterismNode> line : lines) {
                numVertices += line.size();
                numIndices += ((line.size() - 1) * 2);
            }

            float[] vertices = new float[numVertices * 3];
            char[] indices = new char[numIndices];
            int vertexPos = 0;
            int indexPos = 0;
            int indexNum = 0;

            GVRMesh mesh = new GVRMesh(context);
            for (List<AsterismNode> line : lines) {
                for(AsterismNode node : line) {
                    if (node.getStar() == null) {
                        Log.w("foo", ">>>>>>>>>>>>>>>>>> orphan asterism node: " + node.getName());
                        continue;
                    }
                    vertices[vertexPos++] = node.getStar().sceneObj.getTransform().getPositionX();
                    vertices[vertexPos++] = node.getStar().sceneObj.getTransform().getPositionY();
                    vertices[vertexPos++] = node.getStar().sceneObj.getTransform().getPositionZ();
                }
                int indexBase = indexNum;
                for (int i = 0; i < line.size() - 1; i++) {
                    indices[indexPos + i * 2] = (char) indexNum++;
                    indices[indexPos + i * 2 + 1] = (char) indexNum;
                }
                indexPos += (line.size() - 1) * 2;
                indexNum++;
            }

            mesh.setVertices(vertices);
            mesh.setIndices(indices);
            return mesh;
        }
    }

    public static class AsterismNode {
        private String name;
        public String getName() {
            return name;
        }

        private SkyObject star;
        public SkyObject getStar() {
            return star;
        }
        public void setStar(SkyObject star) {
            this.star = star;
        }

        public AsterismNode(String name) {
            this.name = name;
        }
    }

    private static List<Asterism> asterisms = new ArrayList<>();
    public static List<Asterism> getAsterisms() {
        return asterisms;
    }

    public static void addStar(SkyObject star) {
        for (Asterism asterism : asterisms) {
            for (List<AsterismNode> line : asterism.getLines()) {
                for (AsterismNode node : line) {
                    if (star.name != null && star.name.contains(node.getName())) {
                        node.setStar(star);
                        // TODO: make this better
                        // ensure that this star is displayed by artificially boosting its magnitude
                        star.mag = 4f;
                    }
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
                if (!line.contains("\"")) {
                    continue;
                }
                asterisms.add(new Asterism(line.replace("\"", "").trim(), buffreader));
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
