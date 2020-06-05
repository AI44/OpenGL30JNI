//
// Created by Raining on 2020/5/28.
// #I# 渲染yuv数据 + matrix
//

#include <jni.h>
#include <GLES3/gl3.h>
#include "esUtils.h"

namespace example13 {
    static float quadVertices[] = {
            // positions   // texCoords
            -1.0f, 1.0f, 0.0f, 1.0f,
            -1.0f, -1.0f, 0.0f, 0.0f,
            1.0f, 1.0f, 1.0f, 1.0f,
            1.0f, -1.0f, 1.0f, 0.0f
    };

    static const int IMAGE_WIDTH = 1280;
    static const int IMAGE_HEIGHT = 720;

    static esUtils::Shader *shader = nullptr;
    static GLuint textureY;
    static GLuint textureVU;
    static GLuint quadVAO, quadVBO;

    static void release() {
        glDeleteVertexArrays(1, &quadVAO);
        glDeleteBuffers(1, &quadVBO);

        if (shader) {
            shader->release();
            delete shader;
            shader = nullptr;
        }
    }
}

using namespace example13;

extern "C"
JNIEXPORT void JNICALL
Java_com_ideacarry_example13_GLRenderer_surfaceCreated(JNIEnv *env, jobject thiz, jobject context, jint bg_color) {
    if (shader) {
        shader->release();
        delete shader;
        shader = nullptr;
    }
    shader = new esUtils::Shader(env, context, "example13/vertex.glsl",
                                 "example13/fragment.glsl");

    glGenVertexArrays(1, &quadVAO);
    glGenBuffers(1, &quadVBO);
    glBindVertexArray(quadVAO);
    glBindBuffer(GL_ARRAY_BUFFER, quadVBO);
    glBufferData(GL_ARRAY_BUFFER, sizeof(quadVertices), &quadVertices, GL_STATIC_DRAW);
    glEnableVertexAttribArray(0);
    glVertexAttribPointer(0, 2, GL_FLOAT, GL_FALSE, 4 * sizeof(float), (void *) 0);
    glEnableVertexAttribArray(1);
    glVertexAttribPointer(1, 2, GL_FLOAT, GL_FALSE, 4 * sizeof(float),
                          (void *) (2 * sizeof(float)));


    int dataLen = 0;
    uint8_t *data = readAssetFile(env, context, "yuv1280x720", false, &dataLen);
    if (data) {
        glGenTextures(1, &textureY);
        glBindTexture(GL_TEXTURE_2D, textureY);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_R8, IMAGE_WIDTH, IMAGE_HEIGHT, 0,
                     GL_RED, GL_UNSIGNED_BYTE, data);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

        glGenTextures(1, &textureVU);
        glBindTexture(GL_TEXTURE_2D, textureVU);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RG8, IMAGE_WIDTH / 2, IMAGE_HEIGHT / 2,
                     0, GL_RG, GL_UNSIGNED_BYTE, data + IMAGE_WIDTH * IMAGE_HEIGHT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

        delete data;
    }
    //设置常量到program
    shader->use();
    //设置常量y
    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D, textureY);
    glUniform1i(3, 0);
    glBindTexture(GL_TEXTURE_2D, 0);
    //设置常量vu
    glActiveTexture(GL_TEXTURE1);
    glBindTexture(GL_TEXTURE_2D, textureVU);
    glUniform1i(4, 1);
    glBindTexture(GL_TEXTURE_2D, 0);
    //matrix
    glm::mat4 matrix = glm::mat4(1.0f);
    //顺时针旋转90度
    matrix = glm::rotate(matrix, glm::radians(-90.0f), glm::vec3(0.0f, 0.0f, 1.0f));
    //左右翻转
    matrix = glm::scale(matrix, glm::vec3(1.0f, -1.0f, 1.0f));
    glUniformMatrix4fv(2, 1, GL_FALSE, &matrix[0][0]);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_ideacarry_example13_GLRenderer_surfaceChanged(JNIEnv *env, jobject thiz, jint width, jint height) {
    glViewport(50, 50, IMAGE_HEIGHT, IMAGE_WIDTH); //图像旋转90度
}

extern "C"
JNIEXPORT void JNICALL
Java_com_ideacarry_example13_GLRenderer_drawFrame(JNIEnv *env, jobject thiz) {
    if (shader) {
        glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT);

        shader->use();
        glBindVertexArray(quadVAO);

        glActiveTexture(GL_TEXTURE0); // 在绑定纹理之前先激活纹理单元
        glBindTexture(GL_TEXTURE_2D, textureY);

        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D, textureVU);

        glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
        //glDrawArrays(GL_LINE_LOOP, 0, 4);

        glBindVertexArray(0);
        glUseProgram(0);
    }
}