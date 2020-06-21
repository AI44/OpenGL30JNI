package com.ideacarry.example12;

import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * #I# 自建gl环境 + surfaceView
 */
public class DemoActivity extends AppCompatActivity {

    private GLThread glThread;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        glThread = new GLThread(new com.ideacarry.example1.GLRenderer(this, 0xff0000ff));
//        glThread = new GLThread(new GL2Renderer());
//        glThread = new GLThread(new GL3Renderer());
        glThread.start();

        SurfaceView surfaceView = new SurfaceView(this);
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                glThread.glCreate(holder.getSurface());
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                //System.out.println(width + " x " + height);
                glThread.render(holder.getSurface(), width, height);
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                glThread.glDestroy();
            }
        });
        setContentView(surfaceView);
    }

    @Override
    protected void onDestroy() {
        if (glThread != null) {
            glThread.release();
            glThread = null;
        }
        super.onDestroy();
    }
}
