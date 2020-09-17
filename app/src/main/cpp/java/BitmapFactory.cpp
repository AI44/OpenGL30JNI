//
// Created by Raining on 2019/8/14.
//

#include "BitmapFactory.h"

BitmapFactory::BitmapFactory(JNIEnv *env) : Object(env) {}

BitmapFactory::~BitmapFactory() {
    if (mCls) {
        mEnv->DeleteLocalRef(mCls);
        mCls = nullptr;
    }
}

jclass BitmapFactory::getClass() {
    if (!mCls) {
        mCls = mEnv->FindClass("android/graphics/BitmapFactory");
    }
    return mCls;
}

jobject BitmapFactory::decodeStream(jobject inputStream, jobject outPadding, jobject opts) {
    jmethodID id = mEnv->GetStaticMethodID(getClass(), "decodeStream",
                                           "(Ljava/io/InputStream;"
                                           "Landroid/graphics/Rect;"
                                           "Landroid/graphics/BitmapFactory$Options;)"
                                           "Landroid/graphics/Bitmap;");
    return mEnv->CallStaticObjectMethod(getClass(), id, inputStream, outPadding, opts);
}

jobject BitmapFactory::decodeFile(jstring pathName, jobject opts) {
    jmethodID id = mEnv->GetStaticMethodID(getClass(), "decodeFile",
                                           "(Ljava/lang/String;"
                                           "Landroid/graphics/BitmapFactory$Options;)"
                                           "Landroid/graphics/Bitmap;");
    return mEnv->CallStaticObjectMethod(getClass(), id, pathName, opts);
}