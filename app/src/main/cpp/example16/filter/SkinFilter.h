//
// Created by Raining on 2020/6/10.
//

#ifndef OPENGL30JNI_SKINFILTER_H
#define OPENGL30JNI_SKINFILTER_H

#include "esUtils.h"

class SkinFilter {
public:
    SkinFilter();

    ~SkinFilter();

    void init(JNIEnv *env, jobject context);

    void onDraw(GLuint framebuffer, GLuint texture, GLuint vao);

    void release();

private:
    esUtils::Shader *mShader;
    GLuint mGrayTextureId;
    GLuint mBeautyLUTTextureId;
};

#endif //OPENGL30JNI_SKINFILTER_H
