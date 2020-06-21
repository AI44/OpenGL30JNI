//
// Created by Raining on 2020/6/10.
//

#include "CameraFilter.h"
#include <GLES2/gl2ext.h>

CameraFilter::CameraFilter() : mShader(nullptr) {
}

CameraFilter::~CameraFilter() {
    release();
}

void CameraFilter::init(JNIEnv *env, jobject context) {
    if (mShader) {
        delete mShader;
        mShader = nullptr;
    }

    mShader = new esUtils::Shader(env, context, "example16/camera_vertex.glsl",
                                  "example16/camera_fragment.glsl");
}

void CameraFilter::onDraw(GLuint framebuffer, GLuint texture, GLuint vao, float *matrix) {
    //camera
    glBindFramebuffer(GL_FRAMEBUFFER, framebuffer);
    // make sure we clear the framebuffer's content
    glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
    glClear(GL_COLOR_BUFFER_BIT);

    mShader->use();
    //layout (location = 2)
    glUniformMatrix4fv(2, 1, GL_FALSE, matrix);

    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_EXTERNAL_OES, texture);

    glBindVertexArray(vao);
    glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);

    //clear
    glBindVertexArray(0);
    glBindTexture(GL_TEXTURE_EXTERNAL_OES, 0);
    glUseProgram(0);
    //glBindFramebuffer(GL_FRAMEBUFFER, 0);
}

void CameraFilter::release() {
    if (mShader) {
        mShader->release();
        delete mShader;
        mShader = nullptr;
    }
}