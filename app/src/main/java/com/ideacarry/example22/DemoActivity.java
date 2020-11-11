package com.ideacarry.example22;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.SeekBar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.ideacarry.opengl30jni.databinding.Example22Binding;
import com.ideacarry.utils.CommonUtils;
import com.ideacarry.utils.GLShaderProgram;
import com.ideacarry.utils.GLUtils;

import java.util.Arrays;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Raining on 2020/11/4
 * #I# 测试光效
 */
public class DemoActivity extends AppCompatActivity implements GLSurfaceView.Renderer {
    private Example22Binding mBinding;
    private float mDegree = 0;
    private float mScale = 0.7f;

    private Bitmap mBmp; //居中缩放到屏幕
    private Bitmap mLight;

    private float mTranslateX;//屏幕坐标
    private float mTranslateY;

    private float oldX;
    private float oldY;
    private float downX;
    private float downY;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            options.inPreferQualityOverSpeed = true;
            options.inMutable = true;
            mBmp = BitmapFactory.decodeStream(getAssets().open("example22/test2.jpg"), null, options);
            mLight = BitmapFactory.decodeStream(getAssets().open("example22/light.png"), null, options);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        mBinding = Example22Binding.inflate(getLayoutInflater());
        mBinding.glSurfaceView.setEGLContextClientVersion(3);
        mBinding.glSurfaceView.setRenderer(this);
        mBinding.glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        setContentView(mBinding.getRoot());

