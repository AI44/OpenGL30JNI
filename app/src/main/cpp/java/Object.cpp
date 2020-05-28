//
// Created by Raining on 2016/9/27.
//

#include "Object.h"
#include <iostream>

Object::Object(JNIEnv *env) : mEnv(env), mCls(NULL)
{
}

Object::~Object()
{
	if(mCls)
	{
		mEnv->DeleteLocalRef(mCls);
		mCls = NULL;
	}
	mEnv = NULL;
}

jclass Object::getClass()
{
	if(!mCls)
	{
		mCls = mEnv->FindClass("java/lang/Object");
	}
	return mCls;
}

jobject Object::getJavaClass(jobject instance)
{
	jmethodID id = mEnv->GetMethodID(getClass(), "getClass", "()Ljava/lang/Class;");
	return mEnv->CallObjectMethod(instance, id);
}