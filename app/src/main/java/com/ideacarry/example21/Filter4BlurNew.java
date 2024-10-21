package com.ideacarry.example21;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES30;

import com.ideacarry.stable.filter.Gaussian5x5UnitFilter;
import com.ideacarry.utils.GLUtils;
import com.ideacarry.utils.TextureManager;

public class Filter4BlurNew extends BaseFilter {
    public Filter4BlurNew(Context context, Bitmap bmp, IResult<Bitmap> result) {
        super(context, bmp, result);
    }

    @Override
    public void doFilter(int w, int h, int textureId) {
        Gaussian5x5UnitFilter filter = new Gaussian5x5UnitFilter();
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

    public static void printGaussianKernel(float sigma) {
        sigma = sigma > 0 ? sigma : -sigma;
        //高斯核矩阵的大小为(6*sigma+1)*(6*sigma+1)
        int kSize = (int) Math.ceil(sigma * 3) * 2 + 1;
        //计算一维高斯核
        float[] kernel = new float[kSize];
        float scale = -0.5f / (sigma * sigma);
        float cons = 1.0f / (float) Math.sqrt(-scale / Math.PI);

        float sum = 0;
        int kCenter = kSize / 2;
        for (int i = 0; i < kSize; i++) {
            int x = i - kCenter;
            kernel[i] = cons * (float) Math.exp(x * x * scale);//一维高斯函数
            sum += kernel[i];
        }
        //归一化,确保高斯权值在[0,1]之间
        for (int i = 0; i < kSize; i++) {
            kernel[i] /= sum;
            System.out.println(kernel[i]);
        }
    }

    public static void printGaussianKernel2(float r) {
        int radius = (int) r;
        float sigma = r;
        int kSize = 2 * radius + 1;
        float[] kernel = new float[kSize];
        float sum = 0;
        for (int i = -radius; i <= radius; i++) {
            kernel[i + radius] = (float) Math.exp(-(float) i * i / (2 * sigma * sigma));
            sum += kernel[i + radius];
        }
        //归一化,确保高斯权值在[0,1]之间
        for (int i = 0; i < kSize; i++) {
            kernel[i] /= sum;
            System.out.println(kernel[i]);
        }
    }
}
