//
// Created by Raining on 2019/9/10.
// #I# freetype加载文字
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
#include "base_Log.h"
#include <map>
#include <string>
#include <ft2build.h>
#include FT_FREETYPE_H

namespace example11 {
    // Holds all state information relevant to a character as loaded using FreeType
    struct Character {
        GLuint textureID;   // ID handle of the glyph texture
        glm::ivec2 size;    // Size of glyph
        glm::ivec2 bearing;  // Offset from baseline to left/top of glyph
        GLuint advance;    // Horizontal offset to advance to next glyph
    };

    static std::map<GLchar, Character> sCharacters;
    static bool sInit = false;

    static void initFont(JNIEnv *env, jobject context) {
        if (!sInit) {

            FT_Library ft;
            if (FT_Init_FreeType(&ft)) {
                base_LOG("ERROR::FREETYPE: Could not init FreeType Library");
                return;
            }

            int len = 0;
            uint8_t *fontData = readAssetFile(env, context, "font.ttf", false, &len);

            FT_Face face;
            if (FT_New_Memory_Face(ft, fontData, len, 0, &face)) {
                base_LOG("ERROR::FREETYPE: Failed to load font");
                return;
            }
            FT_Set_Pixel_Sizes(face, 0, 128);

            // Disable byte-alignment restriction
            glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

            // Load first 128 characters of ASCII set
            for (GLubyte c = 0; c < 128; c++) {
                // Load character glyph
                if (FT_Load_Char(face, c, FT_LOAD_RENDER)) {
                    base_LOG("ERROR::FREETYTPE: Failed to load Glyph");
                    continue;
                }

                // Generate texture
                GLuint texture;
                glGenTextures(1, &texture);
                glBindTexture(GL_TEXTURE_2D, texture);
                glTexImage2D(
                        GL_TEXTURE_2D,
                        0,
                        GL_R8,
                        face->glyph->bitmap.width,
                        face->glyph->bitmap.rows,
                        0,
                        GL_RED,
                        GL_UNSIGNED_BYTE,
                        face->glyph->bitmap.buffer
                );
                // Set texture options
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
                // Now store character for later use
                Character character = {
                        texture,
                        glm::ivec2(face->glyph->bitmap.width, face->glyph->bitmap.rows),
                        glm::ivec2(face->glyph->bitmap_left, face->glyph->bitmap_top),
                        (GLuint) face->glyph->advance.x
                };
                sCharacters.insert(std::pair<GLchar, Character>(c, character));
            }
            glBindTexture(GL_TEXTURE_2D, 0);
            // Destroy FreeType once we're finished
            FT_Done_Face(face);
            FT_Done_FreeType(ft);
            free(fontData);

            sInit = true;
        }
    }

    static GLuint program = 0;
    static GLuint vao = 0;
    static GLuint vbo = 0;

    static bool sizeChange = true;
    static int sWidth = 1;
    static int sHeight = 1;

