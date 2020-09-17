package com.ideacarry.example13;

import android.content.Context;
import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GLRenderer implements GLSurfaceView.Renderer {

    private Context context;
    private int bgColor;
    private volatile int mMode;

    public GLRenderer(Context context, int bgColor) {
        this.context = context;
        this.bgColor = bgColor;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        surfaceCreated(context, bgColor);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        surfaceChanged(width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        drawFrame(mMode);
    }

    private native void surfaceCreated(Context context, int bgColor);

    private native void surfaceChanged(int width, int height);

    private native void drawFrame(int mode);

    public void setMode(int mode) {
        mMode = mode;
    }
}
