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

public class Util {
    public static final String TAG = "Util";

    public static String formatAsHtml(String content) {
        String html = "<html><head><style type=\"text/css\">" +
                "body{background-color:#303030;color:#c0c0c0;}" +
                "a{color:#fff;text-decoration:none;}" +
                "</style></head><body>";
        html += content;
        html += "</body>";
        return html;
    }

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
