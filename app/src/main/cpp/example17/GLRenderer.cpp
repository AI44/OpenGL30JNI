//
// Created by Raining on 2020/6/20.
// #I# LUT
//

#include <jni.h>
#include <GLES3/gl3.h>
#include <image/ImageUtils.h>
#include "esUtils.h"

namespace example17 {
    static float quadVertices[] = {
            // positions   // texCoords
            -1.0f, 1.0f, 0.0f, 1.0f,
            -1.0f, -1.0f, 0.0f, 0.0f,
            1.0f, 1.0f, 1.0f, 1.0f,
            1.0f, -1.0f, 1.0f, 0.0f
    };

    static esUtils::Shader *orgShader = nullptr;
    static esUtils::Shader *lut3DShader = nullptr;
    static esUtils::Shader *lutShader = nullptr;
    static GLuint quadVAO, quadVBO;

    static GLuint picTexture;
    static GLuint lutTexture3D;
    static GLuint lutTexture;

    static void release() {
        glDeleteVertexArrays(1, &quadVAO);
        glDeleteBuffers(1, &quadVBO);

        if (lut3DShader) {
            lut3DShader->release();
            delete lut3DShader;
            lut3DShader = nullptr;
        }
    }

    static bool lutUse3D = true;
    static bool compare = false;
}

using namespace example17;

extern "C"
JNIEXPORT void JNICALL
Java_com_ideacarry_example17_GLRenderer_surfaceCreated(JNIEnv *env, jobject thiz, jobject context, jint bg_color) {
    if (orgShader) {
        orgShader->release();
        delete orgShader;
        orgShader = nullptr;
    }
    if (lut3DShader) {
        lut3DShader->release();
        delete lut3DShader;
        lut3DShader = nullptr;
    }
    if (lutShader) {
        lutShader->release();
        delete lutShader;
        lutShader = nullptr;
    }
    release();
    glDeleteTextures(1, &picTexture);
    glDeleteTextures(1, &lutTexture3D);

    orgShader = new esUtils::Shader(env, context, "example17/vertex.glsl",
                                    "example17/fragment.glsl");
    //使用texture3D实现LUT3D
    lut3DShader = new esUtils::Shader(env, context, "example17/lut_vertex.glsl",
                                      "example17/lut3d_fragment.glsl");
    //使用算法实现4x4的LUT3D
    lutShader = new esUtils::Shader(env, context, "example17/lut_vertex.glsl",
                                    "example17/lut4x4_fragment.glsl");

    glGenVertexArrays(1, &quadVAO);
    glGenBuffers(1, &quadVBO);
    glBindVertexArray(quadVAO);
    glBindBuffer(GL_ARRAY_BUFFER, quadVBO);
    glBufferData(GL_ARRAY_BUFFER, sizeof(quadVertices), quadVertices, GL_STATIC_DRAW);
    glEnableVertexAttribArray(0);
    glVertexAttribPointer(0, 2, GL_FLOAT, GL_FALSE, 4 * sizeof(float), (void *) 0);
    glEnableVertexAttribArray(1);
    glVertexAttribPointer(1, 2, GL_FLOAT, GL_FALSE, 4 * sizeof(float),
                          (void *) (2 * sizeof(float)));

    picTexture = loadAssetsTexture2D(env, context, "lenna_std.jpg");

    jobject bmp = readAssetImage(env, context, "example17/yu4.png");
    Image8888 img;
    img.SetImage(env, bmp);
    if (bmp) {
#define LUT_WIDTH 16
#define LUT_HEIGHT 16
#define LUT_W_NUM 4
#define LUT_H_NUM 4
        int depth = LUT_W_NUM * LUT_H_NUM;
        glGenTextures(1, &lutTexture3D);
        glBindTexture(GL_TEXTURE_3D, lutTexture3D);
        checkGlError("glBindTexture 3D");
        glTexImage3D(GL_TEXTURE_3D, 0, GL_RGBA8, LUT_WIDTH, LUT_HEIGHT, depth, 0,
                     GL_RGBA, GL_UNSIGNED_BYTE, nullptr);
        checkGlError("glTexImage3D");
        glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);
        glPixelStorei(GL_UNPACK_ALIGNMENT, 4);//4字节对齐
        glPixelStorei(GL_UNPACK_ROW_LENGTH, img.m_width);
        for (int i = 0; i < depth; i++) {
            glTexSubImage3D(GL_TEXTURE_3D, 0, 0, 0, i, LUT_WIDTH,
                            LUT_HEIGHT, 1, GL_RGBA, GL_UNSIGNED_BYTE,
                            img.m_pDatas +
                            i / LUT_W_NUM * LUT_WIDTH * LUT_HEIGHT * LUT_W_NUM * 4 +
                            (i % LUT_W_NUM) * LUT_WIDTH * 4);
            checkGlError("glTexSubImage3D");
        }
        glPixelStorei(GL_UNPACK_ROW_LENGTH, 0);
        glPixelStorei(GL_UNPACK_SKIP_PIXELS, 0);

        img.ClearAll();
        env->DeleteLocalRef(bmp);
    }

    lutTexture = loadAssetsTexture2D(env, context, "example17/yu4.png");

    orgShader->use();
    glUniform1i(2, 0);//pic
    lut3DShader->use();
    glUniform1i(2, 0);//pic
    glUniform1i(3, 1);//lut
    glUniform1f(4, 1.0f);//intensity
    lutShader->use();
    glUniform1i(2, 0);//pic
    glUniform1i(3, 1);//lut
    glUniform1f(4, 1.0f);//intensity
}

extern "C"
JNIEXPORT void JNICALL
Java_com_ideacarry_example17_GLRenderer_surfaceChanged(JNIEnv *env, jobject thiz, jint width, jint height) {
    ALOGE("w=%d,h=%d", width, height);
    glViewport(0, (height - width) / 2, width, width);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_ideacarry_example17_GLRenderer_drawFrame(JNIEnv *env, jobject thiz) {
    glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
    glClear(GL_COLOR_BUFFER_BIT);

    if (compare) {
        if (orgShader) {
            orgShader->use();
            glBindVertexArray(quadVAO);
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, picTexture);
            glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
        }
    } else {
        if (lutUse3D) {
            if (lut3DShader) {
                lut3DShader->use();
                glBindVertexArray(quadVAO);
                glActiveTexture(GL_TEXTURE0);
                glBindTexture(GL_TEXTURE_2D, picTexture);
                glActiveTexture(GL_TEXTURE1);
                glBindTexture(GL_TEXTURE_3D, lutTexture3D);
                checkGlError("glBindTexture 3D");
                glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
            }
        } else {
            if (lutShader) {
                lutShader->use();
                glBindVertexArray(quadVAO);
                glActiveTexture(GL_TEXTURE0);
                glBindTexture(GL_TEXTURE_2D, picTexture);
                glActiveTexture(GL_TEXTURE1);
                glBindTexture(GL_TEXTURE_2D, lutTexture);
                glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
            }
        }
    }
    glBindVertexArray(0);
    glUseProgram(0);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_ideacarry_example17_GLRenderer_compare(JNIEnv *env, jclass clazz, jboolean press) {
    compare = press;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_ideacarry_example17_GLRenderer_lut3D(JNIEnv *env, jclass clazz, jboolean use3_d) {
    lutUse3D = use3_d;
}