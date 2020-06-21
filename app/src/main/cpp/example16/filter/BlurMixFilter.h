//
// Created by Raining on 2020/6/11.
//

#ifndef OPENGL30JNI_BLURMIXFILTER_H
#define OPENGL30JNI_BLURMIXFILTER_H

#include "esUtils.h"

class BlurMixFilter {
public:
    BlurMixFilter();

    ~BlurMixFilter();

    void init(JNIEnv *env, jobject context);

    void onDraw(GLuint framebuffer, GLuint orgTexture, GLuint firstBlurTexture, GLuint secondBlurTexture, GLuint vao);

    void release();
private:
    esUtils::Shader *mShader;
};

#endif //OPENGL30JNI_BLURMIXFILTER_H
