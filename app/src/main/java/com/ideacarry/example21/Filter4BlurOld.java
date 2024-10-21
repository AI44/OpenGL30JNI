package com.ideacarry.example21;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES30;

import com.ideacarry.example20.filter.BeautyBlurUnitFilter;
import com.ideacarry.utils.GLUtils;
import com.ideacarry.utils.TextureManager;

public class Filter4BlurOld extends BaseFilter {
    public Filter4BlurOld(Context context, Bitmap bmp, IResult<Bitmap> result) {
        super(context, bmp, result);
    }

    @Override
    public void doFilter(int w, int h, int textureId) {
        BeautyBlurUnitFilter filter = new BeautyBlurUnitFilter();
        filter.onCreate(mContext);
        filter.onSizeChange(mContext, w, h, w, h, 0, 0);

        int[] params = GLUtils.createQuadVertexArrays(0, 1);
        TextureManager manager = new TextureManager();

        GLES30.glViewport(0, 0, w, h);

        //水平
        TextureManager.RendererData data = manager.getData(w, h, false);
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, data.framebuffer);
        filter.setOffset(1.0f / w, 0);
        filter.onDraw(w, h, params[0], textureId);

        //垂直
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);
        filter.setOffset(0, 1.0f / h);
        filter.onDraw(w, h, params[0], data.texture);
        manager.release(data);

        filter.onDestroy(mContext);
        manager.clear();
        GLUtils.deleteQuadVertexArrays(params[0], params[1]);
    }
}
