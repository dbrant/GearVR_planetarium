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

import org.gearvrf.utility.Log;

public class AsterismLoader {
    public static final String TAG = "Asterisms";

    private List<Asterism> asterisms = new ArrayList<>();
    public List<Asterism> getAsterisms() {
        return asterisms;
    }

    public void addStar(SkyObject star) {
        for (Asterism asterism : asterisms) {
            for (Asterism.AsterismNode node : asterism.getNodes()) {
                if (star.hipNum == (node.getHipNum())) {
                    node.setStar(star);
                    // TODO: make this better
                    // ensure that this star is displayed by artificially boosting its magnitude
                    star.mag = 4f;
                }
            }
        }
    }

    public void loadAsterisms(Context context) {
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
