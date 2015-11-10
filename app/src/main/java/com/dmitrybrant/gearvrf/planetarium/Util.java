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

    public static String bayerToGreek(String name) {
        name = name.replace("ALF", "Alpha");
        name = name.replace("BET", "Beta");
        name = name.replace("GAM", "Gamma");
        name = name.replace("DEL", "Delta");
        name = name.replace("EPS", "Epsilon");
        name = name.replace("ZET", "Zeta");
        name = name.replace("ETA", "Eta");
        name = name.replace("TET", "Theta");
        name = name.replace("IOT", "Iota");
        name = name.replace("KAP", "Kappa");
        name = name.replace("LAM", "Lambda");
        name = name.replace("MU", "Mu");
        name = name.replace("NU", "Nu");
        name = name.replace("XI", "Xi");
        name = name.replace("OMI", "Omicron");
        name = name.replace("PI", "Pi");
        name = name.replace("RHO", "Rho");
        name = name.replace("SIG", "Sigma");
        name = name.replace("TAU", "Tau");
        name = name.replace("UPS", "Upsilon");
        name = name.replace("PHI", "Phi");
        name = name.replace("CHI", "Chi");
        name = name.replace("PSI", "Psi");
        name = name.replace("OME", "Omega");
        return name;
    }

    public static String greekToBayer(String name) {
        name = name.replace("Alpha", "ALF");
        name = name.replace("Beta", "BET");
        name = name.replace("Gamma", "GAM");
        name = name.replace("Delta", "DEL");
        name = name.replace("Epsilon", "EPS");
        name = name.replace("Zeta", "ZET");
        name = name.replace("Eta", "ETA");
        name = name.replace("Theta", "TET");
        name = name.replace("Iota", "IOT");
        name = name.replace("Kappa", "KAP");
        name = name.replace("Lambda", "LAM");
        name = name.replace("Mu", "MU");
        name = name.replace("Nu", "NU");
        name = name.replace("Xi", "XI");
        name = name.replace("Omicron", "OMI");
        name = name.replace("Pi", "PI");
        name = name.replace("Rho", "RHO");
        name = name.replace("Sigma", "SIG");
        name = name.replace("Tau", "TAU");
        name = name.replace("Upsilon", "UPS");
        name = name.replace("Phi", "PHI");
        name = name.replace("Chi", "CHI");
        name = name.replace("Psi", "PSI");
        name = name.replace("Omega", "OME");
        return name;
    }

    public static String bayerToFullName(String name) {
        name = name.replace("And", "Andromeda");
        name = name.replace("Ant", "Antlia");
        name = name.replace("Aps", "Apus");
        name = name.replace("Aqr", "Aquarius");
        name = name.replace("Aql", "Aquila");
        name = name.replace("Ara", "Ara");
        name = name.replace("Ari", "Aries");
        name = name.replace("Aur", "Auriga");
        name = name.replace("Boo", "Boötes");
        name = name.replace("Cae", "Caelum");
        name = name.replace("Cam", "Camelopardalis");
        name = name.replace("Cnc", "Cancer");
        name = name.replace("CVn", "Canes Venatici");
        name = name.replace("CMa", "Canis Major");
        name = name.replace("CMi", "Canis Minor");
        name = name.replace("Cap", "Capricornus");
        name = name.replace("Car", "Carina");
        name = name.replace("Cas", "Cassiopeia");
        name = name.replace("Cen", "Centaurus");
        name = name.replace("Cep", "Cepheus");
        name = name.replace("Cet", "Cetus");
        name = name.replace("Cha", "Chamaeleon");
        name = name.replace("Cir", "Circinus");
        name = name.replace("Col", "Columba");
        name = name.replace("Com", "Coma Berenices");
        name = name.replace("CrA", "Corona Australis");
        name = name.replace("CrB", "Corona Borealis");
        name = name.replace("Crv", "Corvus");
        name = name.replace("Crt", "Crater");
        name = name.replace("Cru", "Crux");
        name = name.replace("Cyg", "Cygnus");
        name = name.replace("Del", "Delphinus");
        name = name.replace("Dor", "Dorado");
        name = name.replace("Dra", "Draco");
        name = name.replace("Equ", "Equuleus");
        name = name.replace("Eri", "Eridanus");
        name = name.replace("For", "Fornax");
        name = name.replace("Gem", "Gemini");
        name = name.replace("Gru", "Grus");
        name = name.replace("Her", "Hercules");
        name = name.replace("Hor", "Horologium");
        name = name.replace("Hya", "Hydra");
        name = name.replace("Hyi", "Hydrus");
        name = name.replace("Ind", "Indus");
        name = name.replace("Lac", "Lacerta");
        name = name.replace("Leo", "Leo");
        name = name.replace("LMi", "Leo Minor");
        name = name.replace("Lep", "Lepus");
        name = name.replace("Lib", "Libra");
        name = name.replace("Lup", "Lupus");
        name = name.replace("Lyn", "Lynx");
        name = name.replace("Lyr", "Lyra");
        name = name.replace("Men", "Mensa");
        name = name.replace("Mic", "Microscopium");
        name = name.replace("Mon", "Monoceros");
        name = name.replace("Mus", "Musca");
        name = name.replace("Nor", "Norma");
        name = name.replace("Oct", "Octans");
        name = name.replace("Oph", "Ophiuchus");
        name = name.replace("Ori", "Orion");
        name = name.replace("Pav", "Pavo");
        name = name.replace("Peg", "Pegasus");
        name = name.replace("Per", "Perseus");
        name = name.replace("Phe", "Phoenix");
        name = name.replace("Pic", "Pictor");
        name = name.replace("Psc", "Pisces");
        name = name.replace("PsA", "Piscis Austrinus");
        name = name.replace("Pup", "Puppis");
        name = name.replace("Pyx", "Pyxis");
        name = name.replace("Ret", "Reticulum");
        name = name.replace("Sge", "Sagitta");
        name = name.replace("Sgr", "Sagittarius");
        name = name.replace("Sco", "Scorpius");
        name = name.replace("Scl", "Sculptor");
        name = name.replace("Sct", "Scutum");
        name = name.replace("Ser", "Serpens");
        name = name.replace("Sex", "Sextans");
        name = name.replace("Tau", "Taurus");
        name = name.replace("Tel", "Telescopium");
        name = name.replace("Tri", "Triangulum");
        name = name.replace("TrA", "Triangulum Australe");
        name = name.replace("Tuc", "Tucana");
        name = name.replace("UMa", "Ursa Major");
        name = name.replace("UMi", "Ursa Minor");
        name = name.replace("Vel", "Vela");
        name = name.replace("Vir", "Virgo");
        name = name.replace("Vol", "Volans");
        name = name.replace("Vul", "Vulpecula");
        return name;
    }

    public static String bayerToGenitive(String name) {
        name = name.replace("And", "Andromedae");
        name = name.replace("Ant", "Antliae");
        name = name.replace("Aps", "Apodis");
        name = name.replace("Aqr", "Aquarii");
        name = name.replace("Aql", "Aquilae");
        name = name.replace("Ara", "Arae");
        name = name.replace("Ari", "Arietis");
        name = name.replace("Aur", "Aurigae");
        name = name.replace("Boo", "Boötis");
        name = name.replace("Cae", "Caeli");
        name = name.replace("Cam", "Camelopardalis");
        name = name.replace("Cnc", "Cancri");
        name = name.replace("CVn", "Canum Venaticorum");
        name = name.replace("CMa", "Canis Majoris");
        name = name.replace("CMi", "Canis Minoris");
        name = name.replace("Cap", "Capricorni");
        name = name.replace("Car", "Carinae");
        name = name.replace("Cas", "Cassiopeiae");
        name = name.replace("Cen", "Centauri");
        name = name.replace("Cep", "Cephei");
        name = name.replace("Cet", "Ceti");
        name = name.replace("Cha", "Chamaeleontis");
        name = name.replace("Cir", "Circini");
        name = name.replace("Col", "Columbae");
        name = name.replace("Com", "Comae");
        name = name.replace("CrA", "Coronae Australis");
        name = name.replace("CrB", "Coronae Borealis");
        name = name.replace("Crv", "Corvi");
        name = name.replace("Crt", "Crateris");
        name = name.replace("Cru", "Crucis");
        name = name.replace("Cyg", "Cygni");
        name = name.replace("Del", "Delphini");
        name = name.replace("Dor", "Doradus");
        name = name.replace("Dra", "Draconis");
        name = name.replace("Equ", "Equulei");
        name = name.replace("Eri", "Eridani");
        name = name.replace("For", "Fornacis");
        name = name.replace("Gem", "Geminorum");
        name = name.replace("Gru", "Gruis");
        name = name.replace("Her", "Herculis");
        name = name.replace("Hor", "Horologii");
        name = name.replace("Hya", "Hydrae");
        name = name.replace("Hyi", "Hydri");
        name = name.replace("Ind", "Indi");
        name = name.replace("Lac", "Lacertae");
        name = name.replace("Leo", "Leonis");
        name = name.replace("LMi", "Leonis Minoris");
        name = name.replace("Lep", "Leporis");
        name = name.replace("Lib", "Librae");
        name = name.replace("Lup", "Lupi");
        name = name.replace("Lyn", "Lyncis");
        name = name.replace("Lyr", "Lyrae");
        name = name.replace("Men", "Mensae");
        name = name.replace("Mic", "Microscopii");
        name = name.replace("Mon", "Monocerotis");
        name = name.replace("Mus", "Muscae");
        name = name.replace("Nor", "Normae");
        name = name.replace("Oct", "Octantis");
        name = name.replace("Oph", "Ophiuchi");
        name = name.replace("Ori", "Orionis");
        name = name.replace("Pav", "Pavonis");
        name = name.replace("Peg", "Pegasi");
        name = name.replace("Per", "Persei");
        name = name.replace("Phe", "Phoenicis");
        name = name.replace("Pic", "Pictoris");
        name = name.replace("Psc", "Piscium");
        name = name.replace("PsA", "Piscis Austrini");
        name = name.replace("Pup", "Puppis");
        name = name.replace("Pyx", "Pyxidis");
        name = name.replace("Ret", "Reticuli");
        name = name.replace("Sge", "Sagittae");
        name = name.replace("Sgr", "Sagittarii");
        name = name.replace("Sco", "Scorpii");
        name = name.replace("Scl", "Sculptoris");
        name = name.replace("Sct", "Scuti");
        name = name.replace("Ser", "Serpentis");
        name = name.replace("Sex", "Sextantis");
        name = name.replace("Tau", "Tauri");
        name = name.replace("Tel", "Telescopii");
        name = name.replace("Tri", "Trianguli");
        name = name.replace("TrA", "Trianguli Australis");
        name = name.replace("Tuc", "Tucanae");
        name = name.replace("UMa", "Ursae Majoris");
        name = name.replace("UMi", "Ursae Minoris");
        name = name.replace("Vel", "Velorum");
        name = name.replace("Vir", "Virginis");
        name = name.replace("Vol", "Volantis");
        name = name.replace("Vul", "Vulpeculae");
        return name;
    }

    public static String transformObjectName(String origName) {
        String name = origName.trim();

        // wikify certain names
        name = name.replace("Mercury", "Mercury (planet)");

        String[] nameArr = name.split("\\s+");
        if (nameArr.length == 2) {
            nameArr[0] = bayerToGreek(nameArr[0]);
            nameArr[1] = bayerToGenitive(nameArr[1]);
            name = nameArr[0] + " " + nameArr[1];
        }

        return name;
    }
}
