/*
 *  Created on: 2013-3-10
 *      Author: Raining
 */

#ifndef _BASE_LOG
#define _BASE_LOG

#ifdef __cplusplus
extern "C"
{
#endif

#include <android/log.h>

#define LOG_TAG "OpenGL_log"

#define base_LOG(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

void printMatrix4x4(const float *mat);

void printMatrix3x3(const float *mat);

#ifdef __cplusplus
}
#endif

#endif
