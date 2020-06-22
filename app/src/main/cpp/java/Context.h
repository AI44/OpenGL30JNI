//
// Created by Raining on 2016/9/26.
//

#ifndef SECURITYJNILIBS2016_CONTEXT_H
#define SECURITYJNILIBS2016_CONTEXT_H

#include <jni.h>
#include "Object.h"

class Context : public Object
{
public:
	Context(JNIEnv *env);

	~Context();

	jobject getAssets(jobject instance);

	jobject getResources(jobject instance);

protected:
	jclass getClass();
};


#endif //SECURITYJNILIBS2016_CONTEXT_H
