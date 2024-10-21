package com.ideacarry.example28

import android.content.Context
import android.graphics.SurfaceTexture
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.opengl.EGL14
import android.os.Handler
import android.os.HandlerThread
import android.os.Process.THREAD_PRIORITY_URGENT_DISPLAY
import android.util.Log
import android.view.Surface
import com.android.grafika.gles.EglCore
import com.android.grafika.gles.OffscreenSurface
import com.ideacarry.example14.GLRenderer
import com.ideacarry.example27.decoder.DecoderHelper
import com.ideacarry.example27.decoder.FrameType
import com.ideacarry.example27.decoder.OnUpdateFrameListener

/**
 * Created by Raining on 2023/08/16
 *
 * 视频解码到texture
 */
class AsyncVideoDecoder2(
    ctx: Context? = null,
    path: String,
    private val updateFrameListener: OnUpdateFrameListener
) {
    private val handlerThread = HandlerThread("async_video_decoder2", THREAD_PRIORITY_URGENT_DISPLAY)
    private val decoderHandler by lazy {
        handlerThread.start()
        Handler(handlerThread.looper)
    }
    private val context: Context? = ctx?.applicationContext
    private var isLoopPlay = true
    private val extractor by lazy {
        DecoderHelper.createMediaExtractor(path, context)
    }
    private val mediaInfo by lazy {
        DecoderHelper.getVideoMediaInfo(extractor)
    }
    private var mediaCodec: MediaCodec? = null
    private var firstPts = -1L // seek后需重置
    private var endPts = -1L
    private var maxPts = -1L

    private fun createMediaCodec(): MediaCodec? {
        val info = mediaInfo ?: return null
        return DecoderHelper.createMediaCodec(info.format)
    }

    /**
     * 离屏渲染gl环境
     */
    private val eglCore by lazy {
        EglCore(EGL14.EGL_NO_CONTEXT, EglCore.FLAG_TRY_GLES3)
    }
    private val offscreenSurface by lazy {
        OffscreenSurface(eglCore, 0, 0)
    }
    private var oesTextureId = -1
    private val surfaceTexture by lazy {
        offscreenSurface.makeCurrent()
        oesTextureId = GLRenderer.createTextureObject()
        Log.d(TAG, "oesTextureId = $oesTextureId")
        SurfaceTexture(oesTextureId)
    }

    /**
     * 用于创建共享gl context实现texture共享
     */
    fun getEglContext() = eglCore.eglContext

    init {
        decoderHandler.post {
            start()
        }
    }

    private fun start() {
        val info = mediaInfo
        if (info == null || info.index < 0) {
            // error
            return
        }
        extractor.selectTrack(info.index)
        if (mediaCodec == null) {
            mediaCodec = createMediaCodec()
        }
        mediaCodec?.let {
            it.setCallback(object : MediaCodec.Callback() {
                override fun onInputBufferAvailable(codec: MediaCodec, index: Int) {
                    // mediaCodec.dequeueInputBuffer
                    val result = writeSample(codec, index)
                    if (result) {
                        extractor.advance()
                    }
                }

                override fun onOutputBufferAvailable(codec: MediaCodec, index: Int, info: MediaCodec.BufferInfo) {
                    // mediaCodec.dequeueOutputBuffer
                    handleOnOutputBufferAvailable(codec, index, info)
                }

                override fun onError(codec: MediaCodec, e: MediaCodec.CodecException) {
                    // error
                }

                override fun onOutputFormatChanged(codec: MediaCodec, format: MediaFormat) {
                }
            }, decoderHandler)
            it.configure(info.format, Surface(surfaceTexture), null, 0)
            it.start()
        }
    }

    private fun writeSample(codec: MediaCodec, index: Int): Boolean {
        val inputBuffer = codec.getInputBuffer(index) ?: return false
        var reread = false
        var size: Int
        var flags: Int
        var presentationTimeUs: Long
        do {
            size = extractor.readSampleData(inputBuffer, 0)
            flags = extractor.sampleFlags
            presentationTimeUs = extractor.sampleTime
            if (size > 0) {
                // 成功读取到数据
                if (presentationTimeUs > maxPts) {
                    maxPts = presentationTimeUs
                }
                if (firstPts < 0) {
                    firstPts = presentationTimeUs
                }
                break
            }
            // 读取结束
            if (endPts < 0) {
                endPts = maxPts
            }
            // 判断是否循环播放
            if (reread.not() && isLoopPlay) {
                // 循环播放
                extractor.seekTo(0, MediaExtractor.SEEK_TO_PREVIOUS_SYNC)
                // 重新读取一次
                reread = true
                continue
            }

            // 解码结束
            size = 0
            flags = flags or MediaCodec.BUFFER_FLAG_END_OF_STREAM
        } while (reread)

        codec.queueInputBuffer(index, 0, size, presentationTimeUs, flags)
        return size > 0
    }


    private fun handleOnOutputBufferAvailable(codec: MediaCodec, index: Int, info: MediaCodec.BufferInfo) {
        codec.getOutputBuffer(index) ?: return
        updateFrameListener.onUpdate(
            textureId = oesTextureId,
            info = info,
            durationUs = mediaInfo?.durationUs ?: 0,
        ) { renderScreen ->
            decoderHandler.post {
                codec.releaseOutputBuffer(index, true)
                renderScreen()
            }
        }
    }

    private fun getFrameType(pts: Long): FrameType {
        return when (pts) {
            firstPts -> FrameType.FIRST
            endPts -> FrameType.END
            else -> FrameType.MIDDLE
        }
    }

    fun release() {
        decoderHandler.post {
            mediaCodec?.let {
                it.stop()
                it.release()
            }
            mediaCodec = null
            extractor.release()
            surfaceTexture.release()
            offscreenSurface.release()
            eglCore.release()
        }
        handlerThread.quitSafely()
    }

    companion object {
        private const val TAG = "AsyncVideoDecoder2"
    }

    interface OnUpdateFrameListener {
        fun onUpdate(
            textureId: Int,
            info: MediaCodec.BufferInfo,
            durationUs: Long,
            renderTexture: (renderScreen: () -> Unit) -> Unit,
        )
    }
}