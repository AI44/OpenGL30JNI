package com.ideacarry.example27.player

import android.content.Context
import android.media.MediaCodec
import android.media.MediaFormat
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.ideacarry.example27.extensions.correctHeight
import com.ideacarry.example27.extensions.correctWidth
import com.ideacarry.example27.extensions.height
import com.ideacarry.example27.extensions.rotation
import com.ideacarry.example27.extensions.width
import com.ideacarry.utils.CommonUtils
import com.ideacarry.utils.GLShaderProgram
import com.ideacarry.utils.GLUtils
import com.ideacarry.utils.GLUtilsKt
import java.nio.ByteBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Created by Raining on 2023/1/6
 *
 * 实现视频画面渲染(YUV)
 */
class VideoGLRenderer(
    private val context: Context,
) : GLSurfaceView.Renderer, DefaultLifecycleObserver {

    private var shaderProgram: GLShaderProgram? = null
    private var vao: Int = -1
    private var vbo: Int = -1
    private var textureY: Int = -1
    private var textureVU: Int = -1
    private var textureArr = intArrayOf(textureY, textureVU)
    private var videoWidth = -1
    private var videoHeight = -1
    private var videoDegree = 0 // 视频旋转方向
    private var surfaceWidth = -1
    private var surfaceHeight = -1
    private val matrix = FloatArray(16)

    override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
        val vsCode = String(CommonUtils.readAssetFile(context, "example27/vertex.glsl"))
        val fsCode = String(CommonUtils.readAssetFile(context, "example27/fragment_bt709yuv.glsl"))
        shaderProgram = GLShaderProgram(vsCode, fsCode)
        shaderProgram?.let {
            it.use()
            //设置常量y
            it.setInt("uTextureY", 0)
            //设置常量vu
            it.setInt("uTextureVU", 1)
        }
        val arr = GLUtils.createQuadVertexArrays(0, 1)
        vao = arr[0]
        vbo = arr[1]
    }

    override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
        surfaceWidth = width
        surfaceHeight = height
        updateViewport()
    }

    override fun onDrawFrame(gl: GL10) {
        GLES30.glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)

        if (textureY < 0 || textureVU < 0) {
            return
        }
        val program = shaderProgram ?: return
        program.use()
        GLES30.glBindVertexArray(vao)

        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureY)

        GLES30.glActiveTexture(GLES30.GL_TEXTURE1)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureVU)

        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4)

        GLES30.glBindVertexArray(0)
        GLES30.glUseProgram(0)
    }

    fun updateDraw(buffer: ByteBuffer, info: MediaCodec.BufferInfo, format: MediaFormat) {
        if (info.size <= 0) {
            return
        }
        correctViewport(format)
        clearTexture()
        val w = format.width()
        val h = format.height()
        buffer.position(info.offset)
        textureY = GLUtilsKt.createYTexture(buffer, w, h)
        buffer.position(info.offset + w * h)
        textureVU = GLUtilsKt.createUVTexture(buffer, w, h)
    }

    private fun clearTexture() {
        if (textureY > -1 || textureVU > -1) {
            textureArr[0] = textureY
            textureArr[1] = textureVU
            GLES30.glDeleteTextures(2, textureArr, 0)
            textureY = -1
            textureVU = -1
        }
    }

    private fun correctViewport(format: MediaFormat) {
        val w = format.correctWidth()
        val h = format.correctHeight()
        if (videoWidth != w || videoHeight != h) {
            videoWidth = w
            videoHeight = h
            videoDegree = format.rotation()
            updateViewport()
        }
    }

    private fun updateViewport() {
        // 居中缩放顶边显示
        if (videoWidth > 0 && videoHeight > 0) {
            val scale = min(surfaceWidth.toFloat() / videoWidth, surfaceHeight.toFloat() / videoHeight)
            val renderWidth = (scale * videoWidth).roundToInt()
            val renderHeight = (scale * videoHeight).roundToInt()
            val x = (surfaceWidth - renderWidth) / 2
            val y = (surfaceHeight - renderHeight) / 2
            GLES30.glViewport(x, y, renderWidth, renderHeight)
        }
        shaderProgram?.let {
            Matrix.setIdentityM(matrix, 0)
            Matrix.scaleM(matrix, 0, 1f, -1f, 1f) // gl画面上下反转矫正
            Matrix.rotateM(matrix, 0, videoDegree.toFloat(), 0f, 0f, 1f)

            it.use()
            it.setMat4("uTextureMatrix", matrix);
        }
    }

    override fun onDestroy(owner: LifecycleOwner) {
        GLUtils.deleteQuadVertexArrays(vao, vbo)
        vao = -1
        vbo = -1

        super.onDestroy(owner)
    }
}