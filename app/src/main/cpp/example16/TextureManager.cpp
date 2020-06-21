//
// Created by Raining on 2020/6/11.
//

#include "TextureManager.h"
#include "esUtils.h"

static InternalRendererData *createData(int w, int h, bool cache) {
    auto *data = new InternalRendererData();
    data->width = w;
    data->height = h;
    data->cache = cache;

    glGenFramebuffers(1, &(data->framebuffer));
    data->texture = createRGBATexture2D(w, h);

    // framebuffer configuration
    glBindFramebuffer(GL_FRAMEBUFFER, data->framebuffer);
    glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, data->texture,
                           0);
    if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE)
        ALOGE ("ERROR::FRAMEBUFFER:: Framebuffer is not complete!");
    glBindFramebuffer(GL_FRAMEBUFFER, 0);

    return data;
}

static void releaseData(InternalRendererData *data) {
    glDeleteFramebuffers(1, &(data->framebuffer));
    glDeleteTextures(1, &(data->texture));
}

static bool removeHistory(std::vector<InternalRendererData *> *list, RendererData *data) {
    bool result = false;
    if (data) {
        for (auto it = list->begin(); it != list->end(); ++it) {
            if (data == (*it)) {
                list->erase(it);
                result = true;
                break;
            }
        }
    }
    return result;
}

RendererData::RendererData() : framebuffer(-1), texture(-1), width(-1), height(-1) {
}

InternalRendererData::InternalRendererData() : cache(false) {
}

TextureManager::TextureManager() : buffer(10), leakHistory(10) {
}

TextureManager::~TextureManager() {
    for (auto const &value: buffer) {
        if (value) {
            releaseData(value);
            delete value;
        }
    }
    buffer.clear();

    for (auto const &value2: leakHistory) {
        if (value2) {
            releaseData(value2);
            delete value2;
        }
    }
    leakHistory.clear();
}

RendererData *TextureManager::getData(int w, int h, bool cache) {
    InternalRendererData *result = nullptr;
    for (auto it = buffer.begin(); it != buffer.end(); ++it) {
        result = *it;
        if (result) {
            if (result->width == w && result->height == h) {
                result->cache = cache;
                buffer.erase(it);
                leakHistory.push_back(result);
                return result;
            }
        }
    }
    result = createData(w, h, cache);
    leakHistory.push_back(result);
    return result;
}

void TextureManager::release(RendererData **data) {
    if (removeHistory(&leakHistory, *data)) {
        auto *temp = (InternalRendererData *) *data;
        if (temp->cache) {
            buffer.push_back(temp);
        } else {
            releaseData(temp);
            delete temp;
        }
        *data = nullptr;
    }
}