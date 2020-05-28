//
// Created by Raining on 2019/8/9.
// #I# uniform
//

#include <GLES3/gl3.h>
#include <jni.h>
#include <ctime>
#include <cmath>
#include "esUtils.h"

namespace example4 {
    static const char *vertexShaderCode =
            "#version 300 es\n"
            "layout(location = 0) in vec3 aPos;"
            "void main(){"
            "gl_Position = vec4(aPos.x, aPos.y, aPos.z, 1.0);"
            "}";

    static const char *fragmentShaderCode =
            "#version 300 es\n"
            "precision mediump float;"
            "out vec4 fragColor;"
            "uniform vec4 ourColor;"
            "void main(){"
            "fragColor = ourColor;"
            "}";

    static const GLfloat vertices[] = {
            -0.5f, -0.5f, 0.0f,
            0.5f, -0.5f, 0.0f,
            0.0f, 0.5f, 0.0f};

    static GLuint program = 0;
    static GLuint vao;
}

using namespace example4;

extern "C"
JNIEXPORT void JNICALL
Java_com_ideacarry_example4_GLRenderer_surfaceCreated(JNIEnv *env, jobject thiz, jobject context, jint color) {
    color = 0xffff8030;
    GLfloat alphaF = ((color >> 24) & 0xFF) * 1.0f / 255;
    GLfloat redF = ((color >> 16) & 0xFF) * 1.0f / 255;
    GLfloat greenF = ((color >> 8) & 0xFF) * 1.0f / 255;
    GLfloat blueF = (color & 0xFF) * 1.0f / 255;
    glClearColor(redF, greenF, blueF, alphaF);

    program = createProgram(vertexShaderCode, fragmentShaderCode);
    if (program) {
        glGenVertexArrays(1, &vao);

        glBindVertexArray(vao);

        glBindBuffer(GL_ARRAY_BUFFER, vao);
        glBufferData(GL_ARRAY_BUFFER, sizeof(vertices), vertices, GL_STATIC_DRAW);

        glVertexAttribPointer(0, 3, GL_FLOAT, GL_FALSE, 3 * sizeof(float), nullptr);
        glEnableVertexAttribArray(0);

        glBindVertexArray(0);
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_ideacarry_example4_GLRenderer_surfaceChanged(JNIEnv *env, jobject thiz, jint width, jint height) {
    glViewport(0, 0, width, height);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_ideacarry_example4_GLRenderer_drawFrame(JNIEnv *env, jobject thiz) {
    //把颜色缓冲区设置为我们预设的颜色
    glClear(GL_COLOR_BUFFER_BIT);

    if (program) {
        timeval tv;
        gettimeofday(&tv, nullptr);
        long tme = tv.tv_sec * 1000 + tv.tv_usec / 1000;
        float ran = (float) ((tme % 5000) / 5000.0f * M_PI);
        float greenValue = sin(ran);
        //ALOGE("%i", tv.tv_usec);
        GLint colorLocation = glGetUniformLocation(program, "ourColor");

        glUseProgram(program);
        glUniform4f(colorLocation, 0.0f, greenValue, 0.0f, 1.0f);
        glBindVertexArray(vao);
        glDrawArrays(GL_TRIANGLES, 0, 3);
    }
}