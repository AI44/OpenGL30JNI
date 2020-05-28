//
// Created by Raining on 2019/8/14.
//

#ifndef OPENGLES30NDK_BITMAPFACTORY_H
#define OPENGLES30NDK_BITMAPFACTORY_H

#include <jni.h>
#include "Object.h"

class BitmapFactory : public Object
{
public:
    BitmapFactory(JNIEnv *env);

    ~BitmapFactory();

    jobject decodeStream(jobject inputStream, jobject outPadding, jobject opts);

protected:
    jclass getClass();
};

#endif //OPENGLES30NDK_BITMAPFACTORY_H
