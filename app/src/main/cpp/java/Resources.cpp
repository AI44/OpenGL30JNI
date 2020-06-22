//
// Created by Raining on 2020/6/23.
//

#include "Resources.h"

Resources::Resources(JNIEnv *env) : Object(env) {}

Resources::~Resources() {}

jclass Resources::getClass() {
    if (!mCls) {
        mCls = mEnv->FindClass("android/content/res/Resources");
    }
    return mCls;
}

jobject Resources::openRawResource(jobject instance, jint id) {
    jmethodID openRawResourceMethodID = mEnv->GetMethodID(getClass(), "openRawResource", "(I)Ljava/io/InputStream;");
    jobject obj = mEnv->CallObjectMethod(instance, openRawResourceMethodID, id);
    if (mEnv->ExceptionCheck()) {
        //throw NotFoundException
        mEnv->ExceptionDescribe();
        mEnv->ExceptionClear();
        return nullptr;
    }
    return obj;
}