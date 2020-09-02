package com.ideacarry.example20.filter;

import android.content.Context;
import android.opengl.GLES11Ext;
import android.opengl.GLES30;
import android.opengl.Matrix;

import com.android.grafika.gles.GlUtil;
import com.ideacarry.utils.CommonUtils;

public class CameraUnitFilter implements IUnitFilter {

    private int mProgram = -1;
    private float[] mMatrix = new float[16];

    @Override
    public void onCreate(Context context) {
        mProgram = GlUtil.createProgram(new String(CommonUtils.readAssetFile(context, "example20/camera/camera_vertex.glsl")),
                new String(CommonUtils.readAssetFile(context, "example20/camera/camera_fragment.glsl")));
    }

    @Override
    public void onSizeChange(Context context, int viewWidth, int viewHeight, int cameraWidth, int cameraHeight, int degree, int flip) {
        Matrix.setIdentityM(mMatrix, 0);
        if (viewWidth > 0 && viewHeight > 0 && cameraWidth > 0 && cameraHeight > 0) {
            //维持camera比例，填满view区域
            float normalScale = (float) cameraWidth / cameraHeight;
            float scale = (float) viewWidth / viewHeight;
            if (normalScale > scale) {
                //System.out.println("x = " + normalScale / scale);
                Matrix.scaleM(mMatrix, 0, normalScale / scale, 1, 1);
            } else {
                //System.out.println("y = " + scale / normalScale);
                Matrix.scaleM(mMatrix, 0, 1, scale / normalScale, 1);
            }
        }
        Matrix.rotateM(mMatrix, 0, degree, 0, 0, 1);

        GLES30.glUseProgram(mProgram);
        GLES30.glUniformMatrix4fv(2, 1, false, mMatrix, 0);
    }

    @Override
    public void onDraw(int width, int height, int commonVao, int... textures) {
        GLES30.glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT);

        GLES30.glUseProgram(mProgram);
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textures[0]);

        GLES30.glBindVertexArray(commonVao);
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4);

        //clear
        GLES30.glBindVertexArray(0);
        GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
        GLES30.glUseProgram(0);
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);
    }

    @Override
    public void onDestroy(Context context) {
        if (mProgram > -1) {
            GLES30.glDeleteProgram(mProgram);
            mProgram = -1;
        }
    }
}
