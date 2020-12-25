package com.ideacarry.example17;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.ideacarry.opengl30jni.R;
import com.ideacarry.opengl30jni.databinding.Example17Binding;

public class DemoActivity extends AppCompatActivity {
    Example17Binding mBinding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = Example17Binding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        mBinding.glSurfaceView.setEGLContextClientVersion(3);
        mBinding.glSurfaceView.setRenderer(new GLRenderer(this, 0xff0000ff));
        //mBinding.glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        mBinding.glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

        mBinding.compareBtn.setOnTouchListener((v, event) -> {
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
    }

    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.texture3dBtn) {
            GLRenderer.lut3D(0);
        } else if (id == R.id.texture2dArrBtn) {
            GLRenderer.lut3D(1);
        } else if (id == R.id.texture2dBtn) {
            GLRenderer.lut3D(2);
        }
    }
}
