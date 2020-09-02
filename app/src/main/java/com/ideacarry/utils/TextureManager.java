package com.ideacarry.utils;

import android.opengl.GLES30;

import java.util.ArrayList;

/**
 * Created by Raining on 2020/9/2.
 */
public class TextureManager {
    private ArrayList<InternalRendererData> buffer = new ArrayList<>();
    private ArrayList<InternalRendererData> leakHistory = new ArrayList<>();

    public static class RendererData {
        public int framebuffer = -1;
        public int texture = -1;
        public int width;
        public int height;
    }

    public static class InternalRendererData extends RendererData {
        public Boolean cache;
    }

    public static InternalRendererData createData(int w, int h, boolean cache) {
        InternalRendererData result = new InternalRendererData();
        int[] params = GLUtils.crateFrameBuffer(w, h);
        result.texture = params[0];
        result.framebuffer = params[1];
        result.width = w;
        result.height = h;
        result.cache = cache;
        return result;
    }

    public static void releaseData(InternalRendererData data) {
        if (data != null) {
            if (data.framebuffer > -1) {
                GLES30.glDeleteFramebuffers(1, new int[]{data.framebuffer}, 0);
                data.framebuffer = -1;
            }
            if (data.texture > -1) {
                GLES30.glDeleteTextures(1, new int[]{data.texture}, 0);
                data.texture = -1;
            }
        }
    }

    public RendererData getData(int w, int h, boolean cache) {
        InternalRendererData result;
        int len = buffer.size();
        for (int i = 0; i < len; i++) {
            result = buffer.get(i);
            if (result != null) {
                if (result.width == w && result.height == h) {
                    result.cache = cache;
                    buffer.remove(i);
                    leakHistory.add(result);
                    return result;
                }
            }
        }
        result = createData(w, h, cache);
        leakHistory.add(result);
        return result;
    }

    public static boolean removeHistory(ArrayList<InternalRendererData> list, RendererData data) {
        boolean result = false;
        if (data != null) {
            int len = list.size();
            InternalRendererData temp;
            for (int i = 0; i < len; i++) {
                temp = list.get(i);
                if (temp == data) {
                    list.remove(i);
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    public void release(RendererData item) {
        if (item != null && removeHistory(leakHistory, item)) {
            InternalRendererData temp = (InternalRendererData) item;
            if (temp.cache) {
                buffer.add(temp);
            } else {
                releaseData(temp);
            }
        }
    }

    public void clear() {
        for (InternalRendererData value : buffer) {
            if (value != null) {
                releaseData(value);
            }
        }
        buffer.clear();

        for (InternalRendererData value2 : leakHistory) {
            if (value2 != null) {
                releaseData(value2);
            }
        }
        leakHistory.clear();
    }
}
