package com.ideacarry.example21;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.EGL14;
import android.os.Handler;
import android.os.Looper;

import com.android.grafika.gles.EglCore;
import com.android.grafika.gles.OffscreenSurface;
import com.ideacarry.utils.GLUtils;

public abstract class BaseFilter {
    protected final Context mContext;

    public BaseFilter(Context context, final Bitmap bmp, IResult<Bitmap> result) {
        mContext = context;

        new Thread(() -> {
            //创建gl环境
            EglCore eglCore = new EglCore(EGL14.EGL_NO_CONTEXT, EglCore.FLAG_TRY_GLES3);
            OffscreenSurface offscreenSurface = new OffscreenSurface(eglCore, bmp.getWidth(), bmp.getHeight());
            offscreenSurface.makeCurrent();

            int textureId = GLUtils.createTexture(bmp);
            doFilter(bmp.getWidth(), bmp.getHeight(), textureId);
            GLUtils.deleteTexture(textureId);

            final Bitmap dst = offscreenSurface.saveFrame();
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(() -> result.onResult(dst));

            offscreenSurface.release();
            eglCore.release();
        }).start();
    }

    public abstract void doFilter(int w, int h, int textureId);
}
