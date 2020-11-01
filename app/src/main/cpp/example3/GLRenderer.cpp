//
// Created by Raining on 2019/8/9.
// #I# VBO + VAO + EBO
//

#include <GLES3/gl3.h>
#include <jni.h>
#include "esUtils.h"

namespace example3 {
    static const char *vertexShaderCode =
            "#version 300 es\n"
            "layout(location = 0) in vec3 aPos;"
            "out vec4 color;"
            "void main(){"
            "gl_Position = vec4(aPos.xyz, 1.0);"
            "color = vec4(0.0, 1.0, 0.0, 1.0);"
            "}";

    static const char *fragmentShaderCode =
            "#version 300 es\n"
            "precision mediump float;"
            "out vec4 fragColor;"
            "in vec4 color;"
            "void main(){"
            "fragColor = color;"
            "}";

    static float vertices[] = {
            0.5f, 0.5f, 0.0f,   // 右上角
            0.5f, -0.5f, 0.0f,  // 右下角
            -0.5f, -0.5f, 0.0f, // 左下角
            -0.5f, 0.5f, 0.0f   // 左上角
    };

    static unsigned int indices[] = { // 注意索引从0开始!
            0, 1, 3, // 第一个三角形
            1, 2, 3  // 第二个三角形
    };

    static GLuint program = 0;
    static GLuint vao;
}

using namespace example3;

extern "C"
JNIEXPORT void JNICALL
Java_com_ideacarry_example3_GLRenderer_surfaceCreated(JNIEnv *env, jobject thiz, jobject context, jint color) {
    color = 0xffff00ff;
    GLfloat alphaF = ((color >> 24) & 0xFF) * 1.0f / 255;
    GLfloat redF = ((color >> 16) & 0xFF) * 1.0f / 255;
    GLfloat greenF = ((color >> 8) & 0xFF) * 1.0f / 255;
    GLfloat blueF = (color & 0xFF) * 1.0f / 255;
    glClearColor(redF, greenF, blueF, alphaF);

    program = createProgram(vertexShaderCode, fragmentShaderCode);
    if (program) {
        GLuint vbo;
        glGenBuffers(1, &vbo);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, sizeof(vertices), vertices, GL_STATIC_DRAW);

        GLuint ebo;
        glGenBuffers(1, &ebo);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, sizeof(indices), indices, GL_STATIC_DRAW);

        //VAO 初始化部分
        glGenVertexArrays(1, &vao);
        glBindVertexArray(vao);

        //开始保存状态
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glVertexAttribPointer(0, 3, GL_FLOAT, GL_FALSE, 3 * sizeof(float), (void *) 0);
        glEnableVertexAttribArray(0);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);

        //保存结束
        glBindVertexArray(0);

        //glDeleteBuffers(1, &vbo);
        //glDeleteBuffers(1, &ebo);
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_ideacarry_example3_GLRenderer_surfaceChanged(JNIEnv *env, jobject thiz, jint width, jint height) {
    glViewport(0, 0, width, height);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_ideacarry_example3_GLRenderer_drawFrame(JNIEnv *env, jobject thiz) {
    //把颜色缓冲区设置为我们预设的颜色
    glClear(GL_COLOR_BUFFER_BIT);

    if (program) {
        glUseProgram(program);
        glBindVertexArray(vao);
        //glDrawElements(GL_LINE_LOOP, 6, GL_UNSIGNED_INT, 0);//画线
        glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);
        glBindVertexArray(0);
    }
}