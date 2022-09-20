package com.ideacarry.example20.softglow;

import android.content.Context;
import android.opengl.GLES30;

import com.ideacarry.example20.filter.BeautyBlurUnitFilter;
import com.ideacarry.stable.filter.BaseFilter;
import com.ideacarry.utils.CommonUtils;
import com.ideacarry.utils.GLShaderProgram;
import com.ideacarry.utils.TextureManager;

public class SoftGlowFilter extends BaseFilter {

    private final BeautyBlurUnitFilter mBlurFilter;
    private GLShaderProgram mProgram;
    private final static float BLUR_SIZE = 4.0f;

    public SoftGlowFilter() {
        mBlurFilter = new BeautyBlurUnitFilter();
    }

    @Override
    public void onCreate(Context context) {
        mBlurFilter.onCreate(context);
        mProgram = new GLShaderProgram(new String(CommonUtils.readAssetFile(context, "example20/softglow/vs_softglow.glsl")),
                new String(CommonUtils.readAssetFile(context, "example20/softglow/fs_softglow.glsl")));
        mProgram.use();
        mProgram.setInt("s_texColor", 0);
        mProgram.setInt("s_blurColor", 1);
    }

    @Override
    public void onSizeChange(Context context, int viewWidth, int viewHeight, int cameraWidth, int cameraHeight, int degree, int flip) {
        mBlurFilter.onSizeChange(context, viewWidth, viewHeight, cameraWidth, cameraHeight, degree, flip);
    }

    @Override
    public TextureManager.RendererData onDraw(TextureManager.RendererData data, int commonVao) {
        int w;
        int h;
        TextureManager.RendererData temp;
        TextureManager.RendererData blur;

        w = data.width;
        h = data.height;
        GLES30.glViewport(0, 0, w, h);
        // 水平
        temp = mTextureManager.getData(w, h, true);
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, temp.framebuffer);
        mBlurFilter.setOffset(BLUR_SIZE / w, 0);
        mBlurFilter.onDraw(w, h, commonVao, data.texture);
        // 垂直
        blur = mTextureManager.getData(w, h, true);
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, blur.framebuffer);
        mBlurFilter.setOffset(0, BLUR_SIZE / h);
        mBlurFilter.onDraw(w, h, commonVao, temp.texture);
        mTextureManager.release(temp);

        // -- soft glow --
        temp = mTextureManager.getData(data.width, data.height, true);
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, temp.framebuffer);

        GLES30.glClearColor(0.1f, 1.0f, 0.1f, 1.0f);
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT);

        mProgram.use();
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, data.texture);
        GLES30.glActiveTexture(GLES30.GL_TEXTURE1);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, blur.texture);
        GLES30.glBindVertexArray(commonVao);
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4);

        //clear
        GLES30.glBindVertexArray(0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0);
        GLES30.glUseProgram(0);
        mTextureManager.release(data);
        mTextureManager.release(blur);

        return temp;
    }

    @Override
    public void onDestroy(Context context) {
        if (mProgram != null) {
            mProgram.release();
            mProgram = null;
        }
        mBlurFilter.onDestroy(context);
    }
}
