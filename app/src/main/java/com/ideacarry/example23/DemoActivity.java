package com.ideacarry.example23;

import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.grafika.gles.GlUtil;
import com.ideacarry.utils.CommonUtils;
import com.ideacarry.utils.GLShaderProgram;

import java.nio.IntBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Raining on 2020/12/14
 * #I# 模板测试
 */
public class DemoActivity extends AppCompatActivity {
    private final float[] vertices = {0.5f, 0.5f, //右上角
            0.5f, -0.5f, //右下角
            -0.5f, -0.5f, //左下角
            -0.5f, 0.5f, //左上角
            1.0f, 0.2f,
            1.0f, -0.2f,
            -1.0f, -0.2f,
            -1.0f, 0.2f,};

    private final short[] indices = {0, 1, 3, //第一个三角形
            1, 2, 3, //第二个三角形
            4, 5, 7,
            5, 6, 7,};

    private GLShaderProgram program;
    private int vbo;
    private int ebo;
    private int vao;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GLSurfaceView glSurfaceView = new GLSurfaceView(this);
        glSurfaceView.setEGLContextClientVersion(3);
        glSurfaceView.setRenderer(new GLSurfaceView.Renderer() {
            @Override
            public void onSurfaceCreated(GL10 gl, EGLConfig config) {
                program = new GLShaderProgram(new String(CommonUtils.readAssetFile(DemoActivity.this, "example23/vertex.glsl")),
                        new String(CommonUtils.readAssetFile(DemoActivity.this, "example23/fragment.glsl")));
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
                GLES30.glVertexAttribPointer(0, 2, GLES30.GL_FLOAT, false, 2 * 4, 0);
                GLES30.glEnableVertexAttribArray(0);

                GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, ebo);

                GLES30.glBindVertexArray(0);

                GLES30.glClearColor(1, 0.9f, 0, 1);
                GLES30.glEnable(GLES30.GL_STENCIL_TEST);
            }

            @Override
            public void onSurfaceChanged(GL10 gl, int width, int height) {
                GLES30.glViewport(0, 0, width, height);
            }

            @Override
            public void onDrawFrame(GL10 gl) {
                GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_STENCIL_BUFFER_BIT);

                program.use();
                GLES30.glBindVertexArray(vao);

                //设置模板值-------------------------------------------------------------------------
                GLES30.glColorMask(false, false, false, false);//禁用颜色显示
                GLES30.glStencilMask(0xFF); // 设置模板缓冲为可写状态

                GLES30.glStencilFunc(GLES30.GL_ALWAYS, 1, 0xFF);
                GLES30.glStencilOp(GLES30.GL_KEEP, GLES30.GL_KEEP, GLES30.GL_REPLACE);
                GLES30.glDrawElements(GLES30.GL_TRIANGLES, indices.length / 2, GLES30.GL_UNSIGNED_SHORT, 6 * 2);

                //设置重叠时挖空
                GLES30.glStencilFunc(GLES30.GL_NOTEQUAL, 1, 0xFF);
                GLES30.glStencilOp(GLES30.GL_ZERO, GLES30.GL_ZERO, GLES30.GL_REPLACE);
                GLES30.glDrawElements(GLES30.GL_TRIANGLES, indices.length / 2, GLES30.GL_UNSIGNED_SHORT, 0);

                //画图------------------------------------------------------------------------------
                GLES30.glColorMask(true, true, true, true);//启用颜色
                GLES30.glStencilMask(0x00); // 禁止修改模板缓冲
                GLES30.glStencilFunc(GLES30.GL_EQUAL, 1, 0xFF);
                GLES30.glStencilOp(GLES30.GL_KEEP, GLES30.GL_KEEP, GLES30.GL_REPLACE);

                //红色矩形
                program.setVec3("color", new float[]{1.0f, 0.0f, 0.0f});
                GLES30.glDrawElements(GLES30.GL_TRIANGLES, indices.length / 2, GLES30.GL_UNSIGNED_SHORT, 6 * 2);

                //青色矩形
                program.setVec3("color", new float[]{0.0f, 1.0f, 0.6f});
                GLES30.glDrawElements(GLES30.GL_TRIANGLES, indices.length / 2, GLES30.GL_UNSIGNED_SHORT, 0);
            }
        });
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        setContentView(glSurfaceView);
    }
}