        mBinding.glSurfaceView.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    oldX = mTranslateX;
                    oldY = mTranslateY;
                    downX = event.getX();
                    downY = event.getY();
                    break;
                }
                case MotionEvent.ACTION_MOVE: {
                    mTranslateX = oldX + event.getX() - downX;
                    mTranslateY = oldY + event.getY() - downY;
                    mBinding.glSurfaceView.requestRender();
                    break;
                }
            }
            return true;
        });

        mBinding.rotate.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mDegree = changeRange(progress, 0, 100, -180, 180);
                    mBinding.glSurfaceView.requestRender();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        mBinding.scale.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mScale = changeRange(progress, 0, 100, 0.2f, 1.2f);
                    mBinding.glSurfaceView.requestRender();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    @Override
    protected void onStart() {
        CommonUtils.activityFullScreen(this);

        super.onStart();
    }

    public static float changeRange(float value, float srcRangeA, float srcRangeB, float dstRangeA, float dstRangeB) {
        float srcRange = Math.abs(srcRangeA - srcRangeB);
        float srcOffset = Math.min(srcRangeA, srcRangeB);
        float dstRange = Math.abs(dstRangeA - dstRangeB);
        float dstOffset = Math.min(dstRangeA, dstRangeB);
        return (value - srcOffset) / srcRange * dstRange + dstOffset;
    }

    private final float[] mMatrix = new float[16]; //光效相对图片的矩阵
    private final float[] mInvertMatrix = new float[16];
    private GLShaderProgram mProgram;
    private int mBmpTexture;
    private int mLightTexture;
    private int mVao = -1;
    private int mVbo = -1;

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mProgram = new GLShaderProgram(new String(CommonUtils.readAssetFile(this, "example22/vertex.glsl")),
                new String(CommonUtils.readAssetFile(this, "example22/fragment.glsl")));
        mProgram.use();
        mProgram.setInt("imageTexture", 0);
        mProgram.setInt("lightTexture", 1);
        mProgram.setFloat("lightWidth", mLight.getWidth());
        mProgram.setFloat("lightHeight", mLight.getHeight());

        mBmpTexture = GLUtils.createTexture(mBmp);
        mLightTexture = GLUtils.createTexture(mLight);

        int[] params = GLUtils.createQuadVertexArrays(0, 1);
        mVao = params[0];
        mVbo = params[1];
    }

    private int mSurfaceWidth;
    private int mSurfaceHeight;

    private int mViewWidth;//跟用户图片一样比例
    private int mViewHeight;
    private int mViewX;
    private int mViewY;
    private float mViewScale;

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        mSurfaceWidth = width;
        mSurfaceHeight = height;

        final int dstW = mSurfaceWidth;
        final int dstH = mSurfaceHeight;
        //final int dstW = 1000;
        //final int dstH = 500;

        float ratio = (float) mBmp.getWidth() / (float) mBmp.getHeight();
        float h = dstH;
        float w = h * ratio;
        if (w > dstW) {
            w = dstW;
            h = w / ratio;
        }
        mViewWidth = Math.round(w) >> 1 << 1;
        mViewHeight = Math.round(h) >> 1 << 1;
        mViewX = (mSurfaceWidth - mViewWidth) / 2;
        mViewY = (mSurfaceHeight - mViewHeight) / 2;
        GLES30.glViewport(mViewX, mViewY, mViewWidth, mViewHeight);
        mViewScale = Math.max(mBmp.getWidth() / (float) mViewWidth, mBmp.getHeight() / (float) mViewHeight);

        mProgram.use();
        mProgram.setFloat("screenWidth", mViewWidth);
        mProgram.setFloat("screenHeight", mViewHeight);

        mTranslateX = (mViewWidth - mLight.getWidth()) / 2f;//居中
        mTranslateY = (mViewHeight - mLight.getHeight()) / 2f;
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        Matrix.setIdentityM(mMatrix, 0);
        Matrix.setIdentityM(mInvertMatrix, 0);
        //矩阵乘法从右往左看
        Matrix.translateM(mMatrix, 0, mTranslateX, mTranslateY, 0);
        float cx = mLight.getWidth() / 2f;
        float cy = mLight.getHeight() / 2f;
        Matrix.translateM(mMatrix, 0, cx, cy, 0);
        Matrix.rotateM(mMatrix, 0, mDegree, 0, 0, 1);
        Matrix.scaleM(mMatrix, 0, mScale, mScale, 1);
        Matrix.translateM(mMatrix, 0, -cx, -cy, 0);
        Matrix.invertM(mInvertMatrix, 0, mMatrix, 0);

        //--------------------------------------------------------------------------------test start
        /*System.out.println("-----------------------");
        float[] resultVec = new float[4];
        Matrix.multiplyMV(resultVec, 0, mInvertMatrix, 0, new float[]{0, 0, 0, 1f}, 0);
        resultVec[0] /= mLight.getWidth();
        resultVec[1] /= mLight.getHeight();
        System.out.println(Arrays.toString(resultVec));

        Matrix.multiplyMV(resultVec, 0, mInvertMatrix, 0, new float[]{0, mViewHeight, 0, 1f}, 0);
        resultVec[0] /= mLight.getWidth();
        resultVec[1] /= mLight.getHeight();
        System.out.println(Arrays.toString(resultVec));

        Matrix.multiplyMV(resultVec, 0, mInvertMatrix, 0, new float[]{mViewWidth, mViewHeight, 0, 1f}, 0);
        resultVec[0] /= mLight.getWidth();
        resultVec[1] /= mLight.getHeight();
        System.out.println(Arrays.toString(resultVec));

        Matrix.multiplyMV(resultVec, 0, mInvertMatrix, 0, new float[]{mViewWidth, 0, 0, 1f}, 0);
        resultVec[0] /= mLight.getWidth();
        resultVec[1] /= mLight.getHeight();
        System.out.println(Arrays.toString(resultVec));*/
        //----------------------------------------------------------------------------------test end

        GLES30.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT);

        mProgram.use();
        mProgram.setMat4("matrix", mInvertMatrix);
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mBmpTexture);
        GLES30.glActiveTexture(GLES30.GL_TEXTURE1);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mLightTexture);

        GLES30.glBindVertexArray(mVao);
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4);

        //clear
        GLES30.glBindVertexArray(0);
        GLES30.glUseProgram(0);
    }
}
