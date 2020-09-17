package com.ideacarry.example16;

import android.Manifest;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.util.Size;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.ideacarry.utils.PermissionUtils;
import com.ideacarry.utils.PreviewUtils;

public class DemoActivity extends AppCompatActivity {
    private Preview mPreview;
    private ImageCapture mImageCapture;
    private Camera mCamera;
    private TextureView mView;
    private GLThread mGlThread;
    private SurfaceTexture mSurfaceTexture;
    private Surface mSurface;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mGlThread = new GLThread(getApplicationContext());
        mGlThread.start();

        FrameLayout fr = new FrameLayout(this);
        setContentView(fr);
        {
            FrameLayout.LayoutParams lp;

            mView = new TextureView(this);
            lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT);
            fr.addView(mView, lp);
            mView.setSurfaceTextureListener(mGlThread);

            Button btn = new Button(this);
            btn.setText("对比");
            lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT);
            lp.gravity = Gravity.RIGHT | Gravity.BOTTOM;
            fr.addView(btn, lp);
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
        }

        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }
    }

    @Override
    protected void onDestroy() {
        mGlThread.release();
        super.onDestroy();
    }

    private void startCamera() {
        final ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                // Preview
                mPreview = new Preview.Builder().build();
                //拍照用
                mImageCapture = new ImageCapture.Builder().build();
                // Select camera
                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_FRONT).build();
                // Unbind use cases before rebinding
                cameraProvider.unbindAll();
                if (mPreview != null) {
                    // Bind use cases to camera
                    mCamera = cameraProvider.bindToLifecycle(DemoActivity.this,
                            cameraSelector, mPreview, mImageCapture);
                    mGlThread.setCameraRotate(mCamera.getCameraInfo().getSensorRotationDegrees());
                    mPreview.setSurfaceProvider(mGlThread, request -> {
                        if (mSurface == null) {
                            final Size size = request.getResolution();
                            final Size previewSize = PreviewUtils.getPreviewSize(mView, size);
                            mGlThread.setCameraSize(previewSize.getWidth(), previewSize.getHeight());
                            mSurfaceTexture = new SurfaceTexture(mGlThread.getTextureId());
                            mSurfaceTexture.setDefaultBufferSize(size.getWidth(), size.getHeight());
                            mSurfaceTexture.setOnFrameAvailableListener(mGlThread, mGlThread.getHandler());
                            mSurface = new Surface(mSurfaceTexture);
                        }
                        request.provideSurface(mSurface, mGlThread, result -> {
                            //System.out.println("provideSurface result " + Thread.currentThread().getId());
                        });
                    });
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera();
            } else {
                Toast.makeText(getApplicationContext(), "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private static final int REQUEST_CODE_PERMISSIONS = 123;
    private static final String[] REQUIRED_PERMISSIONS = new String[]{
            Manifest.permission.CAMERA
    };

    private boolean allPermissionsGranted() {
        return PermissionUtils.checkPermissions(this, REQUIRED_PERMISSIONS);
    }
}
