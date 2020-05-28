//
// Created by Raining on 2019/8/14.
//

#ifndef OPENGLES30NDK_BITMAPFACTORYOPTIONS_H
#define OPENGLES30NDK_BITMAPFACTORYOPTIONS_H

#include <jni.h>
#include "Object.h"

class BitmapFactoryOptions : public Object
{
public:
    BitmapFactoryOptions(JNIEnv *env);

    ~BitmapFactoryOptions();

    jobject newObjectARGB8888();

protected:
    jclass getClass();
};

#endif //OPENGLES30NDK_BITMAPFACTORYOPTIONS_H
