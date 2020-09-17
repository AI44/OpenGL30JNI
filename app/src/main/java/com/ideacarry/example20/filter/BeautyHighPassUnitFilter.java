package com.ideacarry.example20.filter;

import android.content.Context;
import android.opengl.GLES30;

import com.ideacarry.stable.filter.IUnitFilter;
import com.ideacarry.utils.CommonUtils;
import com.ideacarry.utils.GLShaderProgram;

/**
 * @see <a href="https://github.com/CainKernel/CainCamera">CainCamera</a>
 */
public class BeautyHighPassUnitFilter implements IUnitFilter {

    private GLShaderProgram mProgram;

    @Override
    public void onCreate(Context context) {
        mProgram = new GLShaderProgram(new String(CommonUtils.readAssetFile(context, "example20/filter/general_vertex.glsl")),
                new String(CommonUtils.readAssetFile(context, "example20/filter/fragment_beauty_highpass.glsl")));

        mProgram.use();
        mProgram.setInt("inputTexture", 0);
        mProgram.setInt("blurTexture", 1);
    }

    @Override
    public void onSizeChange(Context context, int viewWidth, int viewHeight, int cameraWidth, int cameraHeight, int degree, int flip) {
    }

    /**
     * @param textures [0]:inputTexture, [1]:blurTexture
     */
    @Override
    public void onDraw(int width, int height, int commonVao, int... textures) {
        GLES30.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT);

        mProgram.use();
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textures[0]);
        GLES30.glActiveTexture(GLES30.GL_TEXTURE1);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textures[1]);
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
}
