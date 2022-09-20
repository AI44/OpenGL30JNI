package com.ideacarry.example20.colordissolve;

import android.content.Context;
import android.opengl.GLES30;

import com.ideacarry.stable.filter.BaseFilter;
import com.ideacarry.utils.CommonUtils;
import com.ideacarry.utils.GLShaderProgram;
import com.ideacarry.utils.GLUtils;
import com.ideacarry.utils.TextureManager;

public class ColorDissolveFilter extends BaseFilter {

    private static final long DURATION_MS = 6000;

    private GLShaderProgram mProgram;
    private int mSecondTexture;
    private long mTime;

    @Override
    public void onCreate(Context context) {
        mProgram = new GLShaderProgram(new String(CommonUtils.readAssetFile(context, "example20/colordissolve/vs_colordissolve.glsl")),
                new String(CommonUtils.readAssetFile(context, "example20/colordissolve/fs_colordissolve.glsl")));
        mSecondTexture = GLUtils.createTextureFromAssets(context, "example20/colordissolve/test_img2.jpg");

        mProgram.use();
        mProgram.setInt("s_texColor", 0);
        mProgram.setInt("s_texColor1", 1);
        mTime = System.currentTimeMillis();
    }

    @Override
    public void onSizeChange(Context context, int viewWidth, int viewHeight, int cameraWidth, int cameraHeight, int degree, int flip) {
    }

    @Override
    public TextureManager.RendererData onDraw(TextureManager.RendererData data, int commonVao) {
        float progress = (System.currentTimeMillis() - mTime) / (float)DURATION_MS;
        progress = (float) Math.abs(Math.sin(progress * Math.PI * 2f));
        System.out.println(progress);

        TextureManager.RendererData src = mTextureManager.getData(data.width, data.height, true);
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, src.framebuffer);

        GLES30.glClearColor(0.1f, 1.0f, 0.1f, 1.0f);
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT);

        mProgram.use();
        mProgram.setFloat("s_progress", progress);
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, data.texture);
        GLES30.glActiveTexture(GLES30.GL_TEXTURE1);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mSecondTexture);
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
