//
// Created by Raining on 2016/9/27.
//

#ifndef SECURITYJNILIBS2016_OBJECT_H
#define SECURITYJNILIBS2016_OBJECT_H

#include <jni.h>

class Object
{
public:
	Object(JNIEnv *env);

	virtual ~Object();

	jobject getJavaClass(jobject instance);

protected:
	JNIEnv *mEnv;
	jclass mCls;

	virtual jclass getClass();
};

#endif //SECURITYJNILIBS2016_OBJECT_H
