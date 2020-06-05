package com.ideacarry.example14;

import android.content.Context;

public class GLRenderer {

    public static native void deleteTextureObject(int textureId);

    public static native int createTextureObject();

    public static native void surfaceCreated(Context context);

    public static native void surfaceChanged(int width, int height);

    public static native void drawFrame(int textureId, float[] matrix);
}