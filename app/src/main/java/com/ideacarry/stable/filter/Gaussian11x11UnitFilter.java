package com.ideacarry.stable.filter;

import android.content.Context;

import com.ideacarry.utils.CommonUtils;
import com.ideacarry.utils.GLShaderProgram;

public class Gaussian11x11UnitFilter extends Gaussian5x5UnitFilter {
    @Override
    public void onCreate(Context context) {
        mProgram = new GLShaderProgram(new String(CommonUtils.readAssetFile(context, "stable/gaussian_filter_11x11_vertex.glsl")),
                new String(CommonUtils.readAssetFile(context, "stable/gaussian_filter_11x11_fragment.glsl")));
    }
}
