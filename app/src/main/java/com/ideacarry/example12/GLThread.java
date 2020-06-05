package com.ideacarry.example12;

import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLExt;
import android.opengl.EGLSurface;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.Surface;

public class GLThread extends HandlerThread {

    private GLSurfaceView.Renderer mRenderer;
    private EGLConfig eglConfig = null;
    private EGLDisplay eglDisplay = EGL14.EGL_NO_DISPLAY;
    private EGLContext eglContext = EGL14.EGL_NO_CONTEXT;
    private EGLSurface eglSurface;
    private Handler mHandler;

    public GLThread(GLSurfaceView.Renderer renderer) {
        super("GLThread" + Math.random());
        mRenderer = renderer;
    }

    /**
     * 创建OpenGL环境
     */
    private void createGL() {
        // 获取显示设备(默认的显示设备)
        eglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
        // 初始化
        int[] version = new int[2];
        if (!EGL14.eglInitialize(eglDisplay, version, 0, version, 1)) {
            throw new RuntimeException("EGL error " + EGL14.eglGetError());
        }
        // 获取FrameBuffer格式和能力
        int[] configAttribs = {
//                EGL14.EGL_BUFFER_SIZE, 32,
                EGL14.EGL_RED_SIZE, 8,
                EGL14.EGL_GREEN_SIZE, 8,
                EGL14.EGL_BLUE_SIZE, 8,
                EGL14.EGL_ALPHA_SIZE, 8,
                EGL14.EGL_DEPTH_SIZE, 16,
                EGL14.EGL_RENDERABLE_TYPE, EGLExt.EGL_OPENGL_ES3_BIT_KHR,
//                EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
//                EGL14.EGL_SURFACE_TYPE, EGL14.EGL_WINDOW_BIT,
                EGL14.EGL_NONE
        };
        int[] numConfigs = new int[1];
        EGLConfig[] configs = new EGLConfig[1];
        if (!EGL14.eglChooseConfig(eglDisplay, configAttribs, 0, configs, 0, configs.length, numConfigs, 0)) {
            throw new RuntimeException("EGL error " + EGL14.eglGetError());
        }
        eglConfig = configs[0];
        // 创建OpenGL上下文
        int[] contextAttribs = {
                EGL14.EGL_CONTEXT_CLIENT_VERSION, 3,
                EGL14.EGL_NONE
        };
        //System.out.println("1 " + EGL14.eglGetCurrentContext());
        eglContext = EGL14.eglCreateContext(eglDisplay, eglConfig, EGL14.EGL_NO_CONTEXT, contextAttribs, 0);
        //System.out.println("2 " + eglContext);
        if (eglContext == EGL14.EGL_NO_CONTEXT) {
            throw new RuntimeException("EGL error " + EGL14.eglGetError());
        }
    }

    /**
     * 销毁OpenGL环境
     */
    private void destroyGL() {
        if (eglSurface != null) {
            EGL14.eglDestroySurface(eglDisplay, eglSurface);
            eglSurface = null;
        }
        EGL14.eglDestroyContext(eglDisplay, eglContext);
        eglContext = EGL14.EGL_NO_CONTEXT;
        eglDisplay = EGL14.EGL_NO_DISPLAY;
    }

    @Override
    public synchronized void start() {
        super.start();

        getHandler().post(() -> {
            createGL();
        });
    }

    public void release() {
        mHandler.post(() -> {
            destroyGL();
            quit();
        });
    }

    public Handler getHandler() {
        if (mHandler == null) {
            mHandler = new Handler(getLooper());
        }
        return mHandler;
    }

    public void glCreate(final Surface surface) {
        mHandler.post(() -> {
            if (eglSurface != null) {
                EGL14.eglDestroySurface(eglDisplay, eglSurface);
                eglSurface = null;
            }
            final int[] surfaceAttribs = {EGL14.EGL_NONE};
            eglSurface = EGL14.eglCreateWindowSurface(eglDisplay, eglConfig, surface, surfaceAttribs, 0);
            EGL14.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext);
            //destroy
            //EGL14.eglMakeCurrent(eglDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT);

            mRenderer.onSurfaceCreated(null, null);
        });
    }

    public void render(final Surface surface, final int width, final int height) {
        mHandler.post(() -> {
            mRenderer.onSurfaceChanged(null, width, height);

            drawFrame();
        });
    }

    private Runnable mRunnable = () -> {
        mRenderer.onDrawFrame(null);

        // 交换显存(将surface显存和显示器的显存交换)
        EGL14.eglSwapBuffers(eglDisplay, eglSurface);

        drawFrame();
    };

    private void drawFrame() {
        mHandler.removeCallbacks(mRunnable);
        mHandler.postDelayed(mRunnable, 20);
    }
}
