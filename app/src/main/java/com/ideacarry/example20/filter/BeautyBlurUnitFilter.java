package com.ideacarry.example20.filter;

import android.content.Context;

import com.android.grafika.gles.GlUtil;
import com.ideacarry.utils.CommonUtils;

/**
 * @see <a href="https://github.com/CainKernel/CainCamera">CainCamera</a>
 */
public class BeautyBlurUnitFilter extends GaussPassUnitFilter {

    @Override
    public void onCreate(Context context) {
        mProgram = GlUtil.createProgram(new String(CommonUtils.readAssetFile(context, "example20/filter/vertex_beauty_blur.glsl")),
                new String(CommonUtils.readAssetFile(context, "example20/filter/fragment_beauty_blur.glsl")));
    }
}
