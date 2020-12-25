package com.ideacarry.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES11Ext;
import android.opengl.GLES30;
import android.util.Size;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by Raining on 2020/9/2.
 */
public class GLUtils {
    /**
     * 计算最小texture size(最省内存和减少不必要的计算)，此矩形不会大于显示view和camera数据的size
     */
    public static Size getTextureSize(int viewWidth, int viewHeight, int cameraWidth, int cameraHeight) {
        final int maxSize = 4096;
        //计算texture大小
        int w;
        int h;
        //目标比例
        float scale = (float) viewWidth / (float) viewHeight;
        //选宽来按比例计算长宽，长宽为2的倍数
        w = Math.min(cameraWidth, maxSize);
        w = (w >> 1) << 1;
        h = Math.round((float) w / scale);
        h = (h >> 1) << 1;
        //是否为内切矩形
        int minH = Math.min(cameraHeight, maxSize);
        if (h > minH) {
            h = minH;
            h = (h >> 1) << 1;
            w = Math.round(h * scale);
            w = (w >> 1) << 1;
        }
        //是否大于显示矩形
        if (w > viewWidth) {
            w = viewWidth;
            w = (w >> 1) << 1;
            h = viewHeight;
            h = (h >> 1) << 1;
        }
        return new Size(w, h);
    }

