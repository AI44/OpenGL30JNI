package com.ideacarry.utils

import android.opengl.GLES30
import java.nio.Buffer
import kotlin.math.ceil

/**
 * Created by Raining on 2023/5/26
 *
 * GL工具类
 */
object GLUtilsKt {

    fun createYTexture(buffer: Buffer, width: Int, height: Int): Int {
        val textureId = IntArray(1)
        GLES30.glGenTextures(1, textureId, 0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureId[0])
        GLES30.glPixelStorei(GLES30.GL_UNPACK_ALIGNMENT, 1);//1字节对齐
        GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR.toFloat())
        GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR.toFloat())
        GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE.toFloat())
        GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE.toFloat())
        GLES30.glTexImage2D(
            GLES30.GL_TEXTURE_2D,
            0,
            GLES30.GL_R8,
            width,
            height,
            0,
            GLES30.GL_RED,
            GLES30.GL_UNSIGNED_BYTE,
            buffer
        )
        return textureId[0]
    }

    fun createUVTexture(buffer: Buffer, width: Int, height: Int): Int {
        val textureId = IntArray(1)
        GLES30.glGenTextures(1, textureId, 0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureId[0])
        GLES30.glPixelStorei(GLES30.GL_UNPACK_ALIGNMENT, 2);//2字节对齐
        GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR.toFloat())
        GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR.toFloat())
        GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE.toFloat())
        GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE.toFloat())
        GLES30.glTexImage2D(
            GLES30.GL_TEXTURE_2D,
            0,
            GLES30.GL_RG8,
            ceil(width / 2f).toInt(),
            ceil(height / 2f).toInt(),
            0,
            GLES30.GL_RG,
            GLES30.GL_UNSIGNED_BYTE,
            buffer
        )
        return textureId[0]
    }
}