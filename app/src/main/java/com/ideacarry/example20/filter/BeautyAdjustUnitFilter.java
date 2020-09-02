package com.ideacarry.example20.filter;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLES30;

import com.android.grafika.gles.GlUtil;
import com.ideacarry.utils.CommonUtils;

public class BeautyAdjustUnitFilter implements IUnitFilter {

    protected int mProgram = -1;
    private float mIntensity = 0.6f;

    @Override
    public void onCreate(Context context) {
        mProgram = GlUtil.createProgram(new String(CommonUtils.readAssetFile(context, "example20/filter/general_vertex.glsl")),
                new String(CommonUtils.readAssetFile(context, "example20/filter/fragment_beauty_adjust.glsl")));

        GLES30.glUseProgram(mProgram);
        GLES20.glUniform1f(6, mIntensity);
        GLES30.glUniform1i(3, 3);
        GLES30.glUniform1i(4, 4);
        GLES30.glUniform1i(5, 5);
    }

    @Override
    public void onSizeChange(Context context, int viewWidth, int viewHeight, int cameraWidth, int cameraHeight, int degree, int flip) {
    }

    /**
     * @param textures [0]:inputTexture, [1]:blurTexture, [2]:highPassBlurTexture
     */
    @Override
    public void onDraw(int width, int height, int commonVao, int... textures) {
        GLES30.glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT);

        GLES30.glUseProgram(mProgram);
        GLES30.glActiveTexture(GLES30.GL_TEXTURE3);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textures[0]);
        GLES30.glActiveTexture(GLES30.GL_TEXTURE4);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textures[1]);
        GLES30.glActiveTexture(GLES30.GL_TEXTURE5);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textures[2]);
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
