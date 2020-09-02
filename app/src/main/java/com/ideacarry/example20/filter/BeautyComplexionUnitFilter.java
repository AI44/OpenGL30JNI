package com.ideacarry.example20.filter;

import android.content.Context;
import android.opengl.GLES30;

import com.android.grafika.gles.GlUtil;
import com.ideacarry.utils.CommonUtils;
import com.ideacarry.utils.GLUtils;

/**
 * @see <a href="https://github.com/CainKernel/CainCamera">CainCamera</a>
 */
public class BeautyComplexionUnitFilter implements IUnitFilter {

    private int mProgram = -1;

    private int mGrayTexture;
    private int mLookupTexture;

    private float levelRangeInv = 1.040816f;
    private float levelBlack = 0.01960784f;
    private float alpha = 0.6f;

    @Override
    public void onCreate(Context context) {
        mProgram = GlUtil.createProgram(new String(CommonUtils.readAssetFile(context, "example20/filter/general_vertex.glsl")),
                new String(CommonUtils.readAssetFile(context, "example20/filter/fragment_beauty_complexion.glsl")));
        mGrayTexture = GLUtils.createTextureFromAssets(context, "example20/filter/skin_gray.png");
        mLookupTexture = GLUtils.createTextureFromAssets(context, "example20/filter/skin_lookup.png");

        GLES30.glUseProgram(mProgram);
        GLES30.glUniform1f(6, levelRangeInv);
        GLES30.glUniform1f(7, levelBlack);
        GLES30.glUniform1f(8, alpha);
        GLES30.glUniform1i(3, 3);
        GLES30.glUniform1i(4, 4);
        GLES30.glUniform1i(5, 5);
    }

    @Override
    public void onSizeChange(Context context, int viewWidth, int viewHeight, int cameraWidth, int cameraHeight, int degree, int flip) {
    }

    /**
     * @param textures [0]:inputTexture, [1]:grayTexture, [2]:lookupTexture
     */
    @Override
    public void onDraw(int width, int height, int commonVao, int... textures) {
        GLES30.glClearColor(0.1f, 1.0f, 0.1f, 1.0f);
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT);

        GLES30.glUseProgram(mProgram);
        GLES30.glActiveTexture(GLES30.GL_TEXTURE3);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textures[0]);
        GLES30.glActiveTexture(GLES30.GL_TEXTURE4);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mGrayTexture);
        GLES30.glActiveTexture(GLES30.GL_TEXTURE5);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mLookupTexture);
        GLES30.glBindVertexArray(commonVao);
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4);

        //clear
        GLES30.glBindVertexArray(0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0);
        GLES30.glUseProgram(0);
    }

    @Override
    public void onDestroy(Context context) {
        if (mProgram > -1) {
            GLES30.glDeleteProgram(mProgram);
            mProgram = -1;
        }
        if (mGrayTexture > -1) {
            GLES30.glDeleteTextures(1, new int[]{mGrayTexture}, 0);
            mGrayTexture = -1;
        }
        if (mLookupTexture > -1) {
            GLES30.glDeleteTextures(1, new int[]{mLookupTexture}, 0);
            mLookupTexture = -1;
        }
    }
}
