/*
 * Copyright 2014 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.grafika.gles

import android.opengl.GLES30
import android.opengl.Matrix
import android.util.Log
import com.ideacarry.opengl30jni.BuildConfig
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

/**
 * Some OpenGL utility functions.
 */
object GlUtil {
    const val TAG = "Grafika"

    /**
     * Identity matrix for general use.  Don't modify or life will get weird.
     */
    private val IDENTITY_MATRIX: FloatArray = FloatArray(16)
    private const val SIZEOF_FLOAT = 4
    private const val SIZEOF_SHORT = 2

    /**
     * Creates a new program from the supplied vertex and fragment shaders.
     *
     * @return A handle to the program, or 0 on failure.
     */
    @JvmStatic
    fun createProgram(vertexSource: String, fragmentSource: String): Int {
        val vertexShader = loadShader(GLES30.GL_VERTEX_SHADER, vertexSource)
        if (vertexShader == 0) {
            return 0
        }
        val pixelShader = loadShader(GLES30.GL_FRAGMENT_SHADER, fragmentSource)
        if (pixelShader == 0) {
            return 0
        }
        var program = GLES30.glCreateProgram()
        checkGlError("glCreateProgram")
        if (program == 0) {
            Log.e(TAG, "Could not create program")
        }
        GLES30.glAttachShader(program, vertexShader)
        checkGlError("glAttachShader")
        GLES30.glAttachShader(program, pixelShader)
        checkGlError("glAttachShader")
        GLES30.glLinkProgram(program)
        val linkStatus = IntArray(1)
        GLES30.glGetProgramiv(program, GLES30.GL_LINK_STATUS, linkStatus, 0)
        if (linkStatus[0] != GLES30.GL_TRUE) {
            Log.e(TAG, "Could not link program: ")
            Log.e(TAG, GLES30.glGetProgramInfoLog(program))
            GLES30.glDeleteProgram(program)
            program = 0
        }
        return program
    }

    /**
     * Compiles the provided shader source.
     *
     * @return A handle to the shader, or 0 on failure.
     */
    @JvmStatic
    fun loadShader(shaderType: Int, source: String?): Int {
        var shader = GLES30.glCreateShader(shaderType)
        checkGlError("glCreateShader type=$shaderType")
        GLES30.glShaderSource(shader, source)
        GLES30.glCompileShader(shader)
        val compiled = IntArray(1)
        GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, compiled, 0)
        if (compiled[0] == 0) {
            Log.e(TAG, "Could not compile shader $shaderType:")
            Log.e(TAG, " " + GLES30.glGetShaderInfoLog(shader))
            GLES30.glDeleteShader(shader)
            shader = 0
        }
        return shader
    }

    /**
     * Checks to see if a GLES error has been raised.
     */
    @JvmStatic
    fun checkGlError(op: String) {
        val error = GLES30.glGetError()
        if (error != GLES30.GL_NO_ERROR) {
            val msg = op + ": glError 0x" + Integer.toHexString(error)
            Log.e(TAG, msg)
            throw RuntimeException(msg)
        }
    }

    /**
     * Checks to see if the location we obtained is valid.  GLES returns -1 if a label
     * could not be found, but does not set the GL error.
     *
     *
     * Throws a RuntimeException if the location is invalid.
     */
    @JvmStatic
    fun checkLocation(location: Int, label: String) {
        if (location < 0) {
            throw RuntimeException("Unable to locate '$label' in program")
        }
    }

    /**
     * Creates a texture from raw data.
     *
     * @param data   Image data, in a "direct" ByteBuffer.
     * @param width  Texture width, in pixels (not bytes).
     * @param height Texture height, in pixels.
     * @param format Image data format (use constant appropriate for glTexImage2D(), e.g. GL_RGBA).
     * @return Handle to texture.
     */
    @JvmStatic
    fun createImageTexture(data: ByteBuffer?, width: Int, height: Int, format: Int): Int {
        val textureHandles = IntArray(1)
        GLES30.glGenTextures(1, textureHandles, 0)
        val textureHandle = textureHandles[0]
        checkGlError("glGenTextures")

        // Bind the texture handle to the 2D texture target.
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureHandle)

        // Configure min/mag filtering, i.e. what scaling method do we use if what we're rendering
        // is smaller or larger than the source image.
        GLES30.glTexParameteri(
            GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER,
            GLES30.GL_LINEAR
        )
        GLES30.glTexParameteri(
            GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER,
            GLES30.GL_LINEAR
        )
        checkGlError("loadImageTexture")
        GLES30.glPixelStorei(GLES30.GL_UNPACK_ALIGNMENT, 1) //1字节对齐
        // Load the data from the buffer into the texture handle.
        GLES30.glTexImage2D(
            GLES30.GL_TEXTURE_2D,  /*level*/0, format,
            width, height,  /*border*/0, format, GLES30.GL_UNSIGNED_BYTE, data
        )
        checkGlError("loadImageTexture")
        return textureHandle
    }

    /**
     * Allocates a direct short buffer, and populates it with the short array data.
     */
    @JvmStatic
    fun createShortBuffer(indices: ShortArray): ShortBuffer {
        // Allocate a direct ByteBuffer, using 2 bytes per short, and copy indices into it.
        val bb = ByteBuffer.allocateDirect(indices.size * SIZEOF_SHORT)
        bb.order(ByteOrder.nativeOrder())
        val sb = bb.asShortBuffer()
        sb.put(indices)
        sb.position(0)
        return sb
    }

    /**
     * Allocates a direct float buffer, and populates it with the float array data.
     */
    @JvmStatic
    fun createFloatBuffer(coords: FloatArray): FloatBuffer {
        // Allocate a direct ByteBuffer, using 4 bytes per float, and copy coords into it.
        val bb = ByteBuffer.allocateDirect(coords.size * SIZEOF_FLOAT)
        bb.order(ByteOrder.nativeOrder())
        val fb = bb.asFloatBuffer()
        fb.put(coords)
        fb.position(0)
        return fb
    }

    /**
     * Writes GL version info to the log.
     */
    @JvmStatic
    fun logVersionInfo() {
        Log.i(TAG, "vendor  : " + GLES30.glGetString(GLES30.GL_VENDOR))
        Log.i(TAG, "renderer: " + GLES30.glGetString(GLES30.GL_RENDERER))
        Log.i(TAG, "version : " + GLES30.glGetString(GLES30.GL_VERSION))
        if (BuildConfig.DEBUG) {
            val values = IntArray(1)
            GLES30.glGetIntegerv(GLES30.GL_MAJOR_VERSION, values, 0)
            val majorVersion = values[0]
            GLES30.glGetIntegerv(GLES30.GL_MINOR_VERSION, values, 0)
            val minorVersion = values[0]
            if (GLES30.glGetError() == GLES30.GL_NO_ERROR) {
                Log.i(TAG, "iversion: $majorVersion.$minorVersion")
            }
        }
    }

    init {
        Matrix.setIdentityM(IDENTITY_MATRIX, 0)
    }
}