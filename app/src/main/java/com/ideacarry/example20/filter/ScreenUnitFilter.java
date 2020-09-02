package com.ideacarry.example20.filter;

import android.content.Context;
import android.opengl.GLES30;

import com.android.grafika.gles.GlUtil;
import com.ideacarry.utils.CommonUtils;

public class ScreenUnitFilter implements IUnitFilter {

    private int mProgram = -1;

    @Override
    public void onCreate(Context context) {
        mProgram = GlUtil.createProgram(new String(CommonUtils.readAssetFile(context, "example20/screen/screen_vertex.glsl")),
                new String(CommonUtils.readAssetFile(context, "example20/screen/screen_fragment.glsl")));
    }

    @Override
    public void onSizeChange(Context context, int viewWidth, int viewHeight, int cameraWidth, int cameraHeight, int degree, int flip) {
    }

    @Override
    public void onDraw(int width, int height, int commonVao, int... textures) {
        GLES30.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT);

        GLES30.glUseProgram(mProgram);
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textures[0]);
        GLES30.glBindVertexArray(commonVao);
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4);

        //clear
        GLES30.glBindVertexArray(0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0);
        GLES30.glUseProgram(0);
    }

    @Override
    public void onDestroy(Context context) {
        if (mProgram > -1) {
            GLES30.glDeleteProgram(mProgram);
            mProgram = -1;
        }
    }
}