    public static int createRGBATexture2D(int w, int h) {
        int[] id = {-1};
        GLES30.glGenTextures(1, id, 0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, id[0]);
        GLES30.glPixelStorei(GLES30.GL_UNPACK_ALIGNMENT, 4);//4字节对齐
        GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RGBA8, w, h, 0,
                GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, null);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);
        return id[0];
    }

    public static int crateFrameBuffer(int textureId) {
        int[] id = {-1};
        GLES30.glGenFramebuffers(1, id, 0);
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, id[0]);
        GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0, GLES30.GL_TEXTURE_2D, textureId, 0);
        if (GLES30.glCheckFramebufferStatus(GLES30.GL_FRAMEBUFFER) != GLES30.GL_FRAMEBUFFER_COMPLETE) {
            System.out.println("ERROR::FRAMEBUFFER:: Framebuffer is not complete!");
        }
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);
        return id[0];
    }

    /**
     * @return [0]:texture Id <br/>
     * [1]:frameBuffer id <br/>
     */
    public static int[] crateFrameBuffer(int w, int h) {
        int textureId = createRGBATexture2D(w, h);
        int frameBufferId = crateFrameBuffer(textureId);
        return new int[]{textureId, frameBufferId};
    }

    public static FloatBuffer getFloatBuffer(float[] arr) {
        // 创建顶点坐标数据缓冲
        // vertices.length*4是因为一个float占四个字节
        ByteBuffer vbb = ByteBuffer.allocateDirect(arr.length * 4);
        vbb.order(ByteOrder.nativeOrder()); //设置字节顺序
        FloatBuffer vertexBuf = vbb.asFloatBuffer(); //转换为Float型缓冲
        vertexBuf.put(arr); //向缓冲区中放入顶点坐标数据
        vertexBuf.position(0); //设置缓冲区起始位置
        return vertexBuf;
    }

    public static final float QUAD_VERTICES[] = {
            // positions // texCoords
            -1.0f, 1.0f, 0.0f, 1.0f,
            -1.0f, -1.0f, 0.0f, 0.0f,
            1.0f, 1.0f, 1.0f, 1.0f,
            1.0f, -1.0f, 1.0f, 0.0f
    };

    /**
     * @return [0]vao, [1]vbo
     */
    public static int[] createQuadVertexArrays(int vertexIndex, int textureVertexIndex) {
        int[] vao = {-1};
        int[] vbo = {-1};

        GLES30.glGenVertexArrays(1, vao, 0);
        GLES30.glGenBuffers(1, vbo, 0);
        GLES30.glBindVertexArray(vao[0]);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vbo[0]);
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, QUAD_VERTICES.length * 4, GLUtils.getFloatBuffer(QUAD_VERTICES), GLES30.GL_STATIC_DRAW);
        GLES30.glEnableVertexAttribArray(vertexIndex);
        GLES30.glVertexAttribPointer(vertexIndex, 2, GLES30.GL_FLOAT, false, 4 * 4, 0);
        GLES30.glEnableVertexAttribArray(textureVertexIndex);
        GLES30.glVertexAttribPointer(textureVertexIndex, 2, GLES30.GL_FLOAT, false, 4 * 4, 2 * 4);
        GLES30.glBindVertexArray(0);

        return new int[]{vao[0], vbo[0]};
    }

    public static Bitmap getImageFromAssetsFile(Context context, String fileName) {
        Bitmap bitmap = null;
        AssetManager manager = context.getResources().getAssets();
        try {
            InputStream is = manager.open(fileName);
            bitmap = BitmapFactory.decodeStream(is);
            is.close();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    /**
     * 加载bitmap纹理
     */
    public static int createTexture(Bitmap bitmap) {
        int[] textureId = new int[1];
        GLES30.glGenTextures(1, textureId, 0);

        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureId[0]);

        GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D,
                GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
        GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D,
                GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR);
        GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D,
                GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
        GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D,
                GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);
        android.opengl.GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, bitmap, 0);
        return textureId[0];
    }

    public static void deleteTexture(int textureId) {
        GLES30.glDeleteTextures(1, new int[]{textureId}, 0);
    }

    /**
     * 加载纹理
     */
    public static int createTextureFromAssets(Context context, String name) {
        Bitmap bitmap = getImageFromAssetsFile(context, name);
        int textureId = createTexture(bitmap);
        bitmap.recycle();
        return textureId;
    }

    /**
     * @param target {@link GLES30#GL_TEXTURE_3D}或{@link GLES30#GL_TEXTURE_2D_ARRAY}
     */
    public static int createTexture3D(int target, Bitmap[] imgArr) {
        int[] id = {-1};
        GLES30.glGenTextures(1, id, 0);

        int depth = imgArr.length;//blue
        Bitmap temp = imgArr[0];
        int itemW = temp.getWidth();
        int itemH = temp.getHeight();

        GLES30.glBindTexture(target, id[0]);
        GLES30.glPixelStorei(GLES30.GL_UNPACK_ALIGNMENT, 4);//4字节对齐
        /*GLES30.glTexImage3D(target, 0, GLES30.GL_RGBA8, itemW, itemH, depth, 0,
                GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, null);*/
        GLES30.glTexStorage3D(target, 1, GLES30.GL_RGBA8, itemW, itemH, depth);//不可变纹理，优化性能
        GLES30.glTexParameterf(target, GLES11Ext.GL_TEXTURE_MAX_ANISOTROPY_EXT, 4);
        GLES30.glTexParameteri(target, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR);
        GLES30.glTexParameteri(target, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
        GLES30.glTexParameteri(target, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
        GLES30.glTexParameteri(target, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);
        GLES30.glTexParameteri(target, GLES30.GL_TEXTURE_WRAP_R, GLES30.GL_CLAMP_TO_EDGE);
        GLES30.glPixelStorei(GLES30.GL_UNPACK_ROW_LENGTH, 0);
        ByteBuffer buf = ByteBuffer.allocate(itemW * itemH * 4);
        for (int i = 0; i < depth; i++) {
            Bitmap sub = imgArr[i];
            buf.rewind();
            sub.copyPixelsToBuffer(buf);
            buf.position(0);
            GLES30.glTexSubImage3D(target, 0, 0, 0, i, itemW, itemH, 1, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, buf);
        }

        return id[0];
    }

    public static int createTexture2DArr(Bitmap[] imgArr) {
        return createTexture3D(GLES30.GL_TEXTURE_2D_ARRAY, imgArr);
    }

    public static int createTexture3D(Bitmap[] imgArr) {
        return createTexture3D(GLES30.GL_TEXTURE_3D, imgArr);
    }

    /**
     * @param target {@link GLES30#GL_TEXTURE_3D}或{@link GLES30#GL_TEXTURE_2D_ARRAY}
     */
    public static int createTexture3D(int target, Bitmap img, int column, int row) {
        int[] id = {-1};
        GLES30.glGenTextures(1, id, 0);

        final int wNum = column;
        final int hNum = row;
        int depth = wNum * hNum;//blue
        int itemW = img.getWidth() / wNum;
        int itemH = img.getHeight() / hNum;

        GLES30.glBindTexture(target, id[0]);
        GLES30.glPixelStorei(GLES30.GL_UNPACK_ALIGNMENT, 4);//4字节对齐
        /*GLES30.glTexImage3D(target, 0, GLES30.GL_RGBA8, itemW, itemH, depth, 0,
                GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, null);*/
        GLES30.glTexStorage3D(target, 1, GLES30.GL_RGBA8, itemW, itemH, depth);//不可变纹理，优化性能
        GLES30.glTexParameteri(target, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR);
        GLES30.glTexParameteri(target, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
        GLES30.glTexParameteri(target, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
        GLES30.glTexParameteri(target, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);
        GLES30.glTexParameteri(target, GLES30.GL_TEXTURE_WRAP_R, GLES30.GL_CLAMP_TO_EDGE);
        //GLES30.glPixelStorei(GLES30.GL_UNPACK_ROW_LENGTH, img.getWidth());
        GLES30.glPixelStorei(GLES30.GL_UNPACK_ROW_LENGTH, 0);
        ByteBuffer buf = ByteBuffer.allocate(itemW * itemH * 4);
        for (int i = 0; i < depth; i++) {
            Bitmap sub = Bitmap.createBitmap(img, (i % wNum) * itemW, i / wNum * itemH, itemW, itemH);
            buf.rewind();
            sub.copyPixelsToBuffer(buf);
            buf.position(0);
            sub.recycle();
            GLES30.glTexSubImage3D(target, 0, 0, 0, i, itemW, itemH, 1, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, buf);
        }
        //GLES30.glPixelStorei(GLES30.GL_UNPACK_ROW_LENGTH, 0);

        return id[0];
    }

    public static int createTexture2DArr(Bitmap img, int column, int row) {
        return createTexture3D(GLES30.GL_TEXTURE_2D_ARRAY, img, column, row);
    }

    public static int createTexture3D(Bitmap img, int column, int row) {
        return createTexture3D(GLES30.GL_TEXTURE_3D, img, column, row);
    }

    /**
     * 加载3d纹理
     */
    public static int createTexture3DFromAssets(Context context, String name, int column, int row) {
        Bitmap bitmap = getImageFromAssetsFile(context, name);
        int textureId = createTexture3D(bitmap, column, row);
        bitmap.recycle();
        return textureId;
    }
}
