//
// Created by Raining on 2020/6/10.
//

#include "ScreenFilter.h"

ScreenFilter::ScreenFilter() : mShader(nullptr) {

}

ScreenFilter::~ScreenFilter() {
    release();
}

void ScreenFilter::init(JNIEnv *env, jobject context) {
    if (mShader) {
        delete mShader;
        mShader = nullptr;
    }

    mShader = new esUtils::Shader(env, context, "example16/screen_vertex.glsl",
                                  "example16/screen_fragment.glsl");
}

void ScreenFilter::onDraw(GLuint texture, GLuint vao) {
    glBindFramebuffer(GL_FRAMEBUFFER, 0);

    glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
    glClear(GL_COLOR_BUFFER_BIT);

    mShader->use();
    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D, texture);
    glBindVertexArray(vao);
    glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);

    //clear
    glBindVertexArray(0);
    glBindTexture(GL_TEXTURE_2D, 0);
    glUseProgram(0);
}

void ScreenFilter::release() {
    if (mShader) {
        mShader->release();
        delete mShader;
        mShader = nullptr;
    }
}