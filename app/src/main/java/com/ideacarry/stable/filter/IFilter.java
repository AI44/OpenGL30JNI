package com.ideacarry.stable.filter;

import android.content.Context;

import com.ideacarry.utils.TextureManager;

/**
 * Created by Raining on 2020/9/2.
 * 一个完整滤镜
 */
public interface IFilter {
    void onCreate(Context context);

    void onSizeChange(Context context, int viewWidth, int viewHeight, int cameraWidth, int cameraHeight, int degree, int flip);

    TextureManager.RendererData onDraw(TextureManager.RendererData data, int commonVao);

    void onDestroy(Context context);
}
