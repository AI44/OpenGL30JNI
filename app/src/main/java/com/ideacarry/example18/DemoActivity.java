package com.ideacarry.example18;

import android.graphics.Bitmap;
import android.opengl.EGL14;
import android.opengl.GLES30;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.grafika.gles.EglCore;
import com.android.grafika.gles.GlUtil;
import com.android.grafika.gles.OffscreenSurface;
import com.ideacarry.example1.GL2Renderer;
import com.ideacarry.example12.GL3Renderer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

/**
 * #I# 离屏渲染
 */
public class DemoActivity extends AppCompatActivity {
    private ImageView imageView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*GLSurfaceView mGLSurfaceView = new GLSurfaceView(this);
        mGLSurfaceView.setEGLContextClientVersion(3);
        mGLSurfaceView.setRenderer(new GLSurfaceView.Renderer() {
            @Override
            public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            }

            @Override
            public void onSurfaceChanged(GL10 gl, int width, int height) {
            }

            @Override
            public void onDrawFrame(GL10 gl) {
                draw();
            }
        });
        mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        setContentView(mGLSurfaceView);*/

        imageView = new ImageView(this);
        setContentView(imageView);
        new Thread(() -> {
            //创建gl环境
            EglCore eglCore = new EglCore(EGL14.EGL_NO_CONTEXT, EglCore.FLAG_TRY_GLES3);
            OffscreenSurface offscreenSurface = new OffscreenSurface(eglCore, W, H);
            offscreenSurface.makeCurrent();

            draw();
            GLES30.glFinish();

            //离屏渲染到bitmap
            ByteBuffer buf = ByteBuffer.allocateDirect(W * H * 4);
            buf.order(ByteOrder.LITTLE_ENDIAN);
            GLES30.glReadPixels(0, 0, W, H, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, buf);
            GlUtil.checkGlError("glReadPixels");
            buf.rewind();
            final Bitmap bmp = Bitmap.createBitmap(W, H, Bitmap.Config.ARGB_8888);
            bmp.copyPixelsFromBuffer(buf);

            //显示到UI
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(() -> {
                imageView.setImageBitmap(bmp);
            });
        }).start();
    }

    public static final int W = 500;
    public static final int H = 500;

    private static final String VERTEX_SHADER_CODE =
            "#version 300 es\n" +
                    "layout(location = 0) in vec3 aPos;" +
                    "out vec4 color;" +
                    "void main(){" +
                    "gl_Position = vec4(aPos.x, aPos.y, aPos.z, 1.0);" +
                    "color = vec4(0.5, 0.0, 0.0, 1.0);" +
                    "}";

    private static final String FRAGMENT_SHADER_CODE =
            "#version 300 es\n" +
                    "precision mediump float;" +
                    "out vec4 fragColor;" +
                    "in vec4 color;" +
                    "void main(){" +
                    "fragColor = color;" +
                    "}";
    private static final float[] VERTICES = {
            -0.5f, -0.5f, 0.0f,
            0.5f, -0.5f, 0.0f,
            0.0f, 0.5f, 0.0f};

    private static final String SCREEN_VERTEX_SHADER_CODE =
            "#version 300 es\n" +
                    "layout (location = 0) in vec2 aPos;" +
                    "layout (location = 1) in vec2 aTexCoords;" +
                    "out vec2 TexCoords;" +
                    "void main(){" +
                    "TexCoords = aTexCoords;" +
                    "gl_Position = vec4(aPos.xy, 0.0, 1.0);" +
                    "}";
    private static final String SCREEN_FRAGMENT_SHADER_CODE =
            "#version 300 es\n" +
                    "precision mediump float;" +
                    "out vec4 FragColor;" +
                    "in vec2 TexCoords;" +
                    "uniform sampler2D screenTexture;" +
                    "void main(){" +
                    "vec3 col = texture(screenTexture, TexCoords).rgb;" +
                    "FragColor = vec4(col, 1.0f);" +
                    "}";
    private static final float[] QUAD_VERTICES = {
            // positions // texCoords
            -1.0f, 1.0f, 0.0f, 1.0f,
            -1.0f, -1.0f, 0.0f, 0.0f,
            1.0f, 1.0f, 1.0f, 1.0f,
            1.0f, -1.0f, 1.0f, 0.0f
    };

    public static void draw() {
        //加载program
        int program = GL3Renderer.createProgram(VERTEX_SHADER_CODE, FRAGMENT_SHADER_CODE);
        int screenProgram = GL3Renderer.createProgram(SCREEN_VERTEX_SHADER_CODE, SCREEN_FRAGMENT_SHADER_CODE);
        if (program != 0 && screenProgram != 0) {
            //加载顶点数据
            IntBuffer framebufferVao = IntBuffer.allocate(1);
            IntBuffer framebufferVbo = IntBuffer.allocate(1);
            GLES30.glGenVertexArrays(1, framebufferVao);
            GLES30.glGenBuffers(1, framebufferVbo);
            GLES30.glBindVertexArray(framebufferVao.get(0));
            GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, framebufferVbo.get(0));
            GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, VERTICES.length * 4, GL2Renderer.getFloatBuffer(VERTICES), GLES30.GL_STATIC_DRAW);
            GLES30.glEnableVertexAttribArray(0);
            GLES30.glVertexAttribPointer(0, 3, GLES30.GL_FLOAT, false, 3 * 4, 0);
            GLES30.glBindVertexArray(0);
            //screen顶点数据
            IntBuffer screenVao = IntBuffer.allocate(1);
            IntBuffer screenVbo = IntBuffer.allocate(1);
            GLES30.glGenVertexArrays(1, screenVao);
            GLES30.glGenBuffers(1, screenVbo);
            GLES30.glBindVertexArray(screenVao.get(0));
            GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, screenVbo.get(0));
            GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, QUAD_VERTICES.length * 4, GL2Renderer.getFloatBuffer(QUAD_VERTICES), GLES30.GL_STATIC_DRAW);
            GLES30.glEnableVertexAttribArray(0);
            GLES30.glVertexAttribPointer(0, 2, GLES30.GL_FLOAT, false, 4 * 4, 0);
            GLES30.glEnableVertexAttribArray(1);
            GLES30.glVertexAttribPointer(1, 2, GLES30.GL_FLOAT, false, 4 * 4, 2 * 4);
            GLES30.glBindVertexArray(0);

            //framebuffer
            IntBuffer framebuffer = IntBuffer.allocate(1);
            GLES30.glGenFramebuffers(1, framebuffer);
            GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, framebuffer.get(0));
            IntBuffer texture = IntBuffer.allocate(1);
            texture.put(0, GlUtil.createImageTexture(null, W, H, GLES30.GL_RGBA));
            GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0, GLES30.GL_TEXTURE_2D, texture.get(0), 0);
            GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);

            GLES30.glViewport(0, 0, W, H);

            //画到framebuffer里
            GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, framebuffer.get(0));
            GLES30.glClearColor(1.0f, 1.0f, 0, 1.0f);
            GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT);
            GLES30.glUseProgram(program);
            GLES30.glBindVertexArray(framebufferVao.get(0));
            GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 3);
            //framebuffer画到屏幕
            GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);
            GLES30.glClearColor(1.0f, 0.0f, 1.0f, 1.0f);
            GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT);
            GLES30.glUseProgram(screenProgram);
            GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, texture.get(0));
            GLES30.glBindVertexArray(screenVao.get(0));
            GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4);

            //清理
            GLES30.glDeleteFramebuffers(1, framebuffer);
            GLES30.glDeleteTextures(1, texture);
        }
    }
}
