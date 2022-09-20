package com.ideacarry.example20.split;

import android.content.Context;
import android.opengl.GLES30;

import com.ideacarry.stable.filter.BaseFilter;
import com.ideacarry.utils.CommonUtils;
import com.ideacarry.utils.GLShaderProgram;
import com.ideacarry.utils.TextureManager;

import java.util.Arrays;

public class SplitFilter extends BaseFilter {

    private GLShaderProgram mProgram;
    private static final int COLUMN = 2;
    private static final int ROW = 2;

    @Override
    public void onCreate(Context context) {
        mProgram = new GLShaderProgram(new String(CommonUtils.readAssetFile(context, "example20/split/vs_split.glsl")),
                new String(CommonUtils.readAssetFile(context, "example20/split/fs_split.glsl")));

        mProgram.use();
        mProgram.setInt("s_texColor", 0);
        mProgram.setInt("u_column", COLUMN);
        mProgram.setInt("u_row", ROW);
    }

    public static float[] getTextureOffset(int viewWidth, int viewHeight, int column, int row) {
        float targetWidth = (float) viewWidth / column;
        float targetHeight = (float) viewHeight / row;
        float targetRatio = targetWidth / targetHeight;
        float outWidth = viewWidth;
        float outHeight = outWidth / targetRatio;
        if (outHeight > viewHeight) {
            outHeight = viewHeight;
            outWidth = outHeight * targetRatio;
        }
        float leftOffset = (viewWidth - outWidth) / 2f;
        float topOffset = (viewHeight - outHeight) / 2f;
        float x = leftOffset / viewWidth;
        float y = topOffset / viewHeight;
        float w = outWidth / viewWidth;
        float h = outHeight / viewHeight;
        return new float[]{x, y, w, h};
    }

    @Override
    public void onSizeChange(Context context, int viewWidth, int viewHeight, int cameraWidth, int cameraHeight, int degree, int flip) {
        if (mProgram != null) {
            if (viewWidth > 0 && viewHeight > 0) {
                mProgram.use();
                float[] rect = getTextureOffset(viewWidth, viewHeight, COLUMN, ROW);
                System.out.println(Arrays.toString(rect));
                mProgram.setVec4("u_texOffset", rect);
            }
        }
    }

    @Override
    public TextureManager.RendererData onDraw(TextureManager.RendererData data, int commonVao) {
        TextureManager.RendererData src = mTextureManager.getData(data.width, data.height, true);
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, src.framebuffer);

        GLES30.glClearColor(0.1f, 1.0f, 0.1f, 1.0f);
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT);

        mProgram.use();
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, data.texture);
        GLES30.glBindVertexArray(commonVao);
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4);

        //clear
        GLES30.glBindVertexArray(0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0);
        GLES30.glUseProgram(0);
        mTextureManager.release(data);

        return src;
    }

    @Override
    public void onDestroy(Context context) {
        if (mProgram != null) {
            mProgram.release();
            mProgram = null;
        }
    }
}
