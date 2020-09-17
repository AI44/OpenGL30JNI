package com.ideacarry.example21;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES30;

import com.ideacarry.utils.CommonUtils;
import com.ideacarry.utils.GLShaderProgram;
import com.ideacarry.utils.GLUtils;

public class Filter1 extends BaseFilter {
    public Filter1(Context context, Bitmap bmp, IResult<Bitmap> result) {
        super(context, bmp, result);
    }

    @Override
    public void doFilter(int w, int h, int textureId) {
        int[] params = GLUtils.createQuadVertexArrays(0, 1);

        GLShaderProgram program = new GLShaderProgram(new String(CommonUtils.readAssetFile(mContext, "example21/filter1_vertex.glsl")),
                new String(CommonUtils.readAssetFile(mContext, "example21/filter1_fragment.glsl")));
        program.use();

        GLES30.glViewport(0, 0, w, h);
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureId);
        GLES30.glBindVertexArray(params[0]);
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4);

        program.release();
        GLES30.glDeleteBuffers(1, params, 1);
        GLES30.glDeleteVertexArrays(1, params, 0);
    }
}
