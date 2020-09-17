//
// Created by Raining on 2016/9/26.
//

#include "Context.h"

Context::Context(JNIEnv *env) : Object(env)
{
}

Context::~Context()
{
	if (mCls) {
		mEnv->DeleteLocalRef(mCls);
		mCls = nullptr;
	}
}

jclass Context::getClass()
{
	if(!mCls)
	{
		mCls = mEnv->FindClass("android/content/Context");
	}
	return mCls;
}

jobject Context::getAssets(jobject instance)
{
	jmethodID id = mEnv->GetMethodID(getClass(), "getAssets", "()Landroid/content/res/AssetManager;");
	return mEnv->CallObjectMethod(instance, id);
}

jobject Context::getResources(jobject instance) {
	jmethodID id = mEnv->GetMethodID(getClass(), "getResources", "()Landroid/content/res/Resources;");
	return mEnv->CallObjectMethod(instance, id);
}