//
// Created by Raining on 2019/8/14.
//

#ifndef OPENGLES30NDK_ASSETMANAGER_H
#define OPENGLES30NDK_ASSETMANAGER_H

#include <jni.h>
#include "Object.h"

class AssetManager : public Object
{
public:
    AssetManager(JNIEnv *env);

    ~AssetManager();

    jobject open(jobject instance, jstring fileName);

protected:
    jclass getClass();
};

#endif //OPENGLES30NDK_ASSETMANAGER_H
