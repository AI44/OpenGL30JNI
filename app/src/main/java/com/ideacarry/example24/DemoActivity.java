package com.ideacarry.example24;

import android.graphics.Bitmap;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.grafika.gles.GlUtil;
import com.ideacarry.utils.CommonUtils;
import com.ideacarry.utils.GLShaderProgram;
import com.ideacarry.utils.GLUtils;

import java.nio.IntBuffer;
import java.util.Arrays;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Raining on 2020/12/15
 * #I# texture3D线性插值测试对比
 */
public class DemoActivity extends AppCompatActivity {
    private static final float[] vertices = {
            -0.5f, 0.5f, -0.5f, 0f, 1f, 0f,//内左上    0
            0.5f, 0.5f, -0.5f, 1f, 1f, 0f,//内右上     1
            0.5f, -0.5f, -0.5f, 1f, 0f, 0f,//内右下    2
            -0.5f, -0.5f, -0.5f, 0f, 0f, 0f,//内左下   3
            -0.5f, 0.5f, 0.5f, 0f, 1f, 1f,//外左上     4
            0.5f, 0.5f, 0.5f, 1f, 1f, 1f,//外右上      5
            0.5f, -0.5f, 0.5f, 1f, 0f, 1f,//外右下     6
            -0.5f, -0.5f, 0.5f, 0f, 0f, 1f,//外左下    7
    };

    private static final short[] indices = {
            0, 1, 3, 1, 3, 2,//内矩形
            4, 5, 7, 5, 7, 6,//外矩形
            0, 4, 3, 4, 3, 7,//左矩形
            5, 1, 6, 1, 6, 2,//右矩形
            0, 1, 4, 1, 4, 5,//上矩形
            3, 2, 7, 2, 7, 6,//下矩形
    };

    private GLShaderProgram program3D;
    private GLShaderProgram program2DArr;
    private GLShaderProgram program2D;
    private int vbo;
    private int ebo;
    private int vao;
    private final float[] matrix = new float[16];
    private int cubeTextureId;
    private int arrTextureId;
    private int lutTextureId;

    private int mWidth;
    private int mHeight;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GLSurfaceView glSurfaceView = new GLSurfaceView(this);
        glSurfaceView.setEGLContextClientVersion(3);
        glSurfaceView.setRenderer(new GLSurfaceView.Renderer() {
            @Override
            public void onSurfaceCreated(GL10 gl, EGLConfig config) {
                GLES30.glClearColor(1, 0.9f, 0, 1);
                GLES30.glEnable(GLES30.GL_DEPTH_TEST);

                program3D = new GLShaderProgram(new String(CommonUtils.readAssetFile(DemoActivity.this, "example24/vertex.glsl")),
                        new String(CommonUtils.readAssetFile(DemoActivity.this, "example24/3d_cube_fragment.glsl")));

                program2DArr = new GLShaderProgram(new String(CommonUtils.readAssetFile(DemoActivity.this, "example24/vertex.glsl")),
                        new String(CommonUtils.readAssetFile(DemoActivity.this, "example24/2d_arr_fragment.glsl")));

                program2D = new GLShaderProgram(new String(CommonUtils.readAssetFile(DemoActivity.this, "example24/vertex.glsl")),
                        new String(CommonUtils.readAssetFile(DemoActivity.this, "example24/lut2x2_fragment.glsl")));

                IntBuffer buf = IntBuffer.allocate(1);
                GLES30.glGenBuffers(1, buf);
                vbo = buf.get();
                GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vbo);
                GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, vertices.length * 4, GlUtil.createFloatBuffer(vertices), GLES30.GL_STATIC_DRAW);

                buf.rewind();
                GLES30.glGenBuffers(1, buf);
                ebo = buf.get();
                GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, ebo);
                GLES30.glBufferData(GLES30.GL_ELEMENT_ARRAY_BUFFER, indices.length * 2, GlUtil.createShortBuffer(indices), GLES30.GL_STATIC_DRAW);

                buf.rewind();
                GLES30.glGenVertexArrays(1, buf);
                vao = buf.get();
                GLES30.glBindVertexArray(vao);

                GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vbo);
                GLES30.glVertexAttribPointer(0, 3, GLES30.GL_FLOAT, false, 6 * 4, 0);
                GLES30.glEnableVertexAttribArray(0);
                GLES30.glVertexAttribPointer(1, 3, GLES30.GL_FLOAT, false, 6 * 4, 3 * 4);
                GLES30.glEnableVertexAttribArray(1);

                GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, ebo);

                GLES30.glBindVertexArray(0);

                final int INDEX = 1;
                final int[][][] COLOR = new int[][][]{
                        {
                                {0xff000000, 0xff000000, 0xff000000, 0xff000000},
                                {0xff555555, 0xff555555, 0xff555555, 0xff555555},
                                {0xffababab, 0xffababab, 0xffababab, 0xffababab},
                                {0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff}
                        },
                        {
                                {0xffff0000, 0xffff0000, 0xffff0000, 0xffff0000},
                                {0xff00ff00, 0xff00ff00, 0xff00ff00, 0xff00ff00},
                                {0xff0000ff, 0xff0000ff, 0xff0000ff, 0xff0000ff},
                                {0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff}
                        },
                };

                Bitmap bmp1 = Bitmap.createBitmap(COLOR[INDEX][0], 2, 2, Bitmap.Config.ARGB_8888);
                Bitmap bmp2 = Bitmap.createBitmap(COLOR[INDEX][1], 2, 2, Bitmap.Config.ARGB_8888);
                Bitmap bmp3 = Bitmap.createBitmap(COLOR[INDEX][2], 2, 2, Bitmap.Config.ARGB_8888);
                Bitmap bmp4 = Bitmap.createBitmap(COLOR[INDEX][3], 2, 2, Bitmap.Config.ARGB_8888);
                cubeTextureId = GLUtils.createTexture3D(new Bitmap[]{bmp1, bmp2, bmp3, bmp4});

                arrTextureId = GLUtils.createTexture2DArr(new Bitmap[]{bmp1, bmp2, bmp3, bmp4});

                final int[] COLOR2 = new int[]{
                        COLOR[INDEX][0][0], COLOR[INDEX][0][1], COLOR[INDEX][1][0], COLOR[INDEX][1][1],
                        COLOR[INDEX][0][2], COLOR[INDEX][0][3], COLOR[INDEX][1][2], COLOR[INDEX][1][3],

                        COLOR[INDEX][2][0], COLOR[INDEX][2][1], COLOR[INDEX][3][0], COLOR[INDEX][3][1],
                        COLOR[INDEX][2][2], COLOR[INDEX][2][3], COLOR[INDEX][3][2], COLOR[INDEX][3][3],
                };
                Bitmap bmp = Bitmap.createBitmap(COLOR2, 4, 4, Bitmap.Config.ARGB_8888);
                lutTextureId = GLUtils.createTexture(bmp);

                Matrix.setIdentityM(matrix, 0);
                Matrix.rotateM(matrix, 0, 90, 0, 1, 0);

                program3D.use();
                program3D.setMat4("model", matrix);
                program3D.setInt("cube", 0);

                program2DArr.use();
                program2D.setMat4("model", matrix);
                program2D.setInt("arr", 2);

                program2D.use();
                program2D.setMat4("model", matrix);
                program2D.setInt("lut", 1);
            }

            @Override
            public void onSurfaceChanged(GL10 gl, int width, int height) {
                mWidth = width;
                mHeight = height;
            }

            @Override
            public void onDrawFrame(GL10 gl) {
                GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);

                //画3d
                final int size = mHeight / 3;
                GLES30.glViewport(0, size * 2, mWidth, size);
                program3D.use();
                GLES30.glBindVertexArray(vao);

                GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
                GLES30.glBindTexture(GLES30.GL_TEXTURE_3D, cubeTextureId);
                GLES30.glDrawElements(GLES30.GL_TRIANGLES, indices.length, GLES30.GL_UNSIGNED_SHORT, 0);

                //画arr
                GLES30.glViewport(0, size, mWidth, size);
                program2DArr.use();
                GLES30.glBindVertexArray(vao);

                GLES30.glActiveTexture(GLES30.GL_TEXTURE2);
                GLES30.glBindTexture(GLES30.GL_TEXTURE_2D_ARRAY, arrTextureId);
                GLES30.glDrawElements(GLES30.GL_TRIANGLES, indices.length, GLES30.GL_UNSIGNED_SHORT, 0);

                //画2d
                GLES30.glViewport(0, 0, mWidth, size);
                program2D.use();
                GLES30.glBindVertexArray(vao);

                GLES30.glActiveTexture(GLES30.GL_TEXTURE1);
                GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, lutTextureId);
                GLES30.glDrawElements(GLES30.GL_TRIANGLES, indices.length, GLES30.GL_UNSIGNED_SHORT, 0);
            }
        });
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        setContentView(glSurfaceView);
    }

    public static int[] concatAll(int[] first, int[]... rest) {
        int totalLength = first.length;
        for (int[] array : rest) {
            totalLength += array.length;
        }
        int[] result = Arrays.copyOf(first, totalLength);
        int offset = first.length;
        for (int[] array : rest) {
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }
        return result;
    }
}
