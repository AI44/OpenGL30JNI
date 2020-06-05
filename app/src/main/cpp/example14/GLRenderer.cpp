//
// Created by Raining on 2020/5/29.
// CameraX+SurfaceTexture+SurfaceView
//

#include <GLES3/gl3.h>
#include <GLES2/gl2ext.h>
#include <jni.h>
#include "esUtils.h"

namespace example14 {
    static float quadVertices[] = { // vertex attributes for a quad that fills the entire screen in Normalized Device Coordinates.
            // positions   // texCoords
            -1.0f, 1.0f, 0.0f, 1.0f,
            -1.0f, -1.0f, 0.0f, 0.0f,
            1.0f, -1.0f, 1.0f, 0.0f,

            -1.0f, 1.0f, 0.0f, 1.0f,
            1.0f, -1.0f, 1.0f, 0.0f,
            1.0f, 1.0f, 1.0f, 1.0f
    };

    static esUtils::Shader *screenShader = nullptr;

    static int sWidth = 0;
    static int sHeight = 0;

    static GLuint quadVAO, quadVBO;
}

using namespace example14;

extern "C"
JNIEXPORT void JNICALL
Java_com_ideacarry_example14_GLRenderer_deleteTextureObject(JNIEnv *env, jclass clazz, jint texture_id) {
    glDeleteTextures(1, reinterpret_cast<const GLuint *>(&texture_id));
    checkGlError("glDeleteTextures");
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_ideacarry_example14_GLRenderer_createTextureObject(JNIEnv *env, jclass clazz) {
    GLuint texture;
    glGenTextures(1, &texture);
    checkGlError("glGenTextures");
    glBindTexture(GL_TEXTURE_EXTERNAL_OES, texture);
    checkGlError("loadImageTexture");
    glTexParameterf(GL_TEXTURE_EXTERNAL_OES, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    glTexParameterf(GL_TEXTURE_EXTERNAL_OES, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_EXTERNAL_OES, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
    glTexParameteri(GL_TEXTURE_EXTERNAL_OES, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
    //glBindTexture(GL_TEXTURE_EXTERNAL_OES, 0);
    return texture;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_ideacarry_example14_GLRenderer_surfaceCreated(JNIEnv *env, jclass clazz, jobject context) {

    glDisable(GL_DEPTH_TEST);

    if (screenShader) {
        screenShader->release();
        screenShader = nullptr;
    }
    screenShader = new esUtils::Shader(env, context, "example14/screen_vertex.glsl",
                                       "example14/screen_fragment.glsl");

    // screen quad VAO
    glGenVertexArrays(1, &quadVAO);
    glGenBuffers(1, &quadVBO);
    glBindVertexArray(quadVAO);
    glBindBuffer(GL_ARRAY_BUFFER, quadVBO);
    glBufferData(GL_ARRAY_BUFFER, sizeof(quadVertices), &quadVertices, GL_STATIC_DRAW);
    glEnableVertexAttribArray(0);
    glVertexAttribPointer(0, 2, GL_FLOAT, GL_FALSE, 4 * sizeof(float), (void *) 0);
    glEnableVertexAttribArray(1);
    glVertexAttribPointer(1, 2, GL_FLOAT, GL_FALSE, 4 * sizeof(float),
                          (void *) (2 * sizeof(float)));
}

extern "C"
JNIEXPORT void JNICALL
Java_com_ideacarry_example14_GLRenderer_surfaceChanged(JNIEnv *env, jclass clazz, jint width, jint height) {
    sWidth = width;
    sHeight = height;
    glViewport(0, 0, width, height);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_ideacarry_example14_GLRenderer_drawFrame(JNIEnv *env, jclass clazz, jint texture_id, jfloatArray matrix) {
    glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
    glClear(GL_COLOR_BUFFER_BIT);

    screenShader->use();
    jfloat* mat = env->GetFloatArrayElements(matrix, 0);
    //layout (location = 2)
    glUniformMatrix4fv(2, 1, GL_FALSE, mat);
    env->ReleaseFloatArrayElements(matrix, mat, 0);
    glActiveTexture(GL_TEXTURE0);
    glBindVertexArray(quadVAO);
    // use the color attachment texture as the texture of the quad plane
    glBindTexture(GL_TEXTURE_EXTERNAL_OES, texture_id);
    checkGlError("glBindTexture");
    glDrawArrays(GL_TRIANGLES, 0, 6);

    glBindVertexArray(0);
    glBindTexture(GL_TEXTURE_EXTERNAL_OES, 0);
    glUseProgram(0);
}