//
// Created by Raining on 2019/8/9.
// #I# 线性插值效果 & assets文件调用
//

#include <GLES3/gl3.h>
#include <jni.h>
#include "esUtils.h"
#include <cstdlib>

namespace example5 {
    static float vertices[] = {
            // 位置              // 颜色
            0.5f, -0.5f, 0.0f, 1.0f, 0.0f, 0.0f,   // 右下
            -0.5f, -0.5f, 0.0f, 0.0f, 1.0f, 0.0f,   // 左下
            0.0f, 0.5f, 0.0f, 0.0f, 0.0f, 1.0f    // 顶部
    };

    static GLuint program = 0;
    static GLuint vao = 0;
}

using namespace example5;

extern "C"
JNIEXPORT void JNICALL
Java_com_ideacarry_example5_GLRenderer_surfaceCreated(JNIEnv *env, jobject thiz, jobject context, jint color) {
    color = 0xffff8000;
    GLfloat alphaF = ((color >> 24) & 0xFF) * 1.0f / 255;
    GLfloat redF = ((color >> 16) & 0xFF) * 1.0f / 255;
    GLfloat greenF = ((color >> 8) & 0xFF) * 1.0f / 255;
    GLfloat blueF = (color & 0xFF) * 1.0f / 255;
    glClearColor(redF, greenF, blueF, alphaF);

    char *vertexShaderCode = (char *) readAssetFile(env, context, "example5/vertex.glsl", true, nullptr);
    char *fragmentShaderCode = (char *) readAssetFile(env, context, "example5/fragment.glsl", true, nullptr);
    program = createProgram(vertexShaderCode, fragmentShaderCode);
    free(vertexShaderCode);
    free(fragmentShaderCode);

    if (program) {
        glGenVertexArrays(1, &vao);
        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vao);
        glBufferData(GL_ARRAY_BUFFER, sizeof(vertices), vertices, GL_STATIC_DRAW);

        //位置属性
        glVertexAttribPointer(0, 3, GL_FLOAT, GL_FALSE, 6 * sizeof(float), nullptr);
        glEnableVertexAttribArray(0);

        //颜色属性
        glVertexAttribPointer(1, 3, GL_FLOAT, GL_FALSE,
                              6 * sizeof(float), (void *) (3 * sizeof(float)));
        glEnableVertexAttribArray(1);

        glBindVertexArray(0);
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_ideacarry_example5_GLRenderer_surfaceChanged(JNIEnv *env, jobject thiz, jint width, jint height) {
    glViewport(0, 0, width, height);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_ideacarry_example5_GLRenderer_drawFrame(JNIEnv *env, jobject thiz) {
    //把颜色缓冲区设置为我们预设的颜色
    glClear(GL_COLOR_BUFFER_BIT);

    if (program) {
        glUseProgram(program);
        glBindVertexArray(vao);
        glDrawArrays(GL_TRIANGLES, 0, 3);
    }
}