//
// Created by Raining on 2019/8/27.
// #I# lookAt + 欧拉角
//

#include <GLES3/gl3.h>
#include <jni.h>
#include "esUtils.h"
#include <cstdlib>
#include <ctime>
#include "image/ImageUtils.h"
#include <glm/glm.hpp>
#include <glm/gtc/matrix_transform.hpp>
#include <glm/gtc/type_ptr.hpp>

namespace example8 {
    static float vertices[] = {
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

    static glm::vec3 cubePositions[] = {
            glm::vec3(0.0f, 0.0f, 0.0f),
            glm::vec3(2.0f, 5.0f, -15.0f),
            glm::vec3(-1.5f, -2.2f, -2.5f),
            glm::vec3(-3.8f, -2.0f, -12.3f),
            glm::vec3(2.4f, -0.4f, -3.5f),
            glm::vec3(-1.7f, 3.0f, -7.5f),
            glm::vec3(1.3f, -2.0f, -2.5f),
            glm::vec3(1.5f, 2.0f, -2.5f),
            glm::vec3(1.5f, 0.2f, -1.5f),
            glm::vec3(-1.3f, 1.0f, -1.5f)
    };

    static GLuint program = 0;
    static GLuint vao = 0;
    static GLuint texture1 = 0;
    static GLuint texture2 = 0;

    static int sWidth = 1;
    static int sHeight = 1;

    static int sDirection = 0;
    static float lastTime = 0;
    static glm::vec3 cameraPos = glm::vec3(0.0f, 0.0f, 3.0f);
    static glm::vec3 cameraFront = glm::vec3(0.0f, 0.0f, -1.0f);
    static glm::vec3 cameraUp = glm::vec3(0.0f, 1.0f, 0.0f);
}

using namespace example8;

extern "C"
JNIEXPORT void JNICALL
Java_com_ideacarry_example8_GLRenderer_surfaceCreated(JNIEnv *env, jobject thiz, jobject context, jint color) {
    color = 0xffff8000;
    GLfloat alphaF = ((color >> 24) & 0xFF) * 1.0f / 255;
    GLfloat redF = ((color >> 16) & 0xFF) * 1.0f / 255;
    GLfloat greenF = ((color >> 8) & 0xFF) * 1.0f / 255;
    GLfloat blueF = (color & 0xFF) * 1.0f / 255;
    glClearColor(redF, greenF, blueF, alphaF);
    glEnable(GL_DEPTH_TEST);

    glGenTextures(1, &texture1);
    glBindTexture(GL_TEXTURE_2D, texture1);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    jobject bmp = readAssetImage(env, context, "wall.jpg");
    Image8888 img;
    img.SetImage(env, bmp);
    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, img.m_width, img.m_height, 0, GL_RGBA, GL_UNSIGNED_BYTE,
                 img.m_pDatas);
    glGenerateMipmap(GL_TEXTURE_2D);
    img.ClearAll();
    env->DeleteLocalRef(bmp);