    void RenderText(std::string text, GLfloat x, GLfloat y, GLfloat scale, glm::vec3 color) {
        // Activate corresponding render state
        glUseProgram(program);
        glUniform3f(glGetUniformLocation(program, "textColor"), color.x, color.y, color.z);
        glActiveTexture(GL_TEXTURE0);
        glBindVertexArray(vao);

        // Iterate through all characters
        std::string::const_iterator c;
        for (c = text.begin(); c != text.end(); c++) {

            if (sCharacters.count(*c) > 0) {

                Character ch = sCharacters[*c];

                GLfloat xpos = x + ch.bearing.x * scale;
                GLfloat ypos = y - (ch.size.y - ch.bearing.y) * scale;

                GLfloat w = ch.size.x * scale;
                GLfloat h = ch.size.y * scale;
                // Update VBO for each character
                GLfloat vertices[6][4] = {
                        {xpos,     ypos + h, 0.0f, 0.0f},
                        {xpos,     ypos,     0.0f, 1.0f},
                        {xpos + w, ypos,     1.0f, 1.0f},

                        {xpos,     ypos + h, 0.0f, 0.0f},
                        {xpos + w, ypos,     1.0f, 1.0f},
                        {xpos + w, ypos + h, 1.0f, 0.0f}
                };

                //#################debug#################
//            static bool debug = false;
//            if (!debug) {
//                glm::mat4 projection = glm::ortho(0.0f, static_cast<GLfloat>(sWidth), 0.0f,
//                                                  static_cast<GLfloat>(sHeight));
//                glm::vec4 v(xpos, ypos + h, 0.0f, 0.0f);
//                v = projection * v;
//                base_LOG("%f,%f,%f,%f", v.x, v.y, v.z, v.w);
//                debug = true;
//            }
                //#############end debug#################

                // Render glyph texture over quad
                glBindTexture(GL_TEXTURE_2D, ch.textureID);
                // Update content of VBO memory
                glBindBuffer(GL_ARRAY_BUFFER, vbo);
                glBufferSubData(GL_ARRAY_BUFFER, 0, sizeof(vertices),
                                vertices); // Be sure to use glBufferSubData and not glBufferData

                glBindBuffer(GL_ARRAY_BUFFER, 0);
                // Render quad
                glDrawArrays(GL_TRIANGLES, 0, 6);
                // Now advance cursors for next glyph (note that advance is number of 1/64 pixels)
                x += (ch.advance >> 6) *
                     scale; // Bitshift by 6 to get value in pixels (2^6 = 64 (divide amount of 1/64th pixels by 64 to get amount of pixels))
            }
        }
        glBindVertexArray(0);
        glBindTexture(GL_TEXTURE_2D, 0);
    }
}

using namespace example11;

//删除缓存数据
static void clearData() {
    std::map<GLchar, Character>::iterator iter;
    iter = sCharacters.begin();
    while (iter != sCharacters.end()) {
        //base_LOG("%c", iter->first);
        glDeleteTextures(1, &(iter->second.textureID));
        iter++;
    }
    sInit = false;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_ideacarry_example11_GLRenderer_surfaceCreated(JNIEnv *env, jobject thiz, jobject context, jint bg_color) {
    clearData();

    glClearColor(0.2f, 0.3f, 0.3f, 1.0f);
    //开启剔除操作效果
    glEnable(GL_CULL_FACE);
    //启用混合
    glEnable(GL_BLEND);
    //混合函数
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

    initFont(env, context);

    char *vertexShaderCode = (char *) readAssetFile(env, context, "example11/vertex.glsl", true, nullptr);
    char *fragmentShaderCode = (char *) readAssetFile(env, context, "example11/fragment.glsl", true,
                                                      nullptr);
    program = createProgram(vertexShaderCode, fragmentShaderCode);
    free(vertexShaderCode);
    free(fragmentShaderCode);

    if (program) {
        // Configure VAO/VBO for texture quads
        glGenVertexArrays(1, &vao);
        glGenBuffers(1, &vbo);
        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, sizeof(GLfloat) * 6 * 4, NULL, GL_DYNAMIC_DRAW);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 4, GL_FLOAT, GL_FALSE, 4 * sizeof(GLfloat), 0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_ideacarry_example11_GLRenderer_surfaceChanged(JNIEnv *env, jobject thiz, jint width, jint height) {
    sWidth = width;
    sHeight = height;
    glViewport(0, 0, width, height);
    sizeChange = true;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_ideacarry_example11_GLRenderer_drawFrame(JNIEnv *env, jobject thiz) {
    glClear(GL_COLOR_BUFFER_BIT);

    if (program) {
        if (sizeChange) {
            glUseProgram(program);
//            glm::mat4 projection = glm::ortho((float)-sWidth, static_cast<GLfloat>(sWidth), (float)-sHeight,
//                                              static_cast<GLfloat>(sHeight));
            glm::mat4 projection = glm::ortho(0.0f, static_cast<GLfloat>(sWidth), 0.0f,
                                              static_cast<GLfloat>(sHeight));
            glUniformMatrix4fv(glGetUniformLocation(program, "projection"), 1, GL_FALSE,
                               glm::value_ptr(projection));
            sizeChange = false;
        }

        RenderText("This is sample text", 25.0f, 25.0f, 1.0f, glm::vec3(0.5, 0.8f, 0.2f));
        RenderText("(C) LearnOpenGL.com", 540.0f, 570.0f, 0.5f, glm::vec3(0.3, 0.7f, 0.9f));
    }
}