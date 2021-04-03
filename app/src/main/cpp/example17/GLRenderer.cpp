//
// Created by Raining on 2020/6/20.
// #I# LUT
//

#include <jni.h>
#include <GLES3/gl3.h>
#include <image/ImageUtils.h>
#include "esUtils.h"

namespace example17 {

#define LUT_4X4_PATH "example17/test_lut.png"
//#define LUT_8X8_PATH "example17/fugu_lut.png"
#define LUT_8X8_PATH "example17/neutral_lut.png"

    static float quadVertices[] = {
            // positions   // texCoords
            -1.0f, 1.0f, 0.0f, 1.0f,
            -1.0f, -1.0f, 0.0f, 0.0f,
            1.0f, 1.0f, 1.0f, 1.0f,
            1.0f, -1.0f, 1.0f, 0.0f
    };

    static esUtils::Shader *orgShader = nullptr;

    static esUtils::Shader *lut3D4x4Shader = nullptr;
    static esUtils::Shader *lut2D4x4ArrShader = nullptr;
    static esUtils::Shader *lut4x4Shader = nullptr;

    static esUtils::Shader *lut3D8x8Shader = nullptr;
    static esUtils::Shader *lut8x8Shader = nullptr;

    static GLuint quadVAO, quadVBO;

    static GLuint picTexture;

    static GLuint lutTexture3D4x4;
    static GLuint lutTexture2DArr4x4;
    static GLuint lutTexture4x4;

    static GLuint lutTexture3D8x8;
    static GLuint lutTexture8x8;

