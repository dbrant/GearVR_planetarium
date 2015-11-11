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

import org.gearvrf.GVRSceneObject;

public class SkyObject {
    public static final int TYPE_STAR = 0;
    public static final int TYPE_PLANET = 1;
    public static final int TYPE_NEBULA = 2;
    public static final int TYPE_ASTERISM = 3;
    public static final int TYPE_ASTERISM_LABEL = 4;
    public static final int TYPE_OTHER = 5;

    public GVRSceneObject sceneObj;

    // sorry...
    public int type;
    public double ra;
    public double dec;
    public float dist;
    public float mag;
    public int hipNum;
    public String name;
    public String className;

    public float initialScale;
    public int texResId;
    public String texName;
}
