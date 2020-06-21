//
// Created by Raining on 2020/6/10.
//

#include "BlurDiffFilter.h"
#include <GLES2/gl2ext.h>

BlurDiffFilter::BlurDiffFilter() : mShader(nullptr) {
}

BlurDiffFilter::~BlurDiffFilter() {
    release();
}

void BlurDiffFilter::init(JNIEnv *env, jobject context) {
    if (mShader) {
        delete mShader;
        mShader = nullptr;
    }

    mShader = new esUtils::Shader(env, context, "example16/blur_diff_vertex.glsl",
                                  "example16/blur_diff_fragment.glsl");

    mShader->use();
    glUniform1i(2, 2);//blurTexture
    glUniform1i(3, 3);//orgTexture
}

void BlurDiffFilter::onDraw(GLuint framebuffer, GLuint orgTexture, GLuint beautyTexture, GLuint vao) {
    glBindFramebuffer(GL_FRAMEBUFFER, framebuffer);

    glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
    glClear(GL_COLOR_BUFFER_BIT);

    mShader->use();

    glActiveTexture(GL_TEXTURE2);
    glBindTexture(GL_TEXTURE_2D, beautyTexture);
    glActiveTexture(GL_TEXTURE3);
    glBindTexture(GL_TEXTURE_2D, orgTexture);
    glBindVertexArray(vao);
    glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);

    //clear
    glBindVertexArray(0);
    glBindTexture(GL_TEXTURE_2D, 0);
    glUseProgram(0);
    //glBindFramebuffer(GL_FRAMEBUFFER, 0);
}

void BlurDiffFilter::release() {
    if (mShader) {
        mShader->release();
        delete mShader;
        mShader = nullptr;
    }
}