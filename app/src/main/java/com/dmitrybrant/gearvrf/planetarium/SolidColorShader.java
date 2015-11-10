
package com.dmitrybrant.gearvrf.planetarium;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRMaterialMap;
import org.gearvrf.GVRMaterialShaderManager;
import org.gearvrf.GVRCustomMaterialShaderId;

public class SolidColorShader {
    public static final String COLOR_KEY = "color";

    private static final String VERTEX_SHADER = "uniform mat4 u_mvp;\n"
            + "attribute vec4 a_position;\n"
            + "void main() {\n"
            + "gl_Position = u_mvp * a_position;\n"
            + "}";

    private static final String FRAGMENT_SHADER = "precision highp float;\n"
            + "uniform vec4  u_color;\n"
            + "void main() {\n"
            + "gl_FragColor = u_color;\n"
            + "}";

    private GVRCustomMaterialShaderId mShaderId;
    private GVRMaterialMap mCustomShader = null;

    public SolidColorShader(GVRContext gvrContext) {
        final GVRMaterialShaderManager shaderManager = gvrContext
                .getMaterialShaderManager();
        mShaderId = shaderManager.addShader(VERTEX_SHADER, FRAGMENT_SHADER);
        mCustomShader = shaderManager.getShaderMap(mShaderId);
        mCustomShader.addUniformVec4Key("u_color", COLOR_KEY);
    }

    public GVRCustomMaterialShaderId getShaderId() {
        return mShaderId;
    }
}
