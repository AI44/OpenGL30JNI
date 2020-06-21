//
// Created by Raining on 2020/6/10.
//

#ifndef OPENGL30JNI_SCREENFILTER_H
#define OPENGL30JNI_SCREENFILTER_H

#include "esUtils.h"

class ScreenFilter {
public:
    ScreenFilter();

    ~ScreenFilter();

    void init(JNIEnv *env, jobject context);

    void onDraw(GLuint texture, GLuint vao);

    void release();

private:
    esUtils::Shader *mShader;
};

#endif //OPENGL30JNI_SCREENFILTER_H
