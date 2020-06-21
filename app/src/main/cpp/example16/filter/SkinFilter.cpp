//
// Created by Raining on 2020/6/10.
//

#include "SkinFilter.h"

SkinFilter::SkinFilter() : mShader(nullptr), mGrayTextureId(-1), mBeautyLUTTextureId(-1) {
}

SkinFilter::~SkinFilter() {
    release();
}

void SkinFilter::init(JNIEnv *env, jobject context) {
    if (mShader) {
        delete mShader;
        mShader = nullptr;
    }

    mShader = new esUtils::Shader(env, context, "example16/skin_beauty_vertex.glsl",
                                  "example16/skin_beauty_fragment.glsl");

    glDeleteTextures(1, &mGrayTextureId);
    glDeleteTextures(1, &mBeautyLUTTextureId);
    mGrayTextureId = loadAssetsTexture2D(env, context, "example16/gray_filter.png");
    mBeautyLUTTextureId = loadAssetsTexture2D(env, context, "example16/beauty_filter.png");

    mShader->use();
    glUniform1i(2, 2);//inputImageTexture
    glUniform1i(3, 3);//grayTexture
    glUniform1i(4, 4);//lookupTexture
    glUniform1f(5, 1.040816f);//levelRangeInv
    glUniform1f(6, 0.01960784f);//levelBlack
    glUniform1f(7, 0.5f);//alpha(0.0f - 0.5f)
}

void SkinFilter::onDraw(GLuint framebuffer, GLuint texture, GLuint vao) {
    //美白
    glBindFramebuffer(GL_FRAMEBUFFER, framebuffer);

    glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
    glClear(GL_COLOR_BUFFER_BIT);

    mShader->use();
    glActiveTexture(GL_TEXTURE2);
    glBindTexture(GL_TEXTURE_2D, texture);
    glActiveTexture(GL_TEXTURE3);
    glBindTexture(GL_TEXTURE_2D, mGrayTextureId);
    glActiveTexture(GL_TEXTURE4);
    glBindTexture(GL_TEXTURE_2D, mBeautyLUTTextureId);
    glBindVertexArray(vao);
    glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);

    //clear
    glBindVertexArray(0);
    glBindTexture(GL_TEXTURE_2D, 0);
    glUseProgram(0);
    //glBindFramebuffer(GL_FRAMEBUFFER, 0);
}

void SkinFilter::release() {
    if (mShader) {
        mShader->release();
        delete mShader;
        mShader = nullptr;

        glDeleteTextures(1, &mGrayTextureId);
        mGrayTextureId = -1;
        glDeleteTextures(1, &mBeautyLUTTextureId);
        mBeautyLUTTextureId = -1;
    }
}