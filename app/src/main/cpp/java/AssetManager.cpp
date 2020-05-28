//
// Created by Raining on 2019/8/14.
//

#include "AssetManager.h"

AssetManager::AssetManager(JNIEnv *env) : Object(env) {}

AssetManager::~AssetManager() {}

jclass AssetManager::getClass() {
    if (!mCls) {
        mCls = mEnv->FindClass("android/content/res/AssetManager");
    }
    return mCls;
}

jobject AssetManager::open(jobject instance, jstring fileName) {
    jmethodID id = mEnv->GetMethodID(getClass(), "open", "(Ljava/lang/String;)Ljava/io/InputStream;");
    jobject obj = mEnv->CallObjectMethod(instance, id, fileName);
    if (mEnv->ExceptionCheck()) {
        //throw IOException
        mEnv->ExceptionDescribe();
        mEnv->ExceptionClear();
        return nullptr;
    }
    return obj;
}