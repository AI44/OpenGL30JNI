#ifndef GLES_ESUTIL_H
#define GLES_ESUTIL_H

#include <GLES3/gl3.h>
#include <android/log.h>
#include <jni.h>
#include "base_Log.h"

#define ALOGE base_LOG

#include <stdlib.h>

//检查当前程序错误
bool checkGlError(const char* funcName);
//获取并编译着色器对象
GLuint createShader(GLenum shaderType, const char* src);
//使用着色器生成着色器程序对象
GLuint createProgram(const char* vtxSrc, const char* fragSrc);

uint8_t *readAssetFile(JNIEnv *env, jobject context, const char *fileName, bool isString, int *len);

jobject readAssetImage(JNIEnv *env, jobject context, const char *fileName);

#endif //GLES_ESUTIL_H
