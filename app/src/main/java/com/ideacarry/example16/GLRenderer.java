package com.ideacarry.example16;

import android.content.Context;

public class GLRenderer {
    public static native void surfaceCreated(Context context);

    public static native void surfaceChanged(int width, int height, int cameraWidth, int cameraHeight, int degree);

    public static native void drawFrame(int textureId, float[] matrix);

    public static native void compare(boolean press);
}
