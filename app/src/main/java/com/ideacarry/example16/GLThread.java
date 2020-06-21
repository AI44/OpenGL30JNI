package com.ideacarry.example16;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.EGL14;
import android.opengl.GLES11Ext;
import android.opengl.GLES30;
import android.opengl.Matrix;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.TextureView;

import androidx.annotation.NonNull;

import com.android.grafika.gles.EglCore;
import com.android.grafika.gles.WindowSurface;

import java.util.concurrent.Executor;

public class GLThread extends HandlerThread implements Executor, TextureView.SurfaceTextureListener, SurfaceTexture.OnFrameAvailableListener {
    private Context mContext;
    private int mTextureId;
    private EglCore mEglCore;
    private WindowSurface mWindowSurface;
    private Handler mHandler;
    private volatile boolean mIsDestroy;

    private int mCameraWidth = -1;
    private int mCameraHeight = -1;
    private int mViewWidth = -1;
    private int mViewHeight = -1;

    public GLThread(Context context) {
        super("glThread");
        mContext = context.getApplicationContext();
    }

    public Handler getHandler() {
        if (mHandler == null) {
            mHandler = new Handler(getLooper());
        }
        return mHandler;
    }

    @Override
    public synchronized void start() {
        super.start();

        getHandler().post(() -> {
            mEglCore = new EglCore(EGL14.EGL_NO_CONTEXT, EglCore.FLAG_TRY_GLES3);
            mTextureId = createTextureObject();
        });
    }

    public int getTextureId() {
        return mTextureId;
    }

    private void releaseRaw() {
        deleteTextureObject(mTextureId);
        if (mWindowSurface != null) {
            mWindowSurface.release();
            mWindowSurface = null;
        }
    }

    public void release() {
        mHandler.post(this::releaseRaw);
        quitSafely();
        mIsDestroy = true;
    }

    //Executor --------------------------------------------------------------------------------start
    @Override
    public void execute(@NonNull Runnable command) {
        if (!mIsDestroy) {
            getHandler().post(command);
        }
    }
    //Executor ----------------------------------------------------------------------------------end

    public static void deleteTextureObject(int textureId) {
        GLES30.glDeleteTextures(1, new int[]{textureId}, 0);
    }

    public static int createTextureObject() {
        int[] texture = new int[1];
        GLES30.glGenTextures(1, texture, 0);
        GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture[0]);
        GLES30.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR);
        GLES30.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
        GLES30.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
        GLES30.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);
        return texture[0];
    }

    //TextureView.SurfaceTextureListener ------------------------------------------------------start
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        mIsDestroy = false;
        System.out.println("surfaceCreated " + Thread.currentThread().getId());
        mHandler.post(() -> {
            mViewWidth = width;
            mViewHeight = height;

            releaseRaw();

            mWindowSurface = new WindowSurface(mEglCore, surface);
            mWindowSurface.makeCurrent();

            GLRenderer.surfaceCreated(mContext);

            onSizeChange();
        });
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        System.out.println("onSurfaceTextureSizeChanged " + Thread.currentThread().getId());
        if (!mIsDestroy) {
            mHandler.post(() -> {
                mViewWidth = width;
                mViewHeight = height;
                onSizeChange();
            });
        }
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        mIsDestroy = true;
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        //System.out.println("onSurfaceTextureUpdated " + Thread.currentThread().getId());
    }
    //TextureView.SurfaceTextureListener --------------------------------------------------------end

    public void swapBuffers() {
        if (!mIsDestroy && mWindowSurface != null) {
            mWindowSurface.swapBuffers();
        }
    }

    private float[] mMatrix;

    private float[] getMatrix() {
        if (mMatrix == null) {
            mMatrix = new float[16];
            Matrix.setIdentityM(mMatrix, 0);
            if (mViewWidth > 0 && mViewHeight > 0 && mCameraWidth > 0 && mCameraHeight > 0) {
                //维持camera比例，填满view区域
                float normalScale = (float) mCameraWidth / mCameraHeight;
                float scale = (float) mViewWidth / mViewHeight;
                if (normalScale > scale) {
                    //System.out.println("x = " + normalScale / scale);
                    Matrix.scaleM(mMatrix, 0, normalScale / scale, 1, 1);
                } else {
                    //System.out.println("y = " + scale / normalScale);
                    Matrix.scaleM(mMatrix, 0, 1, scale / normalScale, 1);
                }
            }
            Matrix.rotateM(mMatrix, 0, -mCameraDegree, 0, 0, 1);
        }
        return mMatrix;
    }

    //SurfaceTexture.OnFrameAvailableListener -------------------------------------------------start
    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        surfaceTexture.updateTexImage();

        float[] matrix = getMatrix();
        if (matrix != null) {
            GLRenderer.drawFrame(getTextureId(), matrix);
            // 交换显存(将surface显存和显示器的显存交换)
            swapBuffers();
        }
    }
    //SurfaceTexture.OnFrameAvailableListener ---------------------------------------------------end

    private volatile int mCameraDegree;

    public void setCameraRotate(int degree) {
        mCameraDegree = degree;
    }

    public void setCameraSize(int w, int h) {
        mCameraWidth = w;
        mCameraHeight = h;

        onSizeChange();
    }

    private void onSizeChange() {
        mMatrix = null;
        if (mViewWidth > 0 && mViewHeight > 0 && mCameraWidth > 0 && mCameraHeight > 0) {
            GLRenderer.surfaceChanged(mViewWidth, mViewHeight, mCameraWidth, mCameraHeight, mCameraDegree);
        }
    }
}
