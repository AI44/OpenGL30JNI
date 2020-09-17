//
// Created by Raining on 2020/5/31.
// #I# framebuffer
//

#include <jni.h>
#include <GLES3/gl3.h>
#include <image/ImageUtils.h>
#include "esUtils.h"
#include "Camera.h"

namespace example15 {
    // set up vertex data (and buffer(s)) and configure vertex attributes
    // ------------------------------------------------------------------
    static float cubeVertices[] = {
            // positions          // texture Coords
            -0.5f, -0.5f, -0.5f, 0.0f, 0.0f,
            0.5f, -0.5f, -0.5f, 1.0f, 0.0f,
            0.5f, 0.5f, -0.5f, 1.0f, 1.0f,
            0.5f, 0.5f, -0.5f, 1.0f, 1.0f,
            -0.5f, 0.5f, -0.5f, 0.0f, 1.0f,
            -0.5f, -0.5f, -0.5f, 0.0f, 0.0f,

            -0.5f, -0.5f, 0.5f, 0.0f, 0.0f,
            0.5f, -0.5f, 0.5f, 1.0f, 0.0f,
            0.5f, 0.5f, 0.5f, 1.0f, 1.0f,
            0.5f, 0.5f, 0.5f, 1.0f, 1.0f,
            -0.5f, 0.5f, 0.5f, 0.0f, 1.0f,
            -0.5f, -0.5f, 0.5f, 0.0f, 0.0f,

            -0.5f, 0.5f, 0.5f, 1.0f, 0.0f,
            -0.5f, 0.5f, -0.5f, 1.0f, 1.0f,
            -0.5f, -0.5f, -0.5f, 0.0f, 1.0f,
            -0.5f, -0.5f, -0.5f, 0.0f, 1.0f,
            -0.5f, -0.5f, 0.5f, 0.0f, 0.0f,
            -0.5f, 0.5f, 0.5f, 1.0f, 0.0f,

            0.5f, 0.5f, 0.5f, 1.0f, 0.0f,
            0.5f, 0.5f, -0.5f, 1.0f, 1.0f,
            0.5f, -0.5f, -0.5f, 0.0f, 1.0f,
            0.5f, -0.5f, -0.5f, 0.0f, 1.0f,
            0.5f, -0.5f, 0.5f, 0.0f, 0.0f,
            0.5f, 0.5f, 0.5f, 1.0f, 0.0f,

            -0.5f, -0.5f, -0.5f, 0.0f, 1.0f,
            0.5f, -0.5f, -0.5f, 1.0f, 1.0f,
            0.5f, -0.5f, 0.5f, 1.0f, 0.0f,
            0.5f, -0.5f, 0.5f, 1.0f, 0.0f,
            -0.5f, -0.5f, 0.5f, 0.0f, 0.0f,
            -0.5f, -0.5f, -0.5f, 0.0f, 1.0f,

            -0.5f, 0.5f, -0.5f, 0.0f, 1.0f,
            0.5f, 0.5f, -0.5f, 1.0f, 1.0f,
            0.5f, 0.5f, 0.5f, 1.0f, 0.0f,
            0.5f, 0.5f, 0.5f, 1.0f, 0.0f,
            -0.5f, 0.5f, 0.5f, 0.0f, 0.0f,
            -0.5f, 0.5f, -0.5f, 0.0f, 1.0f
    };
    static float planeVertices[] = {
            // positions          // texture Coords
            5.0f, -0.5f, 5.0f, 2.0f, 0.0f,
            -5.0f, -0.5f, 5.0f, 0.0f, 0.0f,
            -5.0f, -0.5f, -5.0f, 0.0f, 2.0f,

            5.0f, -0.5f, 5.0f, 2.0f, 0.0f,
            -5.0f, -0.5f, -5.0f, 0.0f, 2.0f,
            5.0f, -0.5f, -5.0f, 2.0f, 2.0f
    };
    static float quadVertices[] = { // vertex attributes for a quad that fills the entire screen in Normalized Device Coordinates.
            // positions   // texCoords
            -1.0f, 1.0f, 0.0f, 1.0f,
            -1.0f, -1.0f, 0.0f, 0.0f,
            1.0f, -1.0f, 1.0f, 0.0f,

            -1.0f, 1.0f, 0.0f, 1.0f,
            1.0f, -1.0f, 1.0f, 0.0f,
            1.0f, 1.0f, 1.0f, 1.0f
    };

    static esUtils::Shader *shader = nullptr;
    static esUtils::Shader *screenShader = nullptr;

    static int sWidth = 0;
    static int sHeight = 0;

    static GLuint cubeVAO, cubeVBO;
    static GLuint planeVAO, planeVBO;
    static GLuint quadVAO, quadVBO;

    static GLuint cubeTexture;
    static GLuint floorTexture;

    static GLuint framebuffer;
    static GLuint textureColorBuffer;

    // camera
    static Camera camera(glm::vec3(0.0f, 0.0f, 3.0f));

