//
// Created by Raining on 2019/8/9.
// #I# VAO使用

/*
 VBO将顶点信息放到GPU中，GPU在渲染时去缓存中取数据，二者中间的桥梁是GL-Context。GL-Context整个程序一般只有一个，
 所以如果一个渲染流程里有两份不同的绘制代码，GL-context就负责在他们之间进行切换。这也是为什么要在渲染过程中，
 在每份绘制代码之中会有glBindbuffer、glEnableVertexAttribArray、glVertexAttribPointer。那么优化的方法来了，
 把这些都放到初始化时候完成吧！VAO记录该次绘制所需要的所有VBO所需信息，把它保存到VBO特定位置，绘制的时候直接在这个位置取信息绘制。

 VAO的全名是Vertex Array Object，首先，它不是Buffer-Object，所以不用作存储数据；其次，它针对"顶点"而言，
 也就是说它跟"顶点的绘制"息息相关。(VAO和VA没有任何关系)

 VAO记录的是一次绘制中所需要的信息，这包括"数据在哪里glBindBuffer"、"数据的格式是怎么样的glVertexAttribPointer"、
 shader-attribute的location的启用glEnableVertexAttribArray。
 */

#include <GLES3/gl3.h>
#include <jni.h>
#include "esUtils.h"

namespace example2 {
    static const char *vertexShaderCode =
            "#version 300 es\n"
            "layout(location = 0) in vec3 aPos;"
            "out vec4 color;"
            "void main(){"
            "gl_Position = vec4(aPos.x, aPos.y, aPos.z, 1.0);"
            "color = vec4(0.5, 0.0, 0.0, 1.0);"
            "}";

    static const char *fragmentShaderCode =
            "#version 300 es\n"
            "precision mediump float;"
            "out vec4 fragColor;"
            "in vec4 color;"
            "void main(){"
            "fragColor = color;"
            "}";

    static const GLfloat vertices[] = {
            -0.5f, -0.5f, 0.0f,
            0.5f, -0.5f, 0.0f,
            0.0f, 0.5f, 0.0f};

    static GLuint program = 0;
    static GLuint vao;

    static void surfaceCreated(JNIEnv *env, jobject thiz, jobject context, jint color) {
        color = 0xffff8000;
        GLfloat alphaF = ((color >> 24) & 0xFF) * 1.0f / 255;
        GLfloat redF = ((color >> 16) & 0xFF) * 1.0f / 255;
        GLfloat greenF = ((color >> 8) & 0xFF) * 1.0f / 255;
        GLfloat blueF = (color & 0xFF) * 1.0f / 255;
        glClearColor(redF, greenF, blueF, alphaF);

        program = createProgram(vertexShaderCode, fragmentShaderCode);
        if (program) {
            glGenVertexArrays(1, &vao);
            // 1. 绑定VAO
            glBindVertexArray(vao);
            // 2. 把顶点数组复制到缓冲中供OpenGL使用
            glBindBuffer(GL_ARRAY_BUFFER, vao);
            glBufferData(GL_ARRAY_BUFFER, sizeof(vertices), vertices, GL_STATIC_DRAW);
            // 3. 设置顶点属性指针
            glVertexAttribPointer(0, 3, GL_FLOAT, GL_FALSE, 3 * sizeof(float), nullptr);
            glEnableVertexAttribArray(0);
            //解绑
            glBindVertexArray(0);
        }
    }

    static void surfaceChanged(JNIEnv *env, jobject thiz, jint width, jint height) {
        glViewport(0, 0, width, height);
    }

    static void drawFrame(JNIEnv *env, jobject thiz) {
        //把颜色缓冲区设置为我们预设的颜色
        glClear(GL_COLOR_BUFFER_BIT);

        if (program) {
            // 4. 绘制物体
            glUseProgram(program);
            glBindVertexArray(vao);
            glDrawArrays(GL_TRIANGLES, 0, 3);
        }
    }

    /**
    * 动态注册
    */
    static JNINativeMethod methods[] = {
            {"surfaceCreated", "(Landroid/content/Context;I)V", (void *) surfaceCreated},
            {"surfaceChanged", "(II)V",                         (void *) surfaceChanged},
            {"drawFrame",      "()V",                           (void *) drawFrame}
    };

    /**
     * 动态注册
     */
    jint registerNativeMethod(JNIEnv *env) {
        jclass cl = env->FindClass("com/ideacarry/example2/GLRenderer");
        if ((env->RegisterNatives(cl, methods, sizeof(methods) / sizeof(methods[0]))) < 0) {
            return -1;
        }
        //env->UnregisterNatives(cl);
        return 0;
    }
}