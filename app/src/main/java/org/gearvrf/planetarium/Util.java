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

package org.gearvrf.planetarium;

public class Util {
    public static final String TAG = "Util";

    public static float hmsToDec(float h, float m, float s) {
        float ret;
        ret = h * 15f;
        ret += m * 0.25f;
        ret += s * 0.00416667f;
        return ret;
    }

    public static float dmsToDec(float d, float m, float s) {
        float ret = d;
        if (d < 0f) {
            ret -= m * 0.016666667f;
            ret -= s * 2.77777778e-4f;
        } else {
            ret += m * 0.016666667f;
            ret += s * 2.77777778e-4f;
        }
        return ret;
    }
}
