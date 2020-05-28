//
// Created by Raining on 2019/8/14.
//

#include "BitmapFactory.h"

BitmapFactory::BitmapFactory(JNIEnv *env) : Object(env) {}

BitmapFactory::~BitmapFactory() = default;

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