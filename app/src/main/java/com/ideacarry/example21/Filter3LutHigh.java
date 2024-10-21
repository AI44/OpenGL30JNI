package com.ideacarry.example21;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES30;

import com.android.grafika.gles.GlUtil;
import com.ideacarry.utils.CommonUtils;
import com.ideacarry.utils.GLShaderProgram;
import com.ideacarry.utils.GLUtils;

public class Filter3LutHigh extends BaseFilter {
    public Filter3LutHigh(Context context, Bitmap bmp, IResult<Bitmap> result) {
        super(context, bmp, result);
    }

    @Override
    public void doFilter(int w, int h, int textureId) {
        int[] params = GLUtils.createQuadVertexArrays(0, 1);

        GLShaderProgram program = new GLShaderProgram(new String(CommonUtils.readAssetFile(mContext, "example21/filter3_lut_vertex.glsl")),
                new String(CommonUtils.readAssetFile(mContext, "example21/filter3_lut_high_fragment.glsl")));
        program.use();
        program.setInt("uTexturePic", 0);
        program.setInt("uTextureLUT", 1);
        program.setFloat("intensity", 1.0f);
        GlUtil.checkGlError("init");

        int lut = GLUtils.createTexture3DFromAssets(mContext, "example21/lut.png", 8, 8);
        GlUtil.checkGlError("lut");

        GLES30.glViewport(0, 0, w, h);
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureId);
        GlUtil.checkGlError("2d");
        GLES30.glActiveTexture(GLES30.GL_TEXTURE1);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_3D, lut);
        GlUtil.checkGlError("3d");
        GLES30.glBindVertexArray(params[0]);
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4);
        GlUtil.checkGlError("draw");

        GLES30.glDeleteTextures(1, new int[]{lut}, 0);
        program.release();
        GLUtils.deleteQuadVertexArrays(params[0], params[1]);
    }
}