    static void release() {
        glDeleteVertexArrays(1, &quadVAO);
        glDeleteBuffers(1, &quadVBO);

        if (orgShader) {
            orgShader->release();
            delete orgShader;
            orgShader = nullptr;
        }

        if (lut3D4x4Shader) {
            lut3D4x4Shader->release();
            delete lut3D4x4Shader;
            lut3D4x4Shader = nullptr;
        }
        if (lut2D4x4ArrShader) {
            lut2D4x4ArrShader->release();
            delete lut2D4x4ArrShader;
            lut2D4x4ArrShader = nullptr;
        }
        if (lut4x4Shader) {
            lut4x4Shader->release();
            delete lut4x4Shader;
            lut4x4Shader = nullptr;
        }

        if (lut3D8x8Shader) {
            lut3D8x8Shader->release();
            delete lut3D8x8Shader;
            lut3D8x8Shader = nullptr;
        }
        if (lut8x8Shader) {
            lut8x8Shader->release();
            delete lut8x8Shader;
            lut8x8Shader = nullptr;
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
    if (lut3D4x4Shader) {
        lut3D4x4Shader->release();
        delete lut3D4x4Shader;
        lut3D4x4Shader = nullptr;
    }
    if (lut2D4x4ArrShader) {
        lut2D4x4ArrShader->release();
        delete lut2D4x4ArrShader;
        lut2D4x4ArrShader = nullptr;
    }
    if (lut4x4Shader) {
        lut4x4Shader->release();
        delete lut4x4Shader;
        lut4x4Shader = nullptr;
    }
    if (lut3D8x8Shader) {
        lut3D8x8Shader->release();
        delete lut3D8x8Shader;
        lut3D8x8Shader = nullptr;
    }
    if (lut8x8Shader) {
        lut8x8Shader->release();
        delete lut8x8Shader;
        lut8x8Shader = nullptr;
    }
    release();
    glDeleteTextures(1, &picTexture);
    glDeleteTextures(1, &lutTexture3D4x4);

    orgShader = new esUtils::Shader(env, context, "example17/vertex.glsl",
                                    "example17/fragment.glsl");
    //使用texture3D实现4x4的LUT3D
    lut3D4x4Shader = new esUtils::Shader(env, context, "example17/lut_vertex.glsl",
                                         "example17/lut3d_fragment.glsl");

    //使用texture2D array实现4x4的LUT3D
    lut2D4x4ArrShader = new esUtils::Shader(env, context, "example17/lut_vertex.glsl",
                                            "example17/lut2d4x4_fragment.glsl");

    //使用算法实现4x4的LUT3D
    lut4x4Shader = new esUtils::Shader(env, context, "example17/lut_vertex.glsl",
                                       "example17/lut4x4_fragment.glsl");

    //使用texture3D实现8x8的LUT3D
    lut3D8x8Shader = new esUtils::Shader(env, context, "example17/lut_vertex.glsl",
                                         "example17/lut3d_fragment.glsl");

    //使用算法实现8x8的LUT3D
    lut8x8Shader = new esUtils::Shader(env, context, "example17/lut_vertex.glsl",
                                       "example17/lut8x8_fragment.glsl");

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

    jobject bmp = readAssetImage(env, context, LUT_4X4_PATH);
    Image8888 img;
    img.SetImage(env, bmp);
    if (bmp) {
        lutTexture3D4x4 = createTexture3D(GL_TEXTURE_3D, img.m_pDatas, img.m_width, img.m_height,
                                          4, 4);
        lutTexture2DArr4x4 = createTexture3D(GL_TEXTURE_2D_ARRAY, img.m_pDatas, img.m_width,
                                             img.m_height, 4, 4);

        img.ClearAll();
        env->DeleteLocalRef(bmp);
    }
    lutTexture4x4 = loadAssetsTexture2D(env, context, LUT_4X4_PATH);

    bmp = readAssetImage(env, context, LUT_8X8_PATH);
    img.SetImage(env, bmp);
    if (bmp) {
        lutTexture3D8x8 = createTexture3D(GL_TEXTURE_3D, img.m_pDatas, img.m_width, img.m_height,
                                          8, 8);
        img.ClearAll();
        env->DeleteLocalRef(bmp);
    }
    lutTexture8x8 = loadAssetsTexture2D(env, context, LUT_8X8_PATH);

    orgShader->use();
    orgShader->setInt("uTexturePic", 0);//pic

    lut3D4x4Shader->use();
    lut3D4x4Shader->setInt("uTexturePic", 0);//pic
    lut3D4x4Shader->setInt("uTextureLUT", 1);//lut
    lut3D4x4Shader->setFloat("intensity", 1.0f);//intensity

    lut2D4x4ArrShader->use();
    lut2D4x4ArrShader->setInt("uTexturePic", 0);//pic
    lut2D4x4ArrShader->setInt("uTextureLUT", 1);//lut
    lut2D4x4ArrShader->setFloat("intensity", 1.0f);//intensity

    lut4x4Shader->use();
    lut4x4Shader->setInt("uTexturePic", 0);//pic
    lut4x4Shader->setInt("uTextureLUT", 1);//lut
    lut4x4Shader->setFloat("intensity", 1.0f);//intensity

    lut3D8x8Shader->use();
    lut3D8x8Shader->setInt("uTexturePic", 0);//pic
    lut3D8x8Shader->setInt("uTextureLUT", 1);//lut
    lut3D8x8Shader->setFloat("intensity", 1.0f);//intensity

    lut8x8Shader->use();
    lut8x8Shader->setInt("uTexturePic", 0);//pic
    lut8x8Shader->setInt("uTextureLUT", 1);//lut
    lut8x8Shader->setFloat("intensity", 1.0f);//intensity
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
        switch (lutType) {
            case 0: //3d 4x4
                if (lut3D4x4Shader) {
                    lut3D4x4Shader->use();
                    glBindVertexArray(quadVAO);
                    glActiveTexture(GL_TEXTURE0);
                    glBindTexture(GL_TEXTURE_2D, picTexture);
                    glActiveTexture(GL_TEXTURE1);
                    glBindTexture(GL_TEXTURE_3D, lutTexture3D4x4);
                    checkGlError("glBindTexture 3D");
                    glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
                }
                break;

            case 1://2d arr 4x4
                if (lut2D4x4ArrShader) {
                    lut2D4x4ArrShader->use();
                    glBindVertexArray(quadVAO);
                    glActiveTexture(GL_TEXTURE0);
                    glBindTexture(GL_TEXTURE_2D, picTexture);
                    glActiveTexture(GL_TEXTURE1);
                    glBindTexture(GL_TEXTURE_2D_ARRAY, lutTexture2DArr4x4);
                    checkGlError("glBindTexture 3D");
                    glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
                }
                break;

            case 2://old 4x4
                if (lut4x4Shader) {
                    lut4x4Shader->use();
                    glBindVertexArray(quadVAO);
                    glActiveTexture(GL_TEXTURE0);
                    glBindTexture(GL_TEXTURE_2D, picTexture);
                    glActiveTexture(GL_TEXTURE1);
                    glBindTexture(GL_TEXTURE_2D, lutTexture4x4);
                    glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
                }
                break;

            case 3://3d 8x8
                if (lut3D8x8Shader) {
                    lut3D8x8Shader->use();
                    glBindVertexArray(quadVAO);
                    glActiveTexture(GL_TEXTURE0);
                    glBindTexture(GL_TEXTURE_2D, picTexture);
                    glActiveTexture(GL_TEXTURE1);
                    glBindTexture(GL_TEXTURE_3D, lutTexture3D8x8);
                    checkGlError("glBindTexture 3D");
                    glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
                }
                break;

            case 4://old 8x8
                if (lut8x8Shader) {
                    lut8x8Shader->use();
                    glBindVertexArray(quadVAO);
                    glActiveTexture(GL_TEXTURE0);
                    glBindTexture(GL_TEXTURE_2D, picTexture);
                    glActiveTexture(GL_TEXTURE1);
                    glBindTexture(GL_TEXTURE_2D, lutTexture8x8);
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
Java_com_ideacarry_example17_GLRenderer_lut3D(JNIEnv *env, jclass clazz, jint type) {
    lutType = type;
}