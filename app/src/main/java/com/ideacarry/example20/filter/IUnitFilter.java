package com.ideacarry.example20.filter;

import android.content.Context;

/**
 * Created by Raining on 2020/9/2.
 * 最小单元滤镜
 */
public interface IUnitFilter {
    void onCreate(Context context);

    void onSizeChange(Context context, int viewWidth, int viewHeight, int cameraWidth, int cameraHeight, int degree, int flip);

    void onDraw(int width, int height, int commonVao, int... textures);

    void onDestroy(Context context);
}
