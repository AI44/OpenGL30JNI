package com.ideacarry.example21;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES30;

import com.ideacarry.utils.CommonUtils;
import com.ideacarry.utils.GLShaderProgram;
import com.ideacarry.utils.GLUtils;

public class Filter5DetectSkinOld extends BaseFilter {
    public Filter5DetectSkinOld(Context context, Bitmap bmp, IResult<Bitmap> result) {
        super(context, bmp, result);
    }

    @Override
    public void doFilter(int w, int h, int textureId) {
        int[] params = GLUtils.createQuadVertexArrays(0, 1);

        GLShaderProgram program = new GLShaderProgram(new String(CommonUtils.readAssetFile(mContext, "example21/filter5_detect_skin_vertex.glsl")),
                new String(CommonUtils.readAssetFile(mContext, "example21/filter5_detect_skin_old_fragment.glsl")));
        program.use();

        GLES30.glViewport(0, 0, w, h);
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureId);
        GLES30.glBindVertexArray(params[0]);
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4);

        program.release();
        GLUtils.deleteQuadVertexArrays(params[0], params[1]);
    }
}
