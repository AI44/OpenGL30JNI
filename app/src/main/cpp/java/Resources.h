//
// Created by Raining on 2020/6/23.
//

#ifndef OPENGL30JNI_RESOURCES_H
#define OPENGL30JNI_RESOURCES_H

#include <jni.h>
#include "Object.h"

class Resources : public Object
{
public:
    Resources(JNIEnv *env);

    ~Resources();

    jobject openRawResource(jobject instance, jint id);

protected:
    jclass getClass();
};

#endif //OPENGL30JNI_RESOURCES_H
