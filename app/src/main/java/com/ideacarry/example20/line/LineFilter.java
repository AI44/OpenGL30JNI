package com.ideacarry.example20.line;

import android.content.Context;
import android.graphics.PointF;
import android.opengl.GLES30;

import com.ideacarry.example20.mosaic.MosaicFilter;
import com.ideacarry.stable.filter.BaseFilter;
import com.ideacarry.utils.CommonUtils;
import com.ideacarry.utils.GLShaderProgram;
import com.ideacarry.utils.GLUtils;
import com.ideacarry.utils.TextureManager;

import java.util.Arrays;

public class LineFilter extends BaseFilter {

    private GLShaderProgram mProgram;
    private GLShaderProgram mMixProgram;
    private int mSecondTexture;
    private PointF[] mPoints;
    private int mPointVbo;

    private MosaicFilter mosaicFilter = new MosaicFilter();

    @Override
    public void onCreate(Context context) {
        mProgram = new GLShaderProgram(new String(CommonUtils.readAssetFile(context, "example20/line/vs_line.glsl")),
                new String(CommonUtils.readAssetFile(context, "example20/line/fs_line.glsl")));
        mMixProgram = new GLShaderProgram(new String(CommonUtils.readAssetFile(context, "example20/line/vs_mix.glsl")),
                new String(CommonUtils.readAssetFile(context, "example20/line/fs_mix.glsl")));
        mSecondTexture = GLUtils.createTextureFromAssets(context, "example20/line/brush1.png");

        mProgram.use();
        mProgram.setInt("s_texColor", 0);
        mProgram.setFloat("u_size", 100f);
        mProgram.setVec4("u_color", new float[]{1.0f, 0f, 0f, 1.0f});

        final float screenSize = 100f;
        PointF[] pp = pointsWithFrom(new PointF(0f, 0f), new PointF(80, 80), new PointF(-80f, 0f), 5);
        mPoints = new PointF[pp.length - 1];
        System.arraycopy(pp, 1, mPoints, 0, mPoints.length);
        for (PointF point : mPoints) {
            point.x /= screenSize;
            point.y /= screenSize;
        }
        System.out.println(Arrays.toString(mPoints));
        loadPoints(mPoints);

        mosaicFilter.setTextureManager(mTextureManager);
        mosaicFilter.onCreate(context);

        mMixProgram.use();
        mMixProgram.setInt("s_texOrg", 0);
        mMixProgram.setInt("s_texMosaic", 1);
        mMixProgram.setInt("s_texLine", 2);
    }

