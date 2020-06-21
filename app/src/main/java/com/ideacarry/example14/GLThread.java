package com.ideacarry.example14;

import android.content.Context;
import android.opengl.EGL14;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.SurfaceHolder;

import androidx.annotation.NonNull;

import com.android.grafika.gles.EglCore;
import com.android.grafika.gles.WindowSurface;

import java.util.concurrent.Executor;

public class GLThread extends HandlerThread implements Executor, SurfaceHolder.Callback {
    private Context mContext;
    private EglCore mEglCore;
    private WindowSurface mWindowSurface;
    private Handler mHandler;
    private volatile boolean mIsDestroy;

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

        getHandler().post(() -> mEglCore = new EglCore(EGL14.EGL_NO_CONTEXT, EglCore.FLAG_TRY_GLES3));
    }

    public EglCore getEglCore() {
        return mEglCore;
    }

    private void releaseRaw() {
        if (mWindowSurface != null) {
            mWindowSurface.release();
            mWindowSurface = null;
        }
    }

    public void release() {
        mHandler.post(this::releaseRaw);
        quitSafely();
    }

    @Override
    public void execute(@NonNull Runnable command) {
        getHandler().post(command);
    }

    @Override
    public void surfaceCreated(final SurfaceHolder holder) {
        mIsDestroy = false;
        System.out.println("surfaceCreated " + Thread.currentThread().getId());
        mHandler.post(() -> {
            releaseRaw();

            mWindowSurface = new WindowSurface(mEglCore, holder.getSurface(), false);
            mWindowSurface.makeCurrent();

            GLRenderer.surfaceCreated(mContext);
        });
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (!mIsDestroy) {
            mHandler.post(() -> GLRenderer.surfaceChanged(width, height));
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mIsDestroy = true;
    }

    public void swapBuffers() {
        if (!mIsDestroy && mWindowSurface != null) {
            mWindowSurface.swapBuffers();
        }
    }
}
