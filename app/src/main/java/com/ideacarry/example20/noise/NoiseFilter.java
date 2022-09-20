package com.ideacarry.example20.noise;

import android.content.Context;
import android.opengl.GLES30;

import com.ideacarry.stable.filter.BaseFilter;
import com.ideacarry.utils.CommonUtils;
import com.ideacarry.utils.GLShaderProgram;
import com.ideacarry.utils.TextureManager;

public class NoiseFilter extends BaseFilter {

    private GLShaderProgram mProgram;

    @Override
    public void onCreate(Context context) {
        mProgram = new GLShaderProgram(new String(CommonUtils.readAssetFile(context, "example20/noise/vs_noise.glsl")),
                new String(CommonUtils.readAssetFile(context, "example20/noise/fs_noise.glsl")));

        mProgram.use();
        mProgram.setInt("s_texColor", 0);
    }

    @Override
    public void onSizeChange(Context context, int viewWidth, int viewHeight, int cameraWidth, int cameraHeight, int degree, int flip) {
        if (mProgram != null) {
            mProgram.use();
            mProgram.setVec2("u_inputSize", (float) viewWidth, (float) viewHeight);
        }
    }

    @Override
    public TextureManager.RendererData onDraw(TextureManager.RendererData data, int commonVao) {
        TextureManager.RendererData src = mTextureManager.getData(data.width, data.height, true);
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, src.framebuffer);

        GLES30.glClearColor(0.1f, 1.0f, 0.1f, 1.0f);
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT);

        mProgram.use();
        mProgram.setFloat("u_progress", getCurrentProgress());
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

    private final static long CYCLE_TIME_MS = 500000;

    private float getCurrentProgress() {
        final long time = System.currentTimeMillis();
        final float progress = (time % CYCLE_TIME_MS) / (float) CYCLE_TIME_MS;
        //System.out.println(progress);
        return progress;
    }
}
