package com.ideacarry.example20;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.EGL14;
import android.opengl.GLES30;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.TextureView;

import androidx.annotation.NonNull;

import com.android.grafika.gles.EglCore;
import com.android.grafika.gles.WindowSurface;
import com.ideacarry.stable.filter.BaseFilter;
import com.ideacarry.example20.filter.BeautyFilter;
import com.ideacarry.example20.filter.CameraFilter;
import com.ideacarry.example20.filter.ScreenFilter;
import com.ideacarry.utils.GLUtils;
import com.ideacarry.utils.TextureManager;

import java.util.ArrayList;
import java.util.concurrent.Executor;

/**
 * Created by Raining on 2020/9/2.
 */
public class GLThread extends HandlerThread implements Executor, TextureView.SurfaceTextureListener, SurfaceTexture.OnFrameAvailableListener {

    private Context mContext;
    public static final int OES_TEXTURE_ID = 11;
    private EglCore mEglCore;
    private WindowSurface mWindowSurface;
    private Handler mHandler;
    private volatile boolean mIsDestroy;
    private boolean mCompare;

    private int mCameraWidth = -1;
    private int mCameraHeight = -1;
    private int mViewWidth = -1;
    private int mViewHeight = -1;
    private int mCameraDegree = 0;
    private int mVao = -1;
    private int mVbo = -1;

    private volatile ArrayList<BaseFilter> mFilterList = new ArrayList<>();
    private volatile TextureManager mTextureManager;
    private TextureManager.RendererData mCameraRendererData;
    private volatile CameraFilter mCameraFilter;
    private volatile ScreenFilter mScreenFilter;

    public GLThread(Context context) {
        super("glThread");
        mContext = context.getApplicationContext();

        //mFilterList添加滤镜-------------------------------------------------------------------start
        mCameraFilter = new CameraFilter();
        mScreenFilter = new ScreenFilter();
        mFilterList.add(new BeautyFilter());
        mTextureManager = new TextureManager();
        //mFilterList添加滤镜---------------------------------------------------------------------end
    }

    public Handler getHandler() {
        if (mHandler == null) {
            mHandler = new Handler(getLooper());
        }
        return mHandler;
    }

    @Override
    public synchronized void start() {
        super.start();

        getHandler().post(() -> {
            mEglCore = new EglCore(EGL14.EGL_NO_CONTEXT, EglCore.FLAG_TRY_GLES3);
        });
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        if (mViewWidth > -1 && mCameraHeight > -1) {
            surfaceTexture.updateTexImage();

            //onDraw---------------------------------------------------------------------------start
            TextureManager.RendererData data;

            //camera
            data = mCameraFilter.onDraw(mCameraRendererData, mVao);

            //filter
            if (!mCompare && data != null) {
                for (BaseFilter filter : mFilterList) {
                    data = filter.onDraw(data, mVao);
                }
            }

            //screen
            if (data != null) {
                mScreenFilter.onDraw(data, mVao);
                //clear
                mTextureManager.release(data);
            }
            //onDraw-----------------------------------------------------------------------------end

            if (!mIsDestroy && mWindowSurface != null) {
                mWindowSurface.swapBuffers();
            }
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        mIsDestroy = false;
        System.out.println("surfaceCreated " + Thread.currentThread().getId());
        mHandler.post(() -> {
            mViewWidth = width;
            mViewHeight = height;

            mWindowSurface = new WindowSurface(mEglCore, surface);
            mWindowSurface.makeCurrent();

            //onCreate-------------------------------------------------------------------------start
            int[] params = GLUtils.createQuadVertexArrays(0, 1);
            mVao = params[0];
            mVbo = params[1];

            mCameraFilter.setTextureManager(mTextureManager);
            mCameraFilter.onCreate(mContext);

            mScreenFilter.setTextureManager(mTextureManager);
            mScreenFilter.onCreate(mContext);

            for (BaseFilter filter : mFilterList) {
                filter.setTextureManager(mTextureManager);
                filter.onCreate(mContext);
            }
            //onCreate---------------------------------------------------------------------------end

            onSizeChange();
        });
    }

    private void onSizeChange() {
        if (mViewWidth > 0 && mViewHeight > 0 && mCameraWidth > 0 && mCameraHeight > 0) {
            //onSizeChange---------------------------------------------------------------------start
            mCameraFilter.onSizeChange(mContext, mViewWidth, mViewHeight, mCameraWidth, mCameraHeight, mCameraDegree, 0);
            if (mCameraRendererData == null) {
                mCameraRendererData = new TextureManager.RendererData();
            }
            mCameraRendererData.width = mCameraWidth;
            mCameraRendererData.height = mCameraHeight;
            mCameraRendererData.texture = OES_TEXTURE_ID;

            mScreenFilter.onSizeChange(mContext, mViewWidth, mViewHeight, mCameraWidth, mCameraHeight, mCameraDegree, 0);

            for (BaseFilter filter : mFilterList) {
                filter.onSizeChange(mContext, mViewWidth, mViewHeight, mCameraWidth, mCameraHeight, mCameraDegree, 0);
            }
            //onSizeChange-----------------------------------------------------------------------end
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        if (!mIsDestroy) {
            mHandler.post(() -> {
                //System.out.println("onSurfaceTextureSizeChanged " + Thread.currentThread().getId());
                mViewWidth = width;
                mViewHeight = height;
                onSizeChange();
            });
        }

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        if (!mIsDestroy) {
            mHandler.post(() -> {
                //System.out.println("onSurfaceTextureDestroyed " + Thread.currentThread().getId());
                //onDestroy--------------------------------------------------------------------start
                for (BaseFilter filter : mFilterList) {
                    filter.onDestroy(mContext);
                }
                mCameraFilter.onDestroy(mContext);
                mScreenFilter.onDestroy(mContext);
                if (mVao > -1) {
                    GLES30.glDeleteVertexArrays(1, new int[]{mVao}, 0);
                    mVao = -1;
                }
                if (mVbo > -1) {
                    GLES30.glDeleteBuffers(1, new int[]{mVbo}, 0);
                    mVbo = -1;
                }
                //onDestroy----------------------------------------------------------------------end
            });
        }
        mIsDestroy = true;
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }

    //Executor --------------------------------------------------------------------------------start
    @Override
    public void execute(@NonNull Runnable command) {
        if (!mIsDestroy) {
            getHandler().post(command);
        }
    }
    //Executor ----------------------------------------------------------------------------------end

    public void compare(boolean compare) {
        if (!mIsDestroy) {
            mHandler.post(() -> mCompare = compare);
        }
    }

    public void setCameraDegree(final int degree) {
        getHandler().post(() -> {
            mCameraDegree = degree;
            onSizeChange();
        });
    }

    public void setCameraSize(int w, int h) {
        mCameraWidth = w;
        mCameraHeight = h;

        onSizeChange();
    }
}
