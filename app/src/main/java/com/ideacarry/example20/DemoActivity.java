package com.ideacarry.example20;

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
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.ideacarry.utils.CommonUtils;
import com.ideacarry.utils.OrientationProvider;
import com.ideacarry.utils.PermissionUtils;
import com.ideacarry.utils.PreviewUtils;

/**
 * Created by Raining on 2020/9/1
 * #I# 镜头效果测试
 */
public class DemoActivity extends AppCompatActivity {
    private Preview mPreview;
    private ImageCapture mImageCapture;
    private Camera mCamera;
    private TextureView mView;
    private volatile GLThread mGlThread;
    private SurfaceTexture mSurfaceTexture;
    private Surface mSurface;

    private Button mRatioBtn;

    private AnalyzerThread mAnalyzerThread;
    private ImageAnalysis mImageAnalysis;
    private OrientationProvider mOrientationProvider;
    private volatile int mPhoneDegree;
    private volatile int mCameraDegree;

    private enum RatioType {
        full,
        _1_1,
        _3_4,
        _9_16,
    }

    private RatioType[] mRatioArr = RatioType.values();
    private int mRatioIndex = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mGlThread = new GLThread(getApplicationContext());
        mGlThread.start();

        mAnalyzerThread = new AnalyzerThread();
        mAnalyzerThread.start();

        mOrientationProvider = new OrientationProvider(this);
        mOrientationProvider.enable();

        mOrientationProvider.setDegreeListener(degree -> {
            mPhoneDegree = degree;
            //System.out.println("onDegreeChange : " + mPhoneDegree);
            if (mAnalyzerThread != null) {
                mAnalyzerThread.setDegree(mPhoneDegree, mCameraDegree);
            }
        });

        final FrameLayout fr = new FrameLayout(this);
        setContentView(fr);
        {
            FrameLayout.LayoutParams lp;

            mView = new TextureView(this);
            lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT);
            fr.addView(mView, lp);
            mView.setSurfaceTextureListener(mGlThread);

            mRatioBtn = new Button(this);
            mRatioBtn.setText(RatioType.full.toString());
            lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT);
            lp.gravity = Gravity.CENTER_HORIZONTAL | Gravity.TOP;
            fr.addView(mRatioBtn, lp);
            mRatioBtn.setOnClickListener(view -> {
                RatioType ratio = mRatioArr[(++mRatioIndex) % mRatioArr.length];
                mRatioBtn.setText(ratio.toString().replaceFirst("_", "").replace("_", " : "));
                switch (ratio) {
                    case full: {
                        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                                FrameLayout.LayoutParams.MATCH_PARENT,
                                FrameLayout.LayoutParams.MATCH_PARENT);
                        mView.setLayoutParams(layoutParams);
                        break;
                    }
                    case _1_1: {
                        int w = (fr.getWidth() >> 1) << 1;
                        int h = w;
                        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                                w,
                                h);
                        layoutParams.gravity = Gravity.CENTER;
                        mView.setLayoutParams(layoutParams);
                        break;
                    }
                    case _3_4: {
                        int w = (fr.getWidth() >> 1) << 1;
                        int h = ((Math.round(w / 3.0f * 4)) >> 1) << 1;
                        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                                w,
                                h);
                        layoutParams.gravity = Gravity.CENTER;
                        mView.setLayoutParams(layoutParams);
                        break;
                    }
                    case _9_16: {
                        int w = (fr.getWidth() >> 1) << 1;
                        int h = ((Math.round(w / 9.0f * 16)) >> 1) << 1;
                        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                                w,
                                h);
                        layoutParams.gravity = Gravity.CENTER;
                        mView.setLayoutParams(layoutParams);
                        break;
                    }
                }
            });

            Button btn = new Button(this);
            btn.setText("对比");
            lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT);
            lp.gravity = Gravity.RIGHT | Gravity.BOTTOM;
            fr.addView(btn, lp);
            btn.setOnTouchListener((v, event) -> {
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        mGlThread.compare(true);
                        break;

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        mGlThread.compare(false);
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

    private void startCamera() {
        final ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                // Preview
                mPreview = new Preview.Builder().build();
                //拍照用
                mImageCapture = new ImageCapture.Builder().build();
                //人脸检测用
                mImageAnalysis = new ImageAnalysis.Builder()
                        //如果此方法花费的时间超过单帧在当前帧速率下的延迟时间，可能会跳过某些帧，
                        //以便在下一次 analyze 接收数据时，它会获取相机流水线中的最后一个可用帧
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();
                if (mAnalyzerThread != null) {
                    mImageAnalysis.setAnalyzer(mAnalyzerThread, mAnalyzerThread);
                }
                // Select camera
                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_FRONT).build();
                // Unbind use cases before rebinding
                cameraProvider.unbindAll();
                if (mPreview != null && mGlThread != null) {
                    // Bind use cases to camera
                    mCamera = cameraProvider.bindToLifecycle(this,
                            cameraSelector, mPreview, mImageCapture, mImageAnalysis);
                    mCameraDegree = mCamera.getCameraInfo().getSensorRotationDegrees();
                    mGlThread.setCameraDegree(mCameraDegree);
                    mPreview.setSurfaceProvider(mGlThread, request -> {
                        if (mSurface == null) {
                            final Size size = request.getResolution();
                            final Size previewSize = PreviewUtils.getPreviewSize(mView, size);
                            mGlThread.setCameraSize(previewSize.getWidth(), previewSize.getHeight());
                            mSurfaceTexture = new SurfaceTexture(GLThread.OES_TEXTURE_ID);
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

    private static final int REQUEST_CODE_PERMISSIONS = 2020;
    private static final String[] REQUIRED_PERMISSIONS = new String[]{
            Manifest.permission.CAMERA
    };

    private boolean allPermissionsGranted() {
        return PermissionUtils.checkPermissions(this, REQUIRED_PERMISSIONS);
    }

    @Override
    protected void onStart() {
        CommonUtils.activityFullScreen(this);

        super.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAnalyzerThread.quitSafely();
        mAnalyzerThread = null;
        mGlThread.quitSafely();
        mGlThread = null;
    }
}