    private void loadPoints(PointF[] points) {
        int len = points.length;
        float[] floatArr = new float[len << 1];
        for (int i = 0; i < len; i++) {
            int index = i << 1;
            PointF point = points[i];
            floatArr[index] = point.x;
            floatArr[index + 1] = point.y;
        }
        System.out.println(Arrays.toString(floatArr));
        int[] vbo = {-1};
        GLES30.glGenBuffers(1, vbo, 0);
        mPointVbo = vbo[0];
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vbo[0]);
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, floatArr.length * 4, GLUtils.getFloatBuffer(floatArr), GLES30.GL_STATIC_DRAW);
        GLES30.glEnableVertexAttribArray(0);
        GLES30.glVertexAttribPointer(0, 2, GLES30.GL_FLOAT, false, 2 * 4, 0);
    }

    @Override
    public void onSizeChange(Context context, int viewWidth, int viewHeight, int cameraWidth, int cameraHeight, int degree, int flip) {
        mosaicFilter.onSizeChange(context, viewWidth, viewHeight, cameraWidth, cameraHeight, degree, flip);
    }

    @Override
    public TextureManager.RendererData onDraw(TextureManager.RendererData org, int commonVao) {

        // mosaic
        TextureManager.RendererData temp = new TextureManager.RendererData();
        temp.framebuffer = org.framebuffer;
        temp.height = org.height;
        temp.width = org.width;
        temp.texture = org.texture;
        TextureManager.RendererData mosaic = mosaicFilter.onDraw(temp, commonVao);

        // line
        TextureManager.RendererData line = mTextureManager.getData(org.width, org.height, true);
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, line.framebuffer);

        GLES30.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT);

        GLES30.glEnable(GLES30.GL_BLEND);
        GLES30.glBlendFunc(GLES30.GL_ONE, GLES30.GL_ONE_MINUS_SRC_ALPHA);

        mProgram.use();
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mSecondTexture);
        GLES30.glDrawArrays(GLES30.GL_POINTS, 0, mPoints.length);

        GLES30.glDisable(GLES30.GL_BLEND);

        // mix
        TextureManager.RendererData result = mTextureManager.getData(org.width, org.height, true);
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, result.framebuffer);

        GLES30.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT);

        mMixProgram.use();
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, org.texture);
        GLES30.glActiveTexture(GLES30.GL_TEXTURE1);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mosaic.texture);
        GLES30.glActiveTexture(GLES30.GL_TEXTURE2);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, line.texture);
        GLES30.glBindVertexArray(commonVao);
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4);

        //clear
        GLES30.glBindVertexArray(0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0);
        GLES30.glUseProgram(0);
        mTextureManager.release(line);
        mTextureManager.release(mosaic);
        mTextureManager.release(org);

        return result;
    }

    @Override
    public void onDestroy(Context context) {
        if (mProgram != null) {
            mProgram.release();
            mProgram = null;
        }
    }

    private Boolean isCenter(PointF centerPoint, PointF fromPoint, PointF toPoint) {
        boolean isCenterX = Math.abs((fromPoint.x + toPoint.x) / 2 - centerPoint.x) < 0.0001;
        boolean isCenterY = Math.abs((fromPoint.y + toPoint.y) / 2 - centerPoint.y) < 0.0001;

        return isCenterX && isCenterY;
    }

    /**
     * 长度函数
     *
     * @param t t 值
     * @param A 见【注意】
     * @param B 见【注意】
     * @param C 见【注意】
     * @return t 值对应的曲线长度
     */
    private float lengthWithT(float t,
                              float A,
                              float B,
                              float C) {
        if (A < 0.00001f) {
            return 0.0f;
        }

        double temp1 = Math.sqrt(C + t * (B + A * t));
        double temp2 = (2 * A * t * temp1 + B * (temp1 - Math.sqrt(C)));
        double temp3 = Math.log(Math.abs(B + 2 * Math.sqrt(A) * Math.sqrt(C) + 0.0001f));
        double temp4 = Math.log(Math.abs(B + 2 * A * t + 2 * Math.sqrt(A) * temp1) + 0.0001f);
        double temp5 = 2 * Math.sqrt(A) * temp2;
        double temp6 = (B * B - 4 * A * C) * (temp3 - temp4);

        return (float) ((temp5 + temp6) / (8 * Math.pow(A, 1.5f)));
    }

    private PointF[] pointsWithFrom(
            PointF from,
            PointF to,
            PointF control,
            int pointSize
    ) {

        PointF P0 = from;
        // 如果 control 是 from 和 to 的中点，则将 control 设置为和 from 重合
        PointF P1 = isCenter(control, from, to) ? from : control;
        PointF P2 = to;

        float ax = P0.x - 2 * P1.x + P2.x;
        float ay = P0.y - 2 * P1.y + P2.y;
        float bx = 2 * P1.x - 2 * P0.x;
        float by = 2 * P1.y - 2 * P0.y;

        float A = 4 * (ax * ax + ay * ay);
        float B = 4 * (ax * bx + ay * by);
        float C = bx * bx + by * by;

        float totalLength = lengthWithT(1, A, B, C);  // 整条曲线的长度
        float pointsPerLength = 5.0f / pointSize;  // 用点的尺寸计算出，单位长度需要多少个点
        int count = (int) Math.max(1, Math.ceil((pointsPerLength * totalLength)));  // 曲线应该生成的点数

        PointF[] mutArr = new PointF[count + 1];
        for (int i = 0; i <= count; ++i) {
            float t = i * 1.0f / count;
            float length = t * totalLength;
            t = tWithT(t, length, A, B, C);
            // 根据 t 求出坐标
            float x = (1 - t) * (1 - t) * P0.x + 2 * (1 - t) * t * P1.x + t * t * P2.x;
            float y = (1 - t) * (1 - t) * P0.y + 2 * (1 - t) * t * P1.y + t * t * P2.y;
            mutArr[i] = new PointF(x, y);
        }
        return mutArr;
    }

    /**
     * 长度函数反函数，根据 length，求出对应的 t，使用牛顿切线法求解
     *
     * @param t      给出的近似的 t，比如求长度占弧长 0.3 的 t，t 应该是接近 0.3，则传入近似值 0.3
     * @param length 目标弧长，实际长度，非占比
     * @param A      见【注意】
     * @param B      见【注意】
     * @param C      见【注意】
     * @return 结果 t 值
     */
    private float tWithT(
            float t,
            float length,
            float A,
            float B,
            float C
    ) {
        float t1 = t;
        float t2;

        while (true) {
            float speed = speedWithT(t1, A, B, C);
            if (speed < 0.0001f) {
                t2 = t1;
                break;
            }
            t2 = t1 - (lengthWithT(t1, A, B, C) - length) / speed;
            if (Math.abs(t1 - t2) < 0.0001f) {
                break;
            }
            t1 = t2;
        }
        return t2;
    }

    /**
     * 速度函数 s(t) = sqrt(A * t^2 + B * t + C)
     *
     * @param t t 值
     * @param A 见【注意】
     * @param B 见【注意】
     * @param C 见【注意】
     * @return 贝塞尔曲线某一点的速度
     */
    private float speedWithT(
            float t,
            float A,
            float B,
            float C
    ) {
        return (float) Math.sqrt(Math.max(A * Math.pow(t, 2.0) + B * t + C, 0));
    }
}
