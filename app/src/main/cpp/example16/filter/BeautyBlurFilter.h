//
// Created by Raining on 2020/6/10.
//

#ifndef OPENGL30JNI_BEAUTYBLURFILTER_H
#define OPENGL30JNI_BEAUTYBLURFILTER_H

#include "esUtils.h"

class BeautyBlurFilter {
public:
    BeautyBlurFilter();

    ~BeautyBlurFilter();

    void init(JNIEnv *env, jobject context);

    void onDraw(GLuint framebuffer, GLuint texture, float horizontalBlurSize, float verticalBlurSize, GLuint vao);

    void release();

    static int getSize(int size);

private:
    esUtils::Shader *mShader;
};

#endif //OPENGL30JNI_BEAUTYBLURFILTER_H
