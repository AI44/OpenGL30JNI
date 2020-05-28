package com.ideacarry.example1;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * 画三角形
 */
public class DemoActivity extends AppCompatActivity {

    private GLSurfaceView mGLSurfaceView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout fr = new FrameLayout(this);
        setContentView(fr);
        {
            FrameLayout.LayoutParams fl;

            mGLSurfaceView = new GLSurfaceView(this);
            mGLSurfaceView.setEGLContextClientVersion(3);
            mGLSurfaceView.setRenderer(new GLRenderer(this, 0xff0000ff));
            //mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
            mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
            fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
            fr.addView(mGLSurfaceView, fl);

            GLSurfaceView sv = new GLSurfaceView(this);
            sv.setEGLContextClientVersion(3);
            sv.setRenderer(new GL2Renderer());
            sv.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
            fl = new FrameLayout.LayoutParams(200, 200);
            fr.addView(sv, fl);
        }
    }
}