    // utility function for loading a 2D texture from file
    // ---------------------------------------------------
    static unsigned int loadTexture(JNIEnv *env, jobject context, char const *path) {
        GLuint textureID;
        glGenTextures(1, &textureID);

        jobject bmp = readAssetImage(env, context, path);
        Image8888 img;
        img.SetImage(env, bmp);
        if (bmp) {
            glBindTexture(GL_TEXTURE_2D, textureID);
            glPixelStorei(GL_UNPACK_ALIGNMENT, 4);//4字节对齐
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, img.m_width, img.m_height, 0, GL_RGBA,
                         GL_UNSIGNED_BYTE, img.m_pDatas);
            glGenerateMipmap(GL_TEXTURE_2D);

            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

            img.ClearAll();
            env->DeleteLocalRef(bmp);
        }

        return textureID;
    }

    static void release() {
        // optional: de-allocate all resources once they've outlived their purpose:
        // ------------------------------------------------------------------------
        glDeleteVertexArrays(1, &cubeVAO);
        glDeleteVertexArrays(1, &planeVAO);
        glDeleteVertexArrays(1, &quadVAO);
        glDeleteBuffers(1, &cubeVBO);
        glDeleteBuffers(1, &planeVBO);
        glDeleteBuffers(1, &quadVBO);

        if (shader) {
            shader->release();
            delete shader;
            shader = nullptr;
        }
        if (screenShader) {
            screenShader->release();
            delete screenShader;
            screenShader = nullptr;
        }
    }
}

using namespace example15;

