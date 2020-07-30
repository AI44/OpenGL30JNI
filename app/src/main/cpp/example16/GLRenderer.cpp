//
// Created by Raining on 2020/6/6.
// #I# 美颜 + textureView
//

#include <GLES3/gl3.h>
#include <GLES2/gl2ext.h>
#include <jni.h>
#include <cmath>
#include "esUtils.h"
#include "CameraFilter.h"
#include "SkinFilter.h"
#include "BeautyBlurFilter.h"
#include "ScreenFilter.h"
#include "TextureManager.h"
#include "BlurDiffFilter.h"
#include "BlurMixFilter.h"

namespace example16 {
    static float quadVertices[] = {
            // positions   // texCoords
            -1.0f, 1.0f, 0.0f, 1.0f,
            -1.0f, -1.0f, 0.0f, 0.0f,
            1.0f, 1.0f, 1.0f, 1.0f,
            1.0f, -1.0f, 1.0f, 0.0f
    };

    static TextureManager *textureMgr = nullptr;
    static RendererData *orgCameraTexture = nullptr;
    static RendererData *firstBlurTexture = nullptr;

    static CameraFilter cameraFilter;
    static SkinFilter skinFilter;
    static BeautyBlurFilter beautyBlurFilter;
    static BlurDiffFilter blurDiffFilter;
    static BlurMixFilter blurMixFilter;
    static ScreenFilter screenFilter;

    static GLuint quadVAO, quadVBO;
    static int screenWidth, screenHeight;
    static int textureWidth, textureHeight;
    static int blurWidth, blurHeight;
    static bool compare;
}

using namespace example16;

extern "C"
JNIEXPORT void JNICALL
Java_com_ideacarry_example16_GLRenderer_surfaceCreated(JNIEnv *env, jclass clazz, jobject context) {

    cameraFilter.init(env, context);
    skinFilter.init(env, context);
    beautyBlurFilter.init(env, context);
    blurDiffFilter.init(env, context);
    blurMixFilter.init(env, context);
    screenFilter.init(env, context);

    // screen quad VAO
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

    if (textureMgr) {
        delete textureMgr;
        textureMgr = nullptr;
    }
    textureMgr = new TextureManager();
}

extern "C"
JNIEXPORT void JNICALL
Java_com_ideacarry_example16_GLRenderer_surfaceChanged(JNIEnv *env, jclass clazz, jint width, jint height, jint camera_width, jint camera_height, jint degree) {
    //计算texture大小
    int w;
    int h;
    float scale = fmax((float) camera_width / (float) width,
                       (float) camera_height / (float) height);
    if (scale < 1) {
        scale = (float) width / (float) height;
        if (scale > 1) {
            w = camera_width;
            h = w / scale;
        } else {
            h = camera_height;
            w = h * scale;
        }
    } else {
        w = width;
        h = height;
    }
    w = (w >> 1) << 1;
    h = (h >> 1) << 1;
    ALOGE("width=%d, height=%d, cW=%d, cH=%d, w=%d, h=%d", width, height, camera_width,
          camera_height, w, h);

    screenWidth = width;
    screenHeight = height;
    textureWidth = w;
    textureHeight = h;
    blurWidth = BeautyBlurFilter::getSize(textureWidth);
    blurHeight = BeautyBlurFilter::getSize(textureHeight);

    orgCameraTexture = textureMgr->getData(textureWidth, textureHeight, false);
    firstBlurTexture = textureMgr->getData(blurWidth, blurHeight, false);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_ideacarry_example16_GLRenderer_drawFrame(JNIEnv *env, jclass clazz, jint texture_id, jfloatArray matrix) {
    //framebuffer------------------------------------------------------------------------------start
    RendererData *data = nullptr;
    RendererData *data2 = nullptr;
    GLuint textureId;

    //camera
    glViewport(0, 0, textureWidth, textureHeight);
    jfloat *mat = env->GetFloatArrayElements(matrix, 0);
    data = textureMgr->getData(textureWidth, textureHeight, true);
    cameraFilter.onDraw(data->framebuffer, texture_id, quadVAO, mat);
    env->ReleaseFloatArrayElements(matrix, mat, 0);

    if (!compare) {
        //美白
        glViewport(0, 0, textureWidth, textureHeight);
        textureId = data->texture;
        skinFilter.onDraw(orgCameraTexture->framebuffer, textureId, quadVAO);
        textureMgr->release(&data);

        //磨皮，水平 + 垂直
        glViewport(0, 0, blurWidth, blurHeight);
#define BLUR_SIZE 1.0f
        float horizontalBlur = BLUR_SIZE / (float) blurWidth;
        float verticalBlur = BLUR_SIZE / (float) blurHeight;
        textureId = orgCameraTexture->texture;
        data = textureMgr->getData(blurWidth, blurHeight, true);
        beautyBlurFilter.onDraw(data->framebuffer, textureId, horizontalBlur, 0, quadVAO);
        textureId = data->texture;
        beautyBlurFilter.onDraw(firstBlurTexture->framebuffer, textureId, 0, verticalBlur, quadVAO);
        textureMgr->release(&data);

        //diff
        glViewport(0, 0, textureWidth, textureHeight);
//        glViewport(0, 0, blurWidth, blurHeight);
        textureId = firstBlurTexture->texture;
        data = textureMgr->getData(textureWidth, textureHeight, true);
//        data = textureMgr->getData(blurWidth, blurHeight, true);
        blurDiffFilter.onDraw(data->framebuffer, orgCameraTexture->texture, textureId, quadVAO);

        //磨皮，水平 + 垂直
//        glViewport(0, 0, blurWidth, blurHeight);
        glViewport(0, 0, textureWidth, textureHeight);
        textureId = data->texture;
//        data2 = textureMgr->getData(blurWidth, blurHeight, true);
        data2 = textureMgr->getData(textureWidth, textureHeight, true);
        beautyBlurFilter.onDraw(data2->framebuffer, textureId, horizontalBlur, 0, quadVAO);
        textureMgr->release(&data);
        textureId = data2->texture;
//        data = textureMgr->getData(blurWidth, blurHeight, true);
        data = textureMgr->getData(textureWidth, textureHeight, true);
        beautyBlurFilter.onDraw(data->framebuffer, textureId, 0, verticalBlur, quadVAO);
        textureMgr->release(&data2);

        //mix
        glViewport(0, 0, textureWidth, textureHeight);
        textureId = data->texture;
        data2 = textureMgr->getData(textureWidth, textureHeight, true);
        blurMixFilter.onDraw(data2->framebuffer, orgCameraTexture->texture,
                             firstBlurTexture->texture, textureId, quadVAO);
        textureMgr->release(&data);
        data = data2;
    }
    //framebuffer--------------------------------------------------------------------------------end

    //screen
    glViewport(0, 0, screenWidth, screenHeight);
    textureId = data->texture;
//    textureId = orgCameraTexture->texture;
    screenFilter.onDraw(textureId, quadVAO);
    textureMgr->release(&data);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_ideacarry_example16_GLRenderer_compare(JNIEnv *env, jclass clazz, jboolean press) {
    compare = press;
}