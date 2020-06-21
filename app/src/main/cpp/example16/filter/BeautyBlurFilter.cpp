//
// Created by Raining on 2020/6/10.
//

#include "BeautyBlurFilter.h"

BeautyBlurFilter::BeautyBlurFilter() : mShader(nullptr) {
}

BeautyBlurFilter::~BeautyBlurFilter() {
    release();
}

void BeautyBlurFilter::init(JNIEnv *env, jobject context) {
    if (mShader) {
        delete mShader;
        mShader = nullptr;
    }

    mShader = new esUtils::Shader(env, context, "example16/beauty_blur_vertex.glsl",
                                  "example16/beauty_blur_fragment.glsl");
}

void BeautyBlurFilter::onDraw(GLuint framebuffer, GLuint texture, float horizontalBlurSize, float verticalBlurSize, GLuint vao) {
    glBindFramebuffer(GL_FRAMEBUFFER, framebuffer);

    glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
    glClear(GL_COLOR_BUFFER_BIT);

    mShader->use();
    glUniform1f(2, horizontalBlurSize);//texelWidthOffset
    glUniform1f(3, verticalBlurSize);//texelHeightOffset
    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D, texture);
    glBindVertexArray(vao);
    glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);

    //clear
    glBindVertexArray(0);
    glBindTexture(GL_TEXTURE_2D, 0);
    glUseProgram(0);
    //glBindFramebuffer(GL_FRAMEBUFFER, 0);
}

void BeautyBlurFilter::release() {
    if (mShader) {
        mShader->release();
        delete mShader;
        mShader = nullptr;
    }
}

int BeautyBlurFilter::getSize(int size) {
    return ((int) (size * 0.3f + 0.5f)) >> 1 << 1;
}