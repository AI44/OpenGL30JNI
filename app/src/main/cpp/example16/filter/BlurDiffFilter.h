//
// Created by Raining on 2020/6/10.
//

#ifndef OPENGL30JNI_BLURDIFFFILTER_H
#define OPENGL30JNI_BLURDIFFFILTER_H


#include "esUtils.h"

class BlurDiffFilter {
public:
    BlurDiffFilter();

    ~BlurDiffFilter();

    void init(JNIEnv *env, jobject context);

    void onDraw(GLuint framebuffer, GLuint orgTexture, GLuint beautyTexture, GLuint vao);

    void release();
private:
    esUtils::Shader *mShader;
};

#endif //OPENGL30JNI_BLURDIFFFILTER_H
