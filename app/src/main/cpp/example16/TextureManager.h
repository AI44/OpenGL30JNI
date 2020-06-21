//
// Created by Raining on 2020/6/11.
//

#ifndef OPENGL30JNI_TEXTUREMANAGER_H
#define OPENGL30JNI_TEXTUREMANAGER_H

#include <GLES3/gl3.h>
#include <vector>

class RendererData {
public:
    GLuint framebuffer;
    GLuint texture;
    int width;
    int height;

    RendererData();
};

class InternalRendererData : public RendererData {
public:
    bool cache;

    InternalRendererData();
};

class TextureManager {
private:
    std::vector<InternalRendererData *> buffer;
    std::vector<InternalRendererData *> leakHistory;

public:
    TextureManager();

    ~TextureManager();

    RendererData *getData(int w, int h, bool cache);

    void release(RendererData **);
};

#endif //OPENGL30JNI_TEXTUREMANAGER_H
