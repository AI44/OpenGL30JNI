package com.ideacarry.example12;

import android.opengl.EGL14;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.util.Log;

import com.ideacarry.utils.GLUtils;

import java.nio.IntBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GL3Renderer implements GLSurfaceView.Renderer {

    private int program;

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES30.glClearColor(0, 0, 1.0f, 1.0f);
        //GLES30.glEnable(GLES30.GL_DEPTH_TEST);

        program = createProgram(vertexShaderCode, fragmentShaderCode);
        if (program != 0) {
            //准备三角形的坐标数据
            GLES30.glVertexAttribPointer(0, 3, GLES30.GL_FLOAT, false, 0, GLUtils.getFloatBuffer(vertices));
            //启用三角形顶点的句柄
            GLES30.glEnableVertexAttribArray(0);
            //禁止顶点数组的句柄
            //glDisableVertexAttribArray(0);
        }

        IntBuffer maxVertexAttribs = IntBuffer.allocate(1);
        GLES30.glGetIntegerv(GLES30.GL_MAX_VERTEX_ATTRIBS, maxVertexAttribs);
        System.out.println("maxVertexAttribs ： " + maxVertexAttribs.array()[0]);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES30.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        //把颜色缓冲区设置为我们预设的颜色
        //GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT);
        GLES30.glClearColor(0, 0, 1.0f, 1.0f);
        //glClearColor(1.0f, 1.0f, 1.0f, 1.0f);

        if (program != 0) {
            //将程序加入到OpenGLES环境
            GLES30.glUseProgram(program);
            //绘制三角形
            GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 3);
        }
    }

    /**
     * 加载制定shader的方法
     *
     * @param shaderType shader的类型  GLES20.GL_VERTEX_SHADER   GLES20.GL_FRAGMENT_SHADER
     * @param sourceCode shader的脚本
     * @return shader索引
     */
    private static int loadShader(int shaderType, String sourceCode) {
        // 创建一个新shader
        System.out.println("loadShader " + EGL14.eglGetCurrentContext());
        int shader = GLES30.glCreateShader(shaderType);
        // 若创建成功则加载shader
        if (shader != 0) {
            // 加载shader的源代码
            GLES30.glShaderSource(shader, sourceCode);
            // 编译shader
            GLES30.glCompileShader(shader);
            // 存放编译成功shader数量的数组
            int[] compiled = new int[1];
            // 获取Shader的编译情况
            GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, compiled, 0);
            if (compiled[0] == 0) {//若编译失败则显示错误日志并删除此shader
                Log.e("ES20_ERROR", "Could not compile shader " + shaderType + ":");
                Log.e("ES20_ERROR", GLES30.glGetShaderInfoLog(shader));
                GLES30.glDeleteShader(shader);
                shader = 0;
            }
        }
        return shader;
    }

    /**
     * 创建shader程序的方法
     */
    public static int createProgram(String vertexSource, String fragmentSource) {
        //加载顶点着色器
        int vertexShader = loadShader(GLES30.GL_VERTEX_SHADER, vertexSource);
        if (vertexShader == 0) {
            return 0;
        }

        // 加载片元着色器
        int pixelShader = loadShader(GLES30.GL_FRAGMENT_SHADER, fragmentSource);
        if (pixelShader == 0) {
            return 0;
        }

        // 创建程序
        int program = GLES30.glCreateProgram();
        // 若程序创建成功则向程序中加入顶点着色器与片元着色器
        if (program != 0) {
            // 向程序中加入顶点着色器
            GLES30.glAttachShader(program, vertexShader);
            // 向程序中加入片元着色器
            GLES30.glAttachShader(program, pixelShader);
            // 链接程序
            GLES30.glLinkProgram(program);
            // 存放链接成功program数量的数组
            int[] linkStatus = new int[1];
            // 获取program的链接情况
            GLES30.glGetProgramiv(program, GLES30.GL_LINK_STATUS, linkStatus, 0);
            // 若链接失败则报错并删除程序
            if (linkStatus[0] != GLES30.GL_TRUE) {
                Log.e("ES20_ERROR", "Could not link program: ");
                Log.e("ES20_ERROR", GLES30.glGetProgramInfoLog(program));
                GLES30.glDeleteProgram(program);
                program = 0;
            }
        }
        return program;
    }

    private static final String vertexShaderCode =
            "#version 300 es\n" +
                    "layout(location = 0) in vec3 aPos;" +
                    "out vec4 color;" +
                    "void main(){" +
                    "gl_Position = vec4(aPos.x, aPos.y, aPos.z, 1.0);" +
                    "color = vec4(0.5, 0.0, 0.0, 1.0);" +
                    "}";

    private static final String fragmentShaderCode =
            "#version 300 es\n" +
                    "precision mediump float;" +
                    "out vec4 fragColor;" +
                    "in vec4 color;" +
                    "void main(){" +
                    "fragColor = color;" +
                    "}";

    private static final float[] vertices = {
            -0.5f, -0.5f, 0.0f,
            0.5f, -0.5f, 0.0f,
            0.0f, 0.5f, 0.0f};
}