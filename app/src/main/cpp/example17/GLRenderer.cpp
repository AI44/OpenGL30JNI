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
    static esUtils::Shader *lut2DArrShader = nullptr;
    static esUtils::Shader *lutShader = nullptr;
    static GLuint quadVAO, quadVBO;

    static GLuint picTexture;
    static GLuint lutTexture3D;
    static GLuint lutTexture2DArr;
    static GLuint lutTexture;

    static void release() {
        glDeleteVertexArrays(1, &quadVAO);
        glDeleteBuffers(1, &quadVBO);

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
        if (lut2DArrShader) {
            lut2DArrShader->release();
            delete lut2DArrShader;
            lut2DArrShader = nullptr;
        }
        if (lutShader) {
            lutShader->release();
            delete lutShader;
            lutShader = nullptr;
        }
    }

    static int lutType = 0;
    static bool compare = false;
}

using namespace example17;

extern "C"
JNIEXPORT void JNICALL
Java_com_ideacarry_example17_GLRenderer_surfaceCreated(JNIEnv *env, jobject thiz, jobject context,
                                                       jint bg_color) {
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
    if (lut2DArrShader) {
        lut2DArrShader->release();
        delete lut2DArrShader;
        lut2DArrShader = nullptr;
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

    //使用texture2D array实现LUT3D
    lut2DArrShader = new esUtils::Shader(env, context, "example17/lut_vertex.glsl",
                                         "example17/lut2d4x4_fragment.glsl");

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
        lutTexture3D = createTexture3D(GL_TEXTURE_3D, img.m_pDatas, img.m_width, img.m_height,
                                       4, 4);
        lutTexture2DArr = createTexture3D(GL_TEXTURE_2D_ARRAY, img.m_pDatas, img.m_width,
                                          img.m_height, 4, 4);

        img.ClearAll();
        env->DeleteLocalRef(bmp);
    }

    lutTexture = loadAssetsTexture2D(env, context, "example17/yu4.png");

    orgShader->use();
    orgShader->setInt("uTexturePic", 0);//pic

    lut3DShader->use();
    lut3DShader->setInt("uTexturePic", 0);//pic
    lut3DShader->setInt("uTextureLUT", 1);//lut
    lut3DShader->setFloat("intensity", 1.0f);//intensity

    lut2DArrShader->use();
    lut2DArrShader->setInt("uTexturePic", 0);//pic
    lut2DArrShader->setInt("uTextureLUT", 1);//lut
    lut2DArrShader->setFloat("intensity", 1.0f);//intensity

    lutShader->use();
    lutShader->setInt("uTexturePic", 0);//pic
    lutShader->setInt("uTextureLUT", 1);//lut
    lutShader->setFloat("intensity", 1.0f);//intensity
}

extern "C"
JNIEXPORT void JNICALL
Java_com_ideacarry_example17_GLRenderer_surfaceChanged(JNIEnv *env, jobject thiz, jint width,
                                                       jint height) {
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
        switch (lutType) {
            case 0: //3d
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
                break;

            case 1://2d arr
                if (lut2DArrShader) {
                    lut2DArrShader->use();
                    glBindVertexArray(quadVAO);
                    glActiveTexture(GL_TEXTURE0);
                    glBindTexture(GL_TEXTURE_2D, picTexture);
                    glActiveTexture(GL_TEXTURE1);
                    glBindTexture(GL_TEXTURE_2D_ARRAY, lutTexture2DArr);
                    checkGlError("glBindTexture 3D");
                    glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
                }
                break;

            case 2://old
                if (lutShader) {
                    lutShader->use();
                    glBindVertexArray(quadVAO);
                    glActiveTexture(GL_TEXTURE0);
                    glBindTexture(GL_TEXTURE_2D, picTexture);
                    glActiveTexture(GL_TEXTURE1);
                    glBindTexture(GL_TEXTURE_2D, lutTexture);
                    glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
                }
                break;

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
Java_com_ideacarry_example17_GLRenderer_lut3D(JNIEnv *env, jclass clazz, jint type) {
    lutType = type;
}