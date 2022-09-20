package com.ideacarry.example20.edgelight;

import android.content.Context;
import android.opengl.GLES30;

import com.ideacarry.example20.filter.BeautyBlurUnitFilter;
import com.ideacarry.stable.filter.BaseFilter;
import com.ideacarry.utils.CommonUtils;
import com.ideacarry.utils.GLShaderProgram;
import com.ideacarry.utils.TextureManager;

public class EdgeLightFilter extends BaseFilter {

    private final BeautyBlurUnitFilter mBlurFilter;
    private final static float BLUR_SIZE = 4.0f;

    private GLShaderProgram mSobelEdgeDetectionProgram;
    private GLShaderProgram mEdgeProgram;

    public EdgeLightFilter() {
        mBlurFilter = new BeautyBlurUnitFilter();
    }

    @Override
    public void onCreate(Context context) {
        mBlurFilter.onCreate(context);

        mSobelEdgeDetectionProgram = new GLShaderProgram(new String(CommonUtils.readAssetFile(context, "example20/edgelight/vs_general.glsl")),
                new String(CommonUtils.readAssetFile(context, "example20/edgelight/fs_sobel_edge_detection.glsl")));
        mSobelEdgeDetectionProgram.use();
        mSobelEdgeDetectionProgram.setInt("s_texColor", 0);
        mSobelEdgeDetectionProgram.setFloat("u_strength", 0.3f);

        mEdgeProgram = new GLShaderProgram(new String(CommonUtils.readAssetFile(context, "example20/edgelight/vs_general.glsl")),
                new String(CommonUtils.readAssetFile(context, "example20/edgelight/fs_edge_light.glsl")));
        mEdgeProgram.use();
        mEdgeProgram.setInt("s_texColor", 0);
        mEdgeProgram.setInt("s_edgeLightColor", 1);
    }

    @Override
    public void onSizeChange(Context context, int viewWidth, int viewHeight, int cameraWidth, int cameraHeight, int degree, int flip) {
        mBlurFilter.onSizeChange(context, viewWidth, viewHeight, cameraWidth, cameraHeight, degree, flip);

        mSobelEdgeDetectionProgram.use();
        mSobelEdgeDetectionProgram.setVec2("u_sobelStep", 1f / viewWidth, 1f / viewHeight);
    }

    @Override
    public TextureManager.RendererData onDraw(TextureManager.RendererData data, int commonVao) {
        int w = data.width;
        int h = data.height;
        TextureManager.RendererData temp;
        TextureManager.RendererData edge;
        TextureManager.RendererData blur;
        GLES30.glViewport(0, 0, w, h);

        // sobel edge detection
        edge = mTextureManager.getData(w, h, true);
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, edge.framebuffer);

        GLES30.glClearColor(0.1f, 1.0f, 0.1f, 1.0f);
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT);

        mSobelEdgeDetectionProgram.use();
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, data.texture);
        GLES30.glBindVertexArray(commonVao);
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4);

        GLES30.glBindVertexArray(0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0);
        GLES30.glUseProgram(0);

        // blur
        // 水平
        temp = mTextureManager.getData(w, h, true);
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, temp.framebuffer);
        mBlurFilter.setOffset(BLUR_SIZE / w, 0);
        mBlurFilter.onDraw(w, h, commonVao, edge.texture);
        mTextureManager.release(edge);
        // 垂直
        blur = mTextureManager.getData(w, h, true);
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, blur.framebuffer);
        mBlurFilter.setOffset(0, BLUR_SIZE / h);
        mBlurFilter.onDraw(w, h, commonVao, temp.texture);
        mTextureManager.release(temp);

        // edge
        temp = mTextureManager.getData(w, h, true);
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, temp.framebuffer);

        GLES30.glClearColor(0.1f, 1.0f, 0.1f, 1.0f);
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT);

        mEdgeProgram.use();
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
        if (mSobelEdgeDetectionProgram != null) {
            mSobelEdgeDetectionProgram.release();
            mSobelEdgeDetectionProgram = null;
        }
        if (mEdgeProgram != null) {
            mEdgeProgram.release();
            mEdgeProgram = null;
        }
        mBlurFilter.onDestroy(context);
    }
}
