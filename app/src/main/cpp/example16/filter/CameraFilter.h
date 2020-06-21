//
// Created by Raining on 2020/6/10.
//

#ifndef OPENGL30JNI_CAMERAFILTER_H
#define OPENGL30JNI_CAMERAFILTER_H

#include "esUtils.h"

class CameraFilter {
public:
    CameraFilter();

    ~CameraFilter();

    void init(JNIEnv *env, jobject context);

    void onDraw(GLuint framebuffer, GLuint texture, GLuint vao, float *matrix);

    void release();
private:
    esUtils::Shader *mShader;
};

#endif //OPENGL30JNI_CAMERAFILTER_H
