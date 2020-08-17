#ifndef GLES_ESUTIL_H
#define GLES_ESUTIL_H

#include <GLES3/gl3.h>
#include <android/log.h>
#include <jni.h>
#include "base_Log.h"

#define ALOGE base_LOG

#include <stdlib.h>
#include <glm/ext.hpp>

//检查当前程序错误
bool checkGlError(const char *funcName);

//获取并编译着色器对象
GLuint createShader(GLenum shaderType, const char *src);

//使用着色器生成着色器程序对象
GLuint createProgram(const char *vtxSrc, const char *fragSrc);

uint8_t *readAssetFile(JNIEnv *env, jobject context, const char *fileName, bool isString, int *len);

jobject readAssetImage(JNIEnv *env, jobject context, const char *fileName);

jobject readRawResImage(JNIEnv *env, jobject context, jint id);

jobject readPathImage(JNIEnv *env, const char *path);

GLuint loadAssetsTexture2D(JNIEnv *env, jobject context, char const *path);

GLuint loadAssetsTexture2D(JNIEnv *env, jobject context, char const *path, GLint textureWrapping);

GLuint loadAssetsTexture2D(JNIEnv *env, jobject bmp, GLint textureWrapping);

GLuint createRGBATexture2D(int w, int h);

namespace esUtils {

    enum CompileType {
        program,
        shader
    };

    class Shader {
    public:
        GLuint ID;

        Shader(JNIEnv *env, jobject context, const char *vertexAssetsPath, const char *fragmentAssetsPath);

        Shader(const char *vertexShaderCode, const char *fragmentShaderCode);

        // activate the shader
        // ------------------------------------------------------------------------
        void use();

        void release();

        // utility uniform functions
        // ------------------------------------------------------------------------
        void setBool(const char *name, bool value) const;

        // ------------------------------------------------------------------------
        void setInt(const char *name, int value) const;

        // ------------------------------------------------------------------------
        void setFloat(const char *name, float value) const;

        // ------------------------------------------------------------------------
        void setVec2(const char *name, const glm::vec2 &value) const;

        void setVec2(const char *name, float x, float y) const;

        // ------------------------------------------------------------------------
        void setVec3(const char *name, const glm::vec3 &value) const;

        void setVec3(const char *name, float x, float y, float z) const;

        // ------------------------------------------------------------------------
        void setVec4(const char *name, const glm::vec4 &value) const;

        void setVec4(const char *name, float x, float y, float z, float w) const;

        // ------------------------------------------------------------------------
        void setMat2(const char *name, const glm::mat2 &mat) const;

        // ------------------------------------------------------------------------
        void setMat3(const char *name, const glm::mat3 &mat) const;

        // ------------------------------------------------------------------------
        void setMat4(const char *name, const glm::mat4 &mat) const;

    private:
        // utility function for checking shader compilation/linking errors.
        // ------------------------------------------------------------------------
        void checkCompileErrors(GLuint shader, const CompileType type);
    };
}

#endif //GLES_ESUTIL_H
