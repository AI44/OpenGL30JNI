package com.ideacarry.example20.filter;

import android.content.Context;
import android.opengl.GLES30;

import com.ideacarry.stable.filter.BaseFilter;
import com.ideacarry.utils.TextureManager;

/**
 * Created by Raining on 2020/9/2.
 *
 * @see <a href="https://github.com/CainKernel/CainCamera">CainCamera</a>
 */
public class BeautyFilter extends BaseFilter {
    // 美肤滤镜
    private BeautyComplexionUnitFilter mComplexionFilter;
    // 高斯模糊
    private BeautyBlurUnitFilter mBeautyBlurFilter;
    // 高通滤波
    private BeautyHighPassUnitFilter mHighPassFilter;
    // 高通滤波做高斯模糊处理，保留边沿细节
    private GaussPassUnitFilter mHighPassBlurFilter;
    // 磨皮程度调节滤镜
    private BeautyAdjustUnitFilter mBeautyAdjustFilter;

    public BeautyFilter() {
        mComplexionFilter = new BeautyComplexionUnitFilter();
        mBeautyBlurFilter = new BeautyBlurUnitFilter();
        mHighPassFilter = new BeautyHighPassUnitFilter();
        mHighPassBlurFilter = new GaussPassUnitFilter();
        mBeautyAdjustFilter = new BeautyAdjustUnitFilter();
    }

    @Override
    public void onCreate(Context context) {
        mComplexionFilter.onCreate(context);
        mBeautyBlurFilter.onCreate(context);
        mHighPassFilter.onCreate(context);
        mHighPassBlurFilter.onCreate(context);
        mBeautyAdjustFilter.onCreate(context);
    }

    @Override
    public void onSizeChange(Context context, int viewWidth, int viewHeight, int cameraWidth, int cameraHeight, int degree, int flip) {
        mComplexionFilter.onSizeChange(context, viewWidth, viewHeight, cameraWidth, cameraHeight, degree, flip);
        mBeautyBlurFilter.onSizeChange(context, viewWidth, viewHeight, cameraWidth, cameraHeight, degree, flip);
        mHighPassFilter.onSizeChange(context, viewWidth, viewHeight, cameraWidth, cameraHeight, degree, flip);
        mHighPassBlurFilter.onSizeChange(context, viewWidth, viewHeight, cameraWidth, cameraHeight, degree, flip);
        mBeautyAdjustFilter.onSizeChange(context, viewWidth, viewHeight, cameraWidth, cameraHeight, degree, flip);
    }

    @Override
    public TextureManager.RendererData onDraw(TextureManager.RendererData data, int commonVao) {
        int w;
        int h;
        TextureManager.RendererData temp;
        TextureManager.RendererData src;
        TextureManager.RendererData blur;
        TextureManager.RendererData highPassBlur;

        // 美肤滤镜
        w = data.width;
        h = data.height;
        GLES30.glViewport(0, 0, w, h);
        src = mTextureManager.getData(w, h, true);
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, src.framebuffer);
        mComplexionFilter.onDraw(w, h, commonVao, data.texture);
        mTextureManager.release(data);//清理
        data = src;

        // 高斯模糊
        w = data.width / 3;
        h = data.height / 3;
        GLES30.glViewport(0, 0, w, h);
        // 水平
        temp = mTextureManager.getData(w, h, true);
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, temp.framebuffer);
        mBeautyBlurFilter.setOffset(1f / w, 0);
        mBeautyBlurFilter.onDraw(w, h, commonVao, data.texture);
        // 垂直
        blur = mTextureManager.getData(w, h, true);
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, blur.framebuffer);
        mBeautyBlurFilter.setOffset(0, 1f / h);
        mBeautyBlurFilter.onDraw(w, h, commonVao, temp.texture);
        mTextureManager.release(temp);
        data = blur;

        // 高通滤波，做高反差保留
        w = data.width;
        h = data.height;
        GLES30.glViewport(0, 0, w, h);
        temp = mTextureManager.getData(w, h, true);
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, temp.framebuffer);
        mHighPassFilter.onDraw(w, h, commonVao, src.texture, blur.texture);
        data = temp;

        // 对高反差保留的结果进行高斯模糊，过滤边沿数值
        w = data.width;
        h = data.height;
        GLES30.glViewport(0, 0, w, h);
        // 水平
        temp = mTextureManager.getData(w, h, true);
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, temp.framebuffer);
        mHighPassBlurFilter.setOffset(1f / w, 0);
        mHighPassBlurFilter.onDraw(w, h, commonVao, data.texture);
        mTextureManager.release(data);
        // 垂直
        highPassBlur = mTextureManager.getData(w, h, true);
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, highPassBlur.framebuffer);
        mHighPassBlurFilter.setOffset(0, 1f / h);
        mHighPassBlurFilter.onDraw(w, h, commonVao, temp.texture);
        mTextureManager.release(temp);
        data = highPassBlur;

        //调节
        w = src.width;
        h = src.height;
        GLES30.glViewport(0, 0, w, h);
        temp = mTextureManager.getData(w, h, true);
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, temp.framebuffer);
        mBeautyAdjustFilter.onDraw(w, h, commonVao, src.texture, blur.texture, highPassBlur.texture);
        data = temp;

        //clear
        mTextureManager.release(src);
        mTextureManager.release(blur);
        mTextureManager.release(highPassBlur);
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);

        return data;
    }

    @Override
    public void onDestroy(Context context) {
        mComplexionFilter.onDestroy(context);
        mBeautyBlurFilter.onDestroy(context);
        mHighPassFilter.onDestroy(context);
        mHighPassBlurFilter.onDestroy(context);
        mBeautyAdjustFilter.onDestroy(context);
    }
}
