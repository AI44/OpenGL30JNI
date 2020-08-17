package com.ideacarry.example19;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GLRenderer implements GLSurfaceView.Renderer {

    private Context context;
    private int bgColor;
    private Bitmap bmp;

    public GLRenderer(Context context, int bgColor) {
        this.context = context;
        this.bgColor = bgColor;
        try {
            bmp = BitmapFactory.decodeStream(context.getAssets().open("lenna_std.jpg"));
        } catch (Throwable e) {
            e.printStackTrace();
        }
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
        drawFrame(bmp);
    }

    private native void surfaceCreated(Context context, int bgColor);

    private native void surfaceChanged(int width, int height);

    private native void drawFrame(Bitmap bmp);
}