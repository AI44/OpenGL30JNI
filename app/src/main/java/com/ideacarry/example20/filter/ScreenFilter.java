package com.ideacarry.example20.filter;

import android.content.Context;
import android.opengl.GLES30;

import com.ideacarry.stable.filter.BaseFilter;
import com.ideacarry.utils.TextureManager;

/**
 * Created by Raining on 2020/9/2.
 */
public class ScreenFilter extends BaseFilter {
    private ScreenUnitFilter mFilter;
    private int mViewWidth = -1;
    private int mViewHeight = -1;

    public ScreenFilter() {
        mFilter = new ScreenUnitFilter();
    }

    @Override
    public void onCreate(Context context) {
        mFilter.onCreate(context);
    }

    @Override
    public void onSizeChange(Context context, int viewWidth, int viewHeight, int cameraWidth, int cameraHeight, int degree, int flip) {
        mFilter.onSizeChange(context, viewWidth, viewHeight, cameraWidth, cameraHeight, degree, flip);
        mViewWidth = viewWidth;
        mViewHeight = viewHeight;
    }

    @Override
    public TextureManager.RendererData onDraw(TextureManager.RendererData data, int commonVao) {
        GLES30.glViewport(0, 0, mViewWidth, mViewHeight);
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);

        mFilter.onDraw(data.width, data.height, commonVao, data.texture);

        return data;
    }

    @Override
    public void onDestroy(Context context) {
        mFilter.onDestroy(context);
    }
}
