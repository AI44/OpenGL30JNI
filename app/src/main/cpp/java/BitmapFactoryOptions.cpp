//
// Created by Raining on 2019/8/14.
//

#include "BitmapFactoryOptions.h"

BitmapFactoryOptions::BitmapFactoryOptions(JNIEnv *env) : Object(env) {}

BitmapFactoryOptions::~BitmapFactoryOptions() = default;

jclass BitmapFactoryOptions::getClass() {
    if (!mCls) {
        mCls = mEnv->FindClass("android/graphics/BitmapFactory$Options");
    }
    return mCls;
}

jobject BitmapFactoryOptions::newObjectARGB8888() {
    jmethodID id = mEnv->GetMethodID(getClass(), "<init>", "()V");
    jobject obj = mEnv->NewObject(getClass(), id);
    jfieldID opt_fieldID = mEnv->GetFieldID(getClass(), "inPreferredConfig", "Landroid/graphics/Bitmap$Config;");

    jclass cfg_cls = mEnv->FindClass("android/graphics/Bitmap$Config");
    jfieldID cfg_fieldID = mEnv->GetStaticFieldID(cfg_cls, "ARGB_8888", "Landroid/graphics/Bitmap$Config;");
    jobject argb8888 = mEnv->GetStaticObjectField(cfg_cls, cfg_fieldID);

    mEnv->SetObjectField(obj, opt_fieldID, argb8888);

    mEnv->DeleteLocalRef(argb8888);
    mEnv->DeleteLocalRef(cfg_cls);

    return obj;
}