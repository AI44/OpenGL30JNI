package com.ideacarry.example19;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class DemoActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout fr = new FrameLayout(this);
        setContentView(fr);
        {
            FrameLayout.LayoutParams fl;

            GLSurfaceView mGLSurfaceView = new GLSurfaceView(this);
            mGLSurfaceView.setEGLContextClientVersion(3);
            mGLSurfaceView.setRenderer(new GLRenderer(this, 0xff0000ff));
            //mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
            mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
            fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
            fr.addView(mGLSurfaceView, fl);
        }
    }
}
