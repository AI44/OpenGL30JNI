#include "esUtils.h"
#include "java/Context.h"
#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>
#include <image/ImageUtils.h>
#include "java/AssetManager.h"
#include "java/BitmapFactory.h"
#include "java/BitmapFactoryOptions.h"
#include "java/Resources.h"

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

jobject readRawResImage(JNIEnv *env, jobject context, jint id) {

    jobject bmp = nullptr;

    Context ctx(env);
    jobject resourcesObj = ctx.getResources(context);
    Resources resources(env);
    jobject inputStreamObj = resources.openRawResource(resourcesObj, id);
    BitmapFactory bf(env);
    BitmapFactoryOptions bfo(env);
    jobject optsObj = bfo.newObjectARGB8888();

    bmp = bf.decodeStream(inputStreamObj, nullptr, optsObj);

    env->DeleteLocalRef(optsObj);
    env->DeleteLocalRef(inputStreamObj);
    env->DeleteLocalRef(resourcesObj);

    return bmp;
}

jobject readPathImage(JNIEnv *env, const char *path) {

    jobject bmp = nullptr;

    jstring pathObj = env->NewStringUTF(path);
    BitmapFactory bf(env);
    BitmapFactoryOptions bfo(env);
    jobject optsObj = bfo.newObjectARGB8888();

    bmp = bf.decodeFile(pathObj, optsObj);

    env->DeleteLocalRef(optsObj);
    env->DeleteLocalRef(pathObj);

    return bmp;
}

GLuint loadAssetsTexture2D(JNIEnv *env, jobject context, char const *path) {
    return loadAssetsTexture2D(env, context, path, GL_CLAMP_TO_EDGE);
}

GLuint loadAssetsTexture2D(JNIEnv *env, jobject context, char const *path, GLint textureWrapping) {
    jobject bmp = readAssetImage(env, context, path);
    GLuint re = loadAssetsTexture2D(env, bmp, textureWrapping);
    env->DeleteLocalRef(bmp);
    return re;
}

GLuint loadAssetsTexture2D(JNIEnv *env, jobject bmp, GLint textureWrapping) {
    GLuint id;
    glGenTextures(1, &id);

    Image8888 img;
    img.SetImage(env, bmp);
    if (bmp) {
        glBindTexture(GL_TEXTURE_2D, id);
        glPixelStorei(GL_UNPACK_ALIGNMENT, 4);//4字节对齐
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, img.m_width, img.m_height, 0, GL_RGBA,
                     GL_UNSIGNED_BYTE, img.m_pDatas);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, textureWrapping);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, textureWrapping);

        img.ClearAll();
    }

    return id;
}

GLuint createRGBATexture2D(int w, int h) {
    GLuint id;
    glGenTextures(1, &id);
    glBindTexture(GL_TEXTURE_2D, id);
    glPixelStorei(GL_UNPACK_ALIGNMENT, 4);//4字节对齐
    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, w, h, 0,
                 GL_RGBA, GL_UNSIGNED_BYTE, nullptr);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
    return id;
}

esUtils::Shader::Shader(JNIEnv *env, jobject context, const char *vertexAssetsPath, const char *fragmentAssetsPath) {
    char *vertexShaderCode = (char *) readAssetFile(env, context, vertexAssetsPath, true, nullptr);
    char *fragmentShaderCode = (char *) readAssetFile(env, context, fragmentAssetsPath, true,
                                                      nullptr);

    ID = createProgram(vertexShaderCode, fragmentShaderCode);

    free(vertexShaderCode);
    free(fragmentShaderCode);
}

esUtils::Shader::Shader(const char *vertexShaderCode, const char *fragmentShaderCode) {
    ID = createProgram(vertexShaderCode, fragmentShaderCode);
}

void esUtils::Shader::use() {
    glUseProgram(ID);
}

void esUtils::Shader::release() {
    glDeleteProgram(ID);
}

void esUtils::Shader::setBool(const char *name, bool value) const {
    glUniform1i(glGetUniformLocation(ID, name), (int) value);
}

void esUtils::Shader::setInt(const char *name, int value) const {
    glUniform1i(glGetUniformLocation(ID, name), value);
}

void esUtils::Shader::setFloat(const char *name, float value) const {
    glUniform1f(glGetUniformLocation(ID, name), value);
}

void esUtils::Shader::setVec2(const char *name, const glm::vec2 &value) const {
    glUniform2fv(glGetUniformLocation(ID, name), 1, &value[0]);
}

void esUtils::Shader::setVec2(const char *name, float x, float y) const {
    glUniform2f(glGetUniformLocation(ID, name), x, y);
}

void esUtils::Shader::setVec3(const char *name, const glm::vec3 &value) const {
    glUniform3fv(glGetUniformLocation(ID, name), 1, &value[0]);
}

void esUtils::Shader::setVec3(const char *name, float x, float y, float z) const {
    glUniform3f(glGetUniformLocation(ID, name), x, y, z);
}

void esUtils::Shader::setVec4(const char *name, const glm::vec4 &value) const {
    glUniform4fv(glGetUniformLocation(ID, name), 1, &value[0]);
}

void esUtils::Shader::setVec4(const char *name, float x, float y, float z, float w) const {
    glUniform4f(glGetUniformLocation(ID, name), x, y, z, w);
}

void esUtils::Shader::setMat2(const char *name, const glm::mat2 &mat) const {
    glUniformMatrix2fv(glGetUniformLocation(ID, name), 1, GL_FALSE, &mat[0][0]);
}

void esUtils::Shader::setMat3(const char *name, const glm::mat3 &mat) const {
    glUniformMatrix3fv(glGetUniformLocation(ID, name), 1, GL_FALSE, &mat[0][0]);
}

void esUtils::Shader::setMat4(const char *name, const glm::mat4 &mat) const {
    glUniformMatrix4fv(glGetUniformLocation(ID, name), 1, GL_FALSE, &mat[0][0]);
}

void esUtils::Shader::setMat4(const char *name, const float *fv) const {
    glUniformMatrix4fv(glGetUniformLocation(ID, name), 1, GL_FALSE, fv);
}

void esUtils::Shader::checkCompileErrors(GLuint shader, const CompileType type) {
    GLint success;
    GLchar infoLog[1024];
    if (type != CompileType::program) {
        glGetShaderiv(shader, GL_COMPILE_STATUS, &success);
        if (!success) {
            glGetShaderInfoLog(shader, 1024, nullptr, infoLog);
            ALOGE("ERROR::SHADER_COMPILATION_ERROR of type: PROGRAM\n%s\n -- --------------------------------------------------- -- ",
                  infoLog);
        }
    } else {
        glGetProgramiv(shader, GL_LINK_STATUS, &success);
        if (!success) {
            glGetProgramInfoLog(shader, 1024, nullptr, infoLog);
            ALOGE("ERROR::PROGRAM_LINKING_ERROR of type: SHADER\n%s\n -- --------------------------------------------------- -- ",
                  infoLog);
        }
    }
}