package com.ideacarry.example20.filter;

import android.content.Context;

import com.ideacarry.utils.CommonUtils;
import com.ideacarry.utils.GLShaderProgram;

/**
 * @see <a href="https://github.com/CainKernel/CainCamera">CainCamera</a>
 */
public class BeautyBlurUnitFilter extends GaussPassUnitFilter {

    @Override
    public void onCreate(Context context) {
        mProgram = new GLShaderProgram(new String(CommonUtils.readAssetFile(context, "example20/filter/vertex_beauty_blur.glsl")),
                new String(CommonUtils.readAssetFile(context, "example20/filter/fragment_beauty_blur.glsl")));
    }
}