    glGenTextures(1, &texture2);
    glBindTexture(GL_TEXTURE_2D, texture2);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    bmp = readAssetImage(env, context, "awesomeface.png");
    img.SetImage(env, bmp);
    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, img.m_width, img.m_height, 0, GL_RGBA, GL_UNSIGNED_BYTE,
                 img.m_pDatas);
    glGenerateMipmap(GL_TEXTURE_2D);
    img.ClearAll();
    env->DeleteLocalRef(bmp);

    char *vertexShaderCode = (char *) readAssetFile(env, context, "example8/vertex.glsl", true, nullptr);
    char *fragmentShaderCode = (char *) readAssetFile(env, context, "example8/fragment.glsl", true, nullptr);
    program = createProgram(vertexShaderCode, fragmentShaderCode);
    free(vertexShaderCode);
    free(fragmentShaderCode);

    if (program) {
        GLuint vbo;
        glGenBuffers(1, &vbo);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, sizeof(vertices), vertices, GL_STATIC_DRAW);

        glGenVertexArrays(1, &vao);
        glBindVertexArray(vao);

        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glVertexAttribPointer(0, 3, GL_FLOAT, GL_FALSE, 5 * sizeof(float), (void *) 0);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, 2, GL_FLOAT, GL_FALSE, 5 * sizeof(float),
                              (void *) (3 * sizeof(float)));
        glEnableVertexAttribArray(1);

        glBindVertexArray(0);

        glUseProgram(program);
        glUniform1i(glGetUniformLocation(program, "ourTexture1"), 0);
        glUniform1i(glGetUniformLocation(program, "ourTexture2"), 1);
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_ideacarry_example8_GLRenderer_surfaceChanged(JNIEnv *env, jobject thiz, jint width, jint height) {
    sWidth = width;
    sHeight = height;
    glViewport(0, 0, width, height);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_ideacarry_example8_GLRenderer_drawFrame(JNIEnv *env, jobject thiz) {
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

    if (program) {
        glUseProgram(program);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, texture1);

        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D, texture2);

        glBindVertexArray(vao);

        glm::mat4 projection;
        projection = glm::perspective(glm::radians(45.0f), (float) sWidth / (float) sHeight,
                                      0.1f, 100.0f);
        glUniformMatrix4fv(glGetUniformLocation(program, "projection"), 1, GL_FALSE,
                           glm::value_ptr(projection));

        timeval tv;
        gettimeofday(&tv, nullptr);
        float tme = tv.tv_sec - tv.tv_sec / 10000 * 10000 + tv.tv_usec / 1000000.0f;

        float deltaTime = tme - lastTime;
        lastTime = tme;

        glm::mat4 view = glm::mat4(1.0f);
        switch (sDirection) {
            case 1:
            case 2:
            case 3:
            case 4: {
                float cameraSpeed = 2.5f * deltaTime;

                switch (sDirection) {
                    case 1://左
                        cameraPos -=
                                glm::normalize(glm::cross(cameraFront, cameraUp)) * cameraSpeed;
                        break;
                    case 2://上
                        cameraPos += cameraSpeed * cameraFront;
                        break;
                    case 3://右
                        cameraPos +=
                                glm::normalize(glm::cross(cameraFront, cameraUp)) * cameraSpeed;
                        break;
                    case 4://下
                        cameraPos -= cameraSpeed * cameraFront;
                        break;
                }
                view = glm::lookAt(cameraPos, cameraPos + cameraFront, cameraUp);
                break;
            }

            default: {
                float radius = 10.0f;
                float camX = sin(tme) * radius;
                float camZ = cos(tme) * radius;

                float yaw = 45.0f;
                float pitch = 45.0f;
                glm::vec3 front;
                front.x = cos(glm::radians(yaw)) * cos(glm::radians(pitch));
                front.y = sin(glm::radians(pitch));
                front.z = sin(glm::radians(yaw)) * cos(glm::radians(pitch));
                front = glm::normalize(front);

                //摄像机位置、目标位置、世界空间中的上向量
                view = glm::lookAt(glm::vec3(camX, 0.0, camZ), glm::vec3(0.0, 0.0, 0.0) - front,
                                   glm::vec3(0.0, 1.0, 0.0));
                break;
            }

        }
        glUniformMatrix4fv(glGetUniformLocation(program, "view"), 1, GL_FALSE,
                           glm::value_ptr(view));

        for (int i = 0; i < 10; i++) {
            glm::mat4 model = glm::mat4(1.0f);
            model = glm::translate(model, cubePositions[i]);
            float angle = 20.0f * i;
            model = glm::rotate(model, tme * glm::radians(50.0f) + glm::radians(angle),
                                glm::vec3(0.5f, 1.0f, 0.0f));

            glUniformMatrix4fv(glGetUniformLocation(program, "model"), 1, GL_FALSE,
                               glm::value_ptr(model));

            glDrawArrays(GL_TRIANGLES, 0, 36);
        }
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_ideacarry_example8_DemoActivity_onDirection(JNIEnv *env, jclass clazz, jint d) {
    if (sDirection == d) {
        sDirection = 0;
    } else {
        sDirection = d;
    }
}