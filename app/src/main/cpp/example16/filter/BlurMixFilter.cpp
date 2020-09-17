//
// Created by Raining on 2020/6/11.
//

#include "BlurMixFilter.h"

BlurMixFilter::BlurMixFilter() : mShader(nullptr) {
}

BlurMixFilter::~BlurMixFilter() {
    release();
}

void BlurMixFilter::init(JNIEnv *env, jobject context) {
    if (mShader) {
        delete mShader;
        mShader = nullptr;
    }

    mShader = new esUtils::Shader(env, context, "example16/blur_mix_vertex.glsl",
                                  "example16/blur_mix_fragment.glsl");

    mShader->use();
    mShader->setInt("orgTexture", 2);
    mShader->setInt("firstBlurTexture", 3);
    mShader->setInt("secondBlurTexture", 4);
    mShader->setFloat("blurAlpha", 1.0f);//磨皮程度(0-1.0f)
}

void BlurMixFilter::onDraw(GLuint framebuffer, GLuint orgTexture, GLuint firstBlurTexture, GLuint secondBlurTexture, GLuint vao) {
    glBindFramebuffer(GL_FRAMEBUFFER, framebuffer);

    glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
    glClear(GL_COLOR_BUFFER_BIT);

    mShader->use();

    glActiveTexture(GL_TEXTURE2);
    glBindTexture(GL_TEXTURE_2D, orgTexture);
    glActiveTexture(GL_TEXTURE3);
    glBindTexture(GL_TEXTURE_2D, firstBlurTexture);
    glActiveTexture(GL_TEXTURE4);
    glBindTexture(GL_TEXTURE_2D, secondBlurTexture);
    glBindVertexArray(vao);
    glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);

    //clear
    glBindVertexArray(0);
    glBindTexture(GL_TEXTURE_2D, 0);
    glUseProgram(0);
    //glBindFramebuffer(GL_FRAMEBUFFER, 0);
}

void BlurMixFilter::release() {
    if (mShader) {
        mShader->release();
        delete mShader;
        mShader = nullptr;
    }
}