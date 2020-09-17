package com.ideacarry.example20.filter;

import android.content.Context;
import android.opengl.GLES30;
import android.util.Size;

import com.ideacarry.stable.filter.BaseFilter;
import com.ideacarry.utils.GLUtils;
import com.ideacarry.utils.TextureManager;

/**
 * Created by Raining on 2020/9/2.
 */
public class CameraFilter extends BaseFilter {
    private CameraUnitFilter mFilter;
    private int mTextureWidth;
    private int mTextureHeight;

    public CameraFilter() {
        mFilter = new CameraUnitFilter();
    }

    @Override
    public void onCreate(Context context) {
        mFilter.onCreate(context);
    }

    @Override
    public void onSizeChange(Context context, int viewWidth, int viewHeight, int cameraWidth, int cameraHeight, int degree, int flip) {
        Size size = GLUtils.getTextureSize(viewWidth, viewHeight, cameraWidth, cameraHeight);
        mTextureWidth = size.getWidth();
        mTextureHeight = size.getHeight();

        mFilter.onSizeChange(context, viewWidth, viewHeight, cameraWidth, cameraHeight, degree, flip);
    }

    @Override
    public TextureManager.RendererData onDraw(TextureManager.RendererData data, int commonVao) {
        GLES30.glViewport(0, 0, mTextureWidth, mTextureHeight);
        TextureManager.RendererData result = mTextureManager.getData(mTextureWidth, mTextureHeight, true);
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, result.framebuffer);

        mFilter.onDraw(data.width, data.height, commonVao, data.texture);

        //clear
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);
        return result;
    }

    @Override
    public void onDestroy(Context context) {
        mFilter.onDestroy(context);
    }
}
