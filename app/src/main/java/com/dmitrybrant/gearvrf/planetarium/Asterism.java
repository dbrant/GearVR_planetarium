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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.opengl.GLES20;

import java.util.ArrayList;
import java.util.List;

import org.gearvrf.GVRBitmapImage;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;
import org.gearvrf.utility.Log;

public class Asterism {
    private static final String TAG = "Asterism";
    private static final float LABEL_WIDTH = 50f;
    private static final int LABEL_COLOR = 0x80005080;
    private static final float OPACITY_PASSIVE = 0.3f;
    private static final float OPACITY_ACTIVE = 1.0f;

    private final String name;
    public String getName() {
        return name;
    }

    private List<AsterismNode> nodes = new ArrayList<>();
    public List<AsterismNode> getNodes() {
        return nodes;
    }

    private SkyObject skyObject;
    public SkyObject getSkyObject() {
        return skyObject;
    }

    private GVRSceneObject labelObject;
    public GVRSceneObject getLabelObject() {
        return labelObject;
    }
    public void setLabelObject(GVRSceneObject object) {
        this.labelObject = object;
    }

    public Asterism(String line, SkyObject skyObject) {
        this.skyObject = skyObject;
        String[] lineArr = line.split("\\s+");
        name = Util.bayerExToFullName(Util.bayerToFullName(lineArr[0]));
        for (int i = 2; i < lineArr.length; i++) {
            int hipNum = Integer.parseInt(lineArr[i]);
            AsterismNode node = new AsterismNode(hipNum);
            nodes.add(node);
        }
    }

    public GVRSceneObject createSceneObject(GVRContext context) {
        GVRMaterial material = new GVRMaterial(context, GVRMaterial.GVRShaderType.Phong.ID);
        material.setDiffuseColor(0, 0, 1f, 1f);
        material.setLineWidth(2.0f);
        GVRSceneObject obj = new GVRSceneObject(context);
        GVRRenderData rd = new GVRRenderData(context);
        rd.setMesh(createMesh(context));
        rd.setDrawMode(GLES20.GL_LINES);
        rd.setMaterial(material);
        rd.setDepthTest(false);
        obj.attachRenderData(rd);
        skyObject.sceneObj = obj;
        return obj;
    }

    public GVRSceneObject createLabelObject(GVRContext gvrContext, Context context) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(LABEL_COLOR);
        paint.setTextSize((int) (16 * context.getResources().getDisplayMetrics().density));
        Rect bounds = new Rect();
        paint.getTextBounds(name, 0, name.length(), bounds);

        Bitmap bmp = Bitmap.createBitmap(bounds.width() * 3 / 2, bounds.height() * 3 / 2, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);
        canvas.drawText(name, bmp.getWidth() / 4f, bmp.getHeight() * 3f / 4f, paint);

        final float labelWidthNormal = 120f;
        float widthScale = (float) bounds.width() / labelWidthNormal;

        float aspect = (float) bounds.width() / (float) bounds.height();
        GVRTexture texture = new GVRTexture(gvrContext);
        texture.setImage(new GVRBitmapImage(gvrContext, bmp));
        GVRSceneObject sobj = new GVRSceneObject(gvrContext, gvrContext.createQuad(LABEL_WIDTH * widthScale, LABEL_WIDTH * widthScale / aspect), texture);
        sobj.getRenderData().setDepthTest(false);
        sobj.getRenderData().getMaterial().setOpacity(OPACITY_PASSIVE);
        return sobj;
    }

    public float getCenterRa() {
        double xavg = 0.0;
        double yavg = 0.0;
        for (AsterismNode node : nodes) {
            xavg += Math.cos(Math.toRadians(node.getStar().ra));
            yavg += Math.sin(Math.toRadians(node.getStar().ra));
        }
        xavg /= nodes.size();
        yavg /= nodes.size();
        return (float) Math.toDegrees(Math.atan2(yavg, xavg));
    }

    public float getCenterDec() {
        double dec = 0.0;
        for (AsterismNode node : nodes) {
            dec += node.getStar().dec;
        }
        return (float) (dec / nodes.size());
    }

    public void setActive() {
        skyObject.sceneObj.getRenderData().getMaterial().setDiffuseColor(0, 0.1f, 0.5f, 1f);
        labelObject.getRenderData().getMaterial().setOpacity(Asterism.OPACITY_ACTIVE);
    }

    public void setPassive() {
        skyObject.sceneObj.getRenderData().getMaterial().setDiffuseColor(0, 0.02f, 0.1f, 1f);
        labelObject.getRenderData().getMaterial().setOpacity(Asterism.OPACITY_PASSIVE);
    }

    private GVRMesh createMesh(GVRContext context) {
        float[] vertices = new float[nodes.size() * 3];
        int vertexPos = 0;
        GVRMesh mesh = new GVRMesh(context);
        for(AsterismNode node : nodes) {
            if (node.getStar() == null) {
                Log.w(TAG, "Orphan asterism node: " + node.getHipNum());
                continue;
            }

            double ra = Math.toRadians(node.getStar().ra - 180.0);
            double dec = Math.toRadians(node.getStar().dec);

            vertices[vertexPos++] = (float) ((StarLoader.DEFAULT_DISTANCE_STAR * Math.cos(dec)) * Math.sin(ra));
            vertices[vertexPos++] = (float) (StarLoader.DEFAULT_DISTANCE_STAR * Math.sin(dec));
            vertices[vertexPos++] = (float) ((StarLoader.DEFAULT_DISTANCE_STAR * Math.cos(dec)) * Math.cos(ra));
        }
        mesh.setVertices(vertices);
        return mesh;
    }

    public class AsterismNode {
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
}



