//
// Created by Raining on 2020/5/28.
// 渲染yuv数据
//

#include <jni.h>

extern "C"
JNIEXPORT void JNICALL
Java_com_ideacarry_example13_GLRenderer_surfaceCreated(JNIEnv *env, jobject thiz, jobject context, jint bg_color) {
// TODO: implement surfaceCreated()
}

extern "C"
JNIEXPORT void JNICALL
Java_com_ideacarry_example13_GLRenderer_surfaceChanged(JNIEnv *env, jobject thiz, jint width, jint height) {
// TODO: implement surfaceChanged()
}

extern "C"
JNIEXPORT void JNICALL
Java_com_ideacarry_example13_GLRenderer_drawFrame(JNIEnv *env, jobject thiz) {
// TODO: implement drawFrame()
}