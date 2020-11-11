package com.ideacarry.example17;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.widget.Button;
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

            GLSurfaceView glSurfaceView = new GLSurfaceView(this);
            glSurfaceView.setEGLContextClientVersion(3);
            glSurfaceView.setRenderer(new GLRenderer(this, 0xff0000ff));
            //glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
            glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
            fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
            fr.addView(glSurfaceView, fl);

            Button btn = new Button(this);
            btn.setText("compare");
            fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
            fl.gravity = Gravity.RIGHT | Gravity.BOTTOM;
            fr.addView(btn, fl);
            btn.setOnTouchListener((v, event) -> {
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        GLRenderer.compare(true);
                        break;

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        GLRenderer.compare(false);
                        break;
                    default:
                        break;
                }
                return true;
            });

            btn = new Button(this);
            btn.setText("texture3D");
            fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
            fl.gravity = Gravity.LEFT | Gravity.BOTTOM;
            fr.addView(btn, fl);
            btn.setOnClickListener(view -> GLRenderer.lut3D(true));

            btn = new Button(this);
            btn.setText("arithmetic");
            fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
            fl.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
            fr.addView(btn, fl);
            btn.setOnClickListener(view -> GLRenderer.lut3D(false));
        }
    }
}
