package com.ideacarry.example14;

import android.Manifest;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Size;
import android.view.Surface;
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

/**
 * #I# CameraX+SurfaceTexture+SurfaceView
 */
public class DemoActivity extends AppCompatActivity {

    private Preview mPreview;
    private ImageCapture mImageCapture;
    private GLThread mGlThread;

    private FrameLayout mFr;
    private Camera mCamera;
    private AutoFitSurfaceView mSurfaceView;
    private int mTextureId;
    private SurfaceTexture mSurfaceTexture;
    private final float[] mMatrix = new float[16];
    private Surface mSurface;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mFr = new FrameLayout(this);
        setContentView(mFr);

        mGlThread = new GLThread(getApplicationContext());
        mGlThread.start();

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
        System.out.println("startCamera " + Thread.currentThread().getId());
        final ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                // Preview
                mPreview = new Preview.Builder()
                        //.setTargetResolution(new Size(1080, 1920))
                        .build();

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
                    mPreview.setSurfaceProvider(mGlThread, request -> {
                        //if (isShuttingDown()) {
                        //    request.willNotProvideSurface();
                        //    return;
                        //}

                        if (mSurface == null) {
                            final Size size = request.getResolution();
                            System.out.println("size = " + size.getWidth() + " - " + size.getHeight());
                            new Handler(Looper.getMainLooper()).post(() -> {
                                mFr.removeAllViews();
                                mSurfaceView = new AutoFitSurfaceView(DemoActivity.this);
                                mSurfaceView.setSize(mFr, size.getWidth(), size.getHeight());
                                //mSurfaceView.setSize(mFr, size.getHeight(), size.getWidth());
                                mFr.addView(mSurfaceView);
                                mSurfaceView.getHolder().addCallback(mGlThread);
                            });

                            GLRenderer.deleteTextureObject(mTextureId);
                            mTextureId = GLRenderer.createTextureObject();
                            mSurfaceTexture = new SurfaceTexture(mTextureId);
                            mSurfaceTexture.setDefaultBufferSize(size.getWidth(), size.getHeight());
                            //mSurfaceTexture.setDefaultBufferSize(size.getHeight(), size.getWidth());
                            mSurfaceTexture.setOnFrameAvailableListener(surfaceTexture -> {
                                //System.out.println("OnFrameAvailableListener " + Thread.currentThread().getId());
                                surfaceTexture.updateTexImage();
                                surfaceTexture.getTransformMatrix(mMatrix);
                                GLRenderer.drawFrame(mTextureId, mMatrix);

                                // 交换显存(将surface显存和显示器的显存交换)
                                mGlThread.swapBuffers();
                            }, mGlThread.getHandler());
                            mSurface = new Surface(mSurfaceTexture);
                        }
                        request.provideSurface(mSurface, mGlThread, result -> {
                            //closeGlInputSurface(surface)
                            System.out.println("provideSurface result " + Thread.currentThread().getId());
                        });
                        //Surface surface = resetGlInputSurface(request.getResolution());
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
