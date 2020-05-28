#include "esUtils.h"
#include "java/Context.h"
#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>
#include "java/AssetManager.h"
#include "java/BitmapFactory.h"
#include "java/BitmapFactoryOptions.h"

bool checkGlError(const char *funcName) {
    GLint err = glGetError();
    if (err != GL_NO_ERROR) {
        ALOGE("GL error after %s(): 0x%08x\n", funcName, err);
        return true;
    }
    return false;
}

GLuint createShader(GLenum shaderType, const char *src) {
    GLuint shader = glCreateShader(shaderType);
    if (!shader) {
        checkGlError("glCreateShader");
        return 0;
    }
    glShaderSource(shader, 1, &src, NULL);

    GLint compiled = GL_FALSE;
    glCompileShader(shader);
    glGetShaderiv(shader, GL_COMPILE_STATUS, &compiled);
    if (!compiled) {
        GLint infoLogLen = 0;
        glGetShaderiv(shader, GL_INFO_LOG_LENGTH, &infoLogLen);
        if (infoLogLen > 0) {
            GLchar *infoLog = (GLchar *) malloc(infoLogLen * sizeof(GLchar));
            if (infoLog) {
                glGetShaderInfoLog(shader, infoLogLen, NULL, infoLog);
                ALOGE("Could not compile %s shader:\n%s\n",
                      shaderType == GL_VERTEX_SHADER ? "vertex" : "fragment",
                      infoLog);
                free(infoLog);
            }
        }
        glDeleteShader(shader);
        return 0;
    }

    return shader;
}

GLuint createProgram(const char *vtxSrc, const char *fragSrc) {
    GLuint vtxShader = 0;
    GLuint fragShader = 0;
    GLuint program = 0;
    GLint linked = GL_FALSE;

    vtxShader = createShader(GL_VERTEX_SHADER, vtxSrc);
    if (!vtxShader)
        goto EXIT;

    fragShader = createShader(GL_FRAGMENT_SHADER, fragSrc);
    if (!fragShader)
        goto EXIT;

    program = glCreateProgram();
    if (!program) {
        checkGlError("glCreateProgram");
        goto EXIT;
    }
    glAttachShader(program, vtxShader);
    glAttachShader(program, fragShader);

    glLinkProgram(program);
    glGetProgramiv(program, GL_LINK_STATUS, &linked);
    if (!linked) {
        ALOGE("Could not link program");
        GLint infoLogLen = 0;
        glGetProgramiv(program, GL_INFO_LOG_LENGTH, &infoLogLen);
        if (infoLogLen) {
            GLchar *infoLog = (GLchar *) malloc(infoLogLen * sizeof(GLchar));
            if (infoLog) {
                glGetProgramInfoLog(program, infoLogLen, NULL, infoLog);
                ALOGE("Could not link program:\n%s\n", infoLog);
                free(infoLog);
            }
        }
        glDeleteProgram(program);
        program = 0;
    }

    EXIT:
    glDeleteShader(vtxShader);
    glDeleteShader(fragShader);
    return program;
}

uint8_t *
readAssetFile(JNIEnv *env, jobject context, const char *fileName, bool isString, int *len) {

    uint8_t *buf = nullptr;

    Context ctx(env);
    jobject assetManager = ctx.getAssets(context);
    AAssetManager *mgr = AAssetManager_fromJava(env, assetManager);
    if (mgr) {
        AAsset *asset = AAssetManager_open(mgr, fileName, AASSET_MODE_BUFFER);
        off_t size = AAsset_getLength(asset);
        if (len) {
            *len = size;
        }
        if (size > 0) {
            if (isString) {
                buf = (uint8_t *) malloc(size + 1);
                buf[size] = 0;
            } else {
                buf = (uint8_t *) malloc(size);
            }
            AAsset_read(asset, buf, size);
        } else {
            ALOGE("data is NULL");
        }
        AAsset_close(asset);
    } else {
        ALOGE("AAssetManager is NULL");
    }
    env->DeleteLocalRef(assetManager);

    return buf;
}

jobject readAssetImage(JNIEnv *env, jobject context, const char *fileName) {

    jobject bmp = nullptr;

    Context ctx(env);
    jobject assetManagerObj = ctx.getAssets(context);
    AssetManager am(env);
    jstring fileNameObj = env->NewStringUTF(fileName);
    jobject inputStreamObj = am.open(assetManagerObj, fileNameObj);
    BitmapFactory bf(env);
    BitmapFactoryOptions bfo(env);
    jobject optsObj = bfo.newObjectARGB8888();

    bmp = bf.decodeStream(inputStreamObj, nullptr, optsObj);

    env->DeleteLocalRef(optsObj);
    env->DeleteLocalRef(inputStreamObj);
    env->DeleteLocalRef(fileNameObj);
    env->DeleteLocalRef(assetManagerObj);

    return bmp;
}