extern "C"
JNIEXPORT void JNICALL
Java_com_ideacarry_example15_GLRenderer_surfaceCreated(JNIEnv *env, jobject thiz, jobject context, jint bg_color) {

    // configure global opengl state
    // -----------------------------
    glEnable(GL_DEPTH_TEST);

    if (shader) {
        shader->release();
        delete shader;
        shader = nullptr;
    }
    shader = new esUtils::Shader(env, context, "example15/vertex.glsl",
                                 "example15/fragment.glsl");
    if (screenShader) {
        screenShader->release();
        delete screenShader;
        screenShader = nullptr;
    }
    //screenShader = new esUtils::Shader(env, context, "example15/screen_vertex.glsl",
    //                                   "example15/screen_fragment.glsl");
    //反相效果
    screenShader = new esUtils::Shader(env, context, "example15/screen_vertex.glsl",
                                       "example15/screen_inversion_fragment.glsl");
    //灰度效果
    //screenShader = new esUtils::Shader(env, context, "example15/screen_vertex.glsl",
    //                               "example15/screen_grayscale_fragment.glsl");
    //核效果
    //screenShader = new esUtils::Shader(env, context, "example15/screen_vertex.glsl",
    //                                   "example15/screen_kernel_fragment.glsl");
    //模糊效果
    //screenShader = new esUtils::Shader(env, context, "example15/screen_vertex.glsl",
    //                                   "example15/screen_blur_fragment.glsl");
    //边缘检测
    //screenShader = new esUtils::Shader(env, context, "example15/screen_vertex.glsl",
    //                                   "example15/screen_edge_detection_fragment.glsl");

    // cube VAO
    glGenVertexArrays(1, &cubeVAO);
    glGenBuffers(1, &cubeVBO);
    glBindVertexArray(cubeVAO);
    glBindBuffer(GL_ARRAY_BUFFER, cubeVBO);
    glBufferData(GL_ARRAY_BUFFER, sizeof(cubeVertices), cubeVertices, GL_STATIC_DRAW);
    glEnableVertexAttribArray(0);
    glVertexAttribPointer(0, 3, GL_FLOAT, GL_FALSE, 5 * sizeof(float), (void *) 0);
    glEnableVertexAttribArray(1);
    glVertexAttribPointer(1, 2, GL_FLOAT, GL_FALSE, 5 * sizeof(float),
                          (void *) (3 * sizeof(float)));
    // plane VAO
    glGenVertexArrays(1, &planeVAO);
    glGenBuffers(1, &planeVBO);
    glBindVertexArray(planeVAO);
    glBindBuffer(GL_ARRAY_BUFFER, planeVBO);
    glBufferData(GL_ARRAY_BUFFER, sizeof(planeVertices), planeVertices, GL_STATIC_DRAW);
    glEnableVertexAttribArray(0);
    glVertexAttribPointer(0, 3, GL_FLOAT, GL_FALSE, 5 * sizeof(float), (void *) 0);
    glEnableVertexAttribArray(1);
    glVertexAttribPointer(1, 2, GL_FLOAT, GL_FALSE, 5 * sizeof(float),
                          (void *) (3 * sizeof(float)));
    // screen quad VAO
    glGenVertexArrays(1, &quadVAO);
    glGenBuffers(1, &quadVBO);
    glBindVertexArray(quadVAO);
    glBindBuffer(GL_ARRAY_BUFFER, quadVBO);
    glBufferData(GL_ARRAY_BUFFER, sizeof(quadVertices), quadVertices, GL_STATIC_DRAW);
    glEnableVertexAttribArray(0);
    glVertexAttribPointer(0, 2, GL_FLOAT, GL_FALSE, 4 * sizeof(float), (void *) 0);
    glEnableVertexAttribArray(1);
    glVertexAttribPointer(1, 2, GL_FLOAT, GL_FALSE, 4 * sizeof(float),
                          (void *) (2 * sizeof(float)));

    // load textures
    // -------------
    cubeTexture = loadTexture(env, context, "container.jpg");
    floorTexture = loadTexture(env, context, "metal.png");

    // shader configuration
    // --------------------
    shader->use();
    shader->setInt("texture1", 0);

    screenShader->use();
    screenShader->setInt("screenTexture", 0);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_ideacarry_example15_GLRenderer_surfaceChanged(JNIEnv *env, jobject thiz, jint width, jint height) {
    sWidth = width;
    sHeight = height;
    glViewport(0, 0, width, height);

    //clear
    glDeleteFramebuffers(1, &framebuffer);
    glDeleteTextures(1, &textureColorBuffer);

    // framebuffer configuration
    // -------------------------
    glGenFramebuffers(1, &framebuffer);
    glBindFramebuffer(GL_FRAMEBUFFER, framebuffer);

    // create a color attachment texture
    glGenTextures(1, &textureColorBuffer);
    glBindTexture(GL_TEXTURE_2D, textureColorBuffer);
    glPixelStorei(GL_UNPACK_ALIGNMENT, 3);//3字节对齐
    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, sWidth, sHeight, 0, GL_RGB, GL_UNSIGNED_BYTE, nullptr);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, textureColorBuffer,
                           0);

    // create a renderbuffer object for depth and stencil attachment (we won't be sampling these)
    GLuint rbo;
    glGenRenderbuffers(1, &rbo);
    glBindRenderbuffer(GL_RENDERBUFFER, rbo);
    // use a single renderbuffer object for both a depth AND stencil buffer.
    glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH24_STENCIL8, sWidth, sHeight);
    // now actually attach it
    glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT, GL_RENDERBUFFER, rbo);
    // now that we actually created the framebuffer and added all attachments we want to check if it is actually complete now
    if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE)
        ALOGE ("ERROR::FRAMEBUFFER:: Framebuffer is not complete!");
    glBindFramebuffer(GL_FRAMEBUFFER, 0);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_ideacarry_example15_GLRenderer_drawFrame(JNIEnv *env, jobject thiz) {
    // render
    // ------
    // bind to framebuffer and draw scene as we normally would to color texture
    glBindFramebuffer(GL_FRAMEBUFFER, framebuffer);
    glEnable(GL_DEPTH_TEST); // enable depth testing (is disabled for rendering screen-space quad)

    // make sure we clear the framebuffer's content
    glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

    shader->use();
    glm::mat4 model = glm::mat4(1.0f);
    glm::mat4 view = camera.GetViewMatrix();
    glm::mat4 projection = glm::perspective(glm::radians(camera.Zoom),
                                            (float) sWidth / (float) sHeight,
                                            0.1f, 100.0f);
    shader->setMat4("view", view);
    shader->setMat4("projection", projection);
    // cubes
    glBindVertexArray(cubeVAO);
    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D, cubeTexture);
    model = glm::translate(model, glm::vec3(-1.0f, 0.0f, -1.0f));
    shader->setMat4("model", model);
    glDrawArrays(GL_TRIANGLES, 0, 36);
    model = glm::mat4(1.0f);
    model = glm::translate(model, glm::vec3(2.0f, 0.0f, 0.0f));
    shader->setMat4("model", model);
    glDrawArrays(GL_TRIANGLES, 0, 36);
    // floor
    glBindVertexArray(planeVAO);
    glBindTexture(GL_TEXTURE_2D, floorTexture);
    shader->setMat4("model", glm::mat4(1.0f));
    glDrawArrays(GL_TRIANGLES, 0, 6);
    glBindVertexArray(0);

    // now bind back to default framebuffer and draw a quad plane with the attached framebuffer color texture
    glBindFramebuffer(GL_FRAMEBUFFER, 0);
    // disable depth test so screen-space quad isn't discarded due to depth test.
    glDisable(GL_DEPTH_TEST);
    // clear all relevant buffers
    // set clear color to white (not really necessery actually, since we won't be able to see behind the quad anyways)
    glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
    glClear(GL_COLOR_BUFFER_BIT);

    screenShader->use();
    glBindVertexArray(quadVAO);
    // use the color attachment texture as the texture of the quad plane
    glBindTexture(GL_TEXTURE_2D, textureColorBuffer);
    glDrawArrays(GL_TRIANGLES, 0, 6);
    //glDrawArrays(GL_LINE_LOOP, 0, 6);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_ideacarry_example15_DemoActivity_onDirection(JNIEnv *env, jclass clazz, jint d) {
    switch (d) {
        case 1:
            camera.ProcessKeyboard(Camera_Movement::LEFT, 0.1f);
            break;
        case 2:
            camera.ProcessKeyboard(Camera_Movement::FORWARD, 0.1f);
            break;
        case 3:
            camera.ProcessKeyboard(Camera_Movement::RIGHT, 0.1f);
            break;
        case 4:
            camera.ProcessKeyboard(Camera_Movement::BACKWARD, 0.1f);
            break;
    }
}