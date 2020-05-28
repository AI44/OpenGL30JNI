//
// Created by Raining on 2019/8/9.
// #I# VBO + VAO + EBO
//

/*
 VBO，全称为Vertex Buffer Object，与FBO，PBO并称，但它实际上老不少。就某种意义来说，他就是VA(Vertex Array)的升级版。
 VBO出现的背景是人们发现VA和显示列表还有让人不满足的地方。一般，在OpenGL里，提高顶点绘制的办法：
　(1)显示列表：把常规的glBegin()-glEnd()中的代码放到一个显示列表中(通常在初始化阶段完成)，然后每遍渲染都调用这个显示列表。
　(2)VA：使用顶点数组，把顶点以及顶点属性数据作为数组，渲染的时候直接用一个或几个函数调动这些数组里的数据进行绘制，
     形式上是减少函数调用的次数(告别glVertex)，提高绘制效率。

 但是，这两种方法都有缺点。VA是在客户端设置的，所以执行这类函数(glDrawArray或glDrawElement)后，
 客户端还得把得到的顶点数据向服务端传输一次(所谓的“二次处理”)，这样一来就有了不必要的动作了，
 降低了效率——如果我们写的函数能直接把顶点数据发送给服务端就好了——这正是VBO的特性之一。显示列表的缺点在于它的古板，
 一旦设定就不容许修改，所以它只适合对一些“固定”的东西的绘制进行包装。(我们无办法直接在硬件层改顶点数据，因为这是脱离了流水线的事物)。
 而VBO直接把顶点数据交到流水线的第一步，与显示列表的效率还是有差距，但它这样就得到了操作数据的弹性——渲染阶段，
 我们的VBO绘制函数持续把顶点数据交给流水线，在某一刻我们可以把该帧到达了流水线的顶点数据取回客户端修改(Vertex mapping)，
 再提交回流水线(Vertex unmapping)，或者用glBufferData或glBufferSubData重新全部或buffer提交修改了的顶点数据，这是VBO的另一个特性。

 VBO结合了VA和显示列表这个说法不太妥当，应该说它结合了两者的一些特性，绘制效率在两者之间，且拥有良好的数据更改弹性。这种折衷造就了它一直为目前最高的地位。
 */

#include <GLES3/gl3.h>
#include <jni.h>
#include "esUtils.h"

namespace example3 {
    static const char *vertexShaderCode =
            "#version 300 es\n"
            "layout(location = 0) in vec3 aPos;"
            "out vec4 color;"
            "void main(){"
            "gl_Position = vec4(aPos.xyz, 1.0);"
            "color = vec4(0.0, 1.0, 0.0, 1.0);"
            "}";

    static const char *fragmentShaderCode =
            "#version 300 es\n"
            "precision mediump float;"
            "out vec4 fragColor;"
            "in vec4 color;"
            "void main(){"
            "fragColor = color;"
            "}";

    static float vertices[] = {
            0.5f, 0.5f, 0.0f,   // 右上角
            0.5f, -0.5f, 0.0f,  // 右下角
            -0.5f, -0.5f, 0.0f, // 左下角
            -0.5f, 0.5f, 0.0f   // 左上角
    };

    static unsigned int indices[] = { // 注意索引从0开始!
            0, 1, 3, // 第一个三角形
            1, 2, 3  // 第二个三角形
    };

    static GLuint program = 0;
    static GLuint vao;
}

using namespace example3;

extern "C"
JNIEXPORT void JNICALL
Java_com_ideacarry_example3_GLRenderer_surfaceCreated(JNIEnv *env, jobject thiz, jobject context, jint color) {
    color = 0xffff00ff;
    GLfloat alphaF = ((color >> 24) & 0xFF) * 1.0f / 255;
    GLfloat redF = ((color >> 16) & 0xFF) * 1.0f / 255;
    GLfloat greenF = ((color >> 8) & 0xFF) * 1.0f / 255;
    GLfloat blueF = (color & 0xFF) * 1.0f / 255;
    glClearColor(redF, greenF, blueF, alphaF);

    program = createProgram(vertexShaderCode, fragmentShaderCode);
    if (program) {
        GLuint vbo;
        glGenBuffers(1, &vbo);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, sizeof(vertices), vertices, GL_STATIC_DRAW);

        GLuint ebo;
        glGenBuffers(1, &ebo);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, sizeof(indices), indices, GL_STATIC_DRAW);

        //VAO 初始化部分
        glGenVertexArrays(1, &vao);
        glBindVertexArray(vao);

        //开始保存状态
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glVertexAttribPointer(0, 3, GL_FLOAT, GL_FALSE, 3 * sizeof(float), nullptr);
        glEnableVertexAttribArray(0);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);

        //保存结束
        glBindVertexArray(0);

        //glDeleteBuffers(1, &vbo);
        //glDeleteBuffers(1, &ebo);
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_ideacarry_example3_GLRenderer_surfaceChanged(JNIEnv *env, jobject thiz, jint width, jint height) {
    glViewport(0, 0, width, height);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_ideacarry_example3_GLRenderer_drawFrame(JNIEnv *env, jobject thiz) {
    //把颜色缓冲区设置为我们预设的颜色
    glClear(GL_COLOR_BUFFER_BIT);

    if (program) {
        glUseProgram(program);
        glBindVertexArray(vao);
        //glDrawElements(GL_LINE_LOOP, 6, GL_UNSIGNED_INT, 0);//画线
        glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);
        glBindVertexArray(0);
    }
}