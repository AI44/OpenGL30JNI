package com.ideacarry.stable.filter;

import android.content.Context;
import android.opengl.GLES30;

import com.ideacarry.utils.CommonUtils;
import com.ideacarry.utils.GLShaderProgram;

public class Gaussian5x5UnitFilter implements IUnitFilter {
    protected GLShaderProgram mProgram;

    @Override
    public void onCreate(Context context) {
        mProgram = new GLShaderProgram(new String(CommonUtils.readAssetFile(context, "stable/gaussian_filter_5x5_vertex.glsl")),
                new String(CommonUtils.readAssetFile(context, "stable/gaussian_filter_5x5_fragment.glsl")));
    }

    @Override
    public void onSizeChange(Context context, int viewWidth, int viewHeight, int cameraWidth, int cameraHeight, int degree, int flip) {
    }

    @Override
    public void onDraw(int width, int height, int commonVao, int... textures) {
        GLES30.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT);

        mProgram.use();
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
        if (mProgram != null) {
            mProgram.release();
            mProgram = null;
        }
    }

    public void setOffset(float x, float y) {
        mProgram.use();
        mProgram.setFloat("xOffset", x);
        mProgram.setFloat("yOffset", y);
    }
}
