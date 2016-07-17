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
            skyMesh = SphereMesh.createSphereMesh(context, 1f, 32, 32, true);
        }
        return skyMesh;
    }
}
