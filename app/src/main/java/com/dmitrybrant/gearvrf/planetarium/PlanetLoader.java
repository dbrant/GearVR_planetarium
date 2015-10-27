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

import com.mhuss.AstroLib.AstroDate;
import com.mhuss.AstroLib.NoInitException;
import com.mhuss.AstroLib.ObsInfo;
import com.mhuss.AstroLib.PlanetData;
import com.mhuss.AstroLib.Planets;

import java.util.Calendar;
import java.util.List;

public class PlanetLoader {
    private static final float DEFAULT_DISTANCE_PLANET = 50f;

    public static void loadPlanets(List<SkyObject> objectList) {
        try {
            Calendar calendar = Calendar.getInstance();
            AstroDate date = new AstroDate(calendar.get(Calendar.DAY_OF_MONTH),
                    calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.YEAR));
            ObsInfo obsInfo = new ObsInfo();
            double jd = date.jd();

            addPlanet(objectList, jd, obsInfo, Planets.SUN, "gstar.jpg", "Sun");
            addPlanet(objectList, jd, obsInfo, Planets.LUNA, "moon.jpg", "Moon");
            addPlanet(objectList, jd, obsInfo, Planets.MERCURY, "mercurymap.jpg", "Mercury");
            addPlanet(objectList, jd, obsInfo, Planets.VENUS, "venusmap.jpg", "Venus");
            addPlanet(objectList, jd, obsInfo, Planets.MARS, "mars_1k_color.jpg", "Mars");
            addPlanet(objectList, jd, obsInfo, Planets.JUPITER, "jupiter.jpg", "Jupiter");
            addPlanet(objectList, jd, obsInfo, Planets.SATURN, "saturn.jpg", "Saturn");
            addPlanet(objectList, jd, obsInfo, Planets.URANUS, "uranus.jpg", "Uranus");
            addPlanet(objectList, jd, obsInfo, Planets.NEPTUNE, "neptune.jpg", "Neptune");
            addPlanet(objectList, jd, obsInfo, Planets.PLUTO, "pluto.jpg", "Pluto");

        } catch (Exception e) {
            //
        }
    }

    private static void addPlanet(List<SkyObject> objectList, double julianDate, ObsInfo obsInfo,
                                  int planetID, String texName, String name) throws NoInitException {
        PlanetData data = new PlanetData(planetID, julianDate, obsInfo);
        SkyObject obj = new SkyObject();
        objectList.add(obj);
        obj.type = SkyObject.TYPE_PLANET;
        obj.dist = DEFAULT_DISTANCE_PLANET;
        obj.initialScale = 1f;
        obj.name = name;
        obj.texName = texName;
        obj.ra = Math.toDegrees(data.getRightAscension());
        obj.dec = Math.toDegrees(data.getDeclination());
    }
}
