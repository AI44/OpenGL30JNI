package com.ideacarry.example13;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.ideacarry.opengl30jni.R;
import com.ideacarry.opengl30jni.databinding.Example13Binding;

/**
 * 渲染yuv数据
 */
public class DemoActivity extends AppCompatActivity {

    private Example13Binding mBinding;
    private GLRenderer mRenderer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = Example13Binding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        mRenderer = new GLRenderer(this, 0xff0000ff);
        mBinding.surfaceView.setEGLContextClientVersion(3);
        mBinding.surfaceView.setRenderer(mRenderer);
        //mBinding.surfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        mBinding.surfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

    public void onFilterBtn(View view) {
        switch (view.getId()) {
            case R.id.filter1Btn:
                mRenderer.setMode(0);
                mBinding.surfaceView.requestRender();
                break;

            case R.id.filter2Btn:
                mRenderer.setMode(1);
                mBinding.surfaceView.requestRender();
                break;

            case R.id.filter3Btn:
                mRenderer.setMode(2);
                mBinding.surfaceView.requestRender();
                break;
        }
    }
}
