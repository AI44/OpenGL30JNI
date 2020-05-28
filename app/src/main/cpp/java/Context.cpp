//
// Created by Raining on 2016/9/26.
//

#include "Context.h"

Context::Context(JNIEnv *env) : Object(env)
{
}

Context::~Context()
{
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