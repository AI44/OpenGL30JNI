package com.ideacarry.example20.videotape;

import android.content.Context;

import com.ideacarry.stable.filter.BaseFilter;
import com.ideacarry.utils.TextureManager;

public class VideotapeFilter extends BaseFilter {

    @Override
    public void onCreate(Context context) {

    }

    @Override
    public void onSizeChange(Context context, int viewWidth, int viewHeight, int cameraWidth, int cameraHeight, int degree, int flip) {

    }

    @Override
    public TextureManager.RendererData onDraw(TextureManager.RendererData data, int commonVao) {
        return null;
    }

    @Override
    public void onDestroy(Context context) {

    }
}
