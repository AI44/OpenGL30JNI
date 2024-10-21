package com.ideacarry.example27.decoder


import android.content.Context
import android.media.MediaCodec
import android.media.MediaCodec.CryptoInfo
import android.media.MediaExtractor
import android.media.MediaFormat
import android.os.Handler
import android.os.HandlerThread
import android.os.Process.THREAD_PRIORITY_URGENT_DISPLAY
import android.util.Log
import com.ideacarry.example27.extensions.colorFormat
import com.ideacarry.example27.extensions.fps
import com.ideacarry.utils.msToS

/**
 * Created by Raining on 2023/1/6
 *
 * 实现视频异步解码，返回yuv数据
 *
 * @param ctx 如果路径是assets path则需要context
 * @param path 视频路径
 */
class ASyncVideoDecoder(
    ctx: Context? = null,
    path: String,
    private val updateFrameListener: OnUpdateFrameListener
) {
    private val handlerThread = HandlerThread("async_video_decoder", THREAD_PRIORITY_URGENT_DISPLAY)
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
    private val cryptoInfo = CryptoInfo()
    private var mediaCodec: MediaCodec? = null
    private var firstPts = -1L // seek后需重置
    private var endPts = -1L
    private var maxPts = -1L

    private fun createMediaCodec(): MediaCodec? {
        val info = mediaInfo ?: return null
        return DecoderHelper.createMediaCodec(info.format)
    }

    init {
        start()
    }

    private fun start() {
        val info = mediaInfo
        if (info == null || info.index < 0) {
            updateFrameListener.onError()
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
                    updateFrameListener.onError()
                }

                override fun onOutputFormatChanged(codec: MediaCodec, format: MediaFormat) {
                }
            }, decoderHandler)
            it.configure(info.format, null, null, 0)
            it.start()
        }
    }

    private var debugStartTimeMs = -1L
    private fun handleOnOutputBufferAvailable(codec: MediaCodec, index: Int, info: MediaCodec.BufferInfo) {
        if ((info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
            Log.d(TAG, "decode complete time: ${(System.currentTimeMillis() - debugStartTimeMs).msToS()}")
            debugStartTimeMs = -1
        }
        // val image = codec.getOutputImage(index) ?: return // 获取image对象
        // getRowStride()：这个方法返回的值表示在同一平面内，从当前行的第一个像素/元素移动到下一行的第一个像素所需的字节跨度。
        // 换句话说，它指示内存中每一行的字节长度。
        // getRowStride() 受到图像数据的 padding（填充）和平台特定优化的影响，因此两行之间可能存在一定的间隔。
        // 实际上，getRowStride() 告诉我们如何在图像的行之间进行遍历，以便正确读取或处理 YUV 平面中的信息。
        //
        // getPixelStride()：这个方法返回的值表示当前平面中的每个像素/元素之间的字节跨度。
        // 在某些平台上，图像数据可能会以特定的方式排列，以便更有效地进行处理。
        // 对于 YUV420 图像格式，Y 平面的 getPixelStride() 通常为 1，因为 Y 平面包含平面图像的每个像素。
        // 对于 UV 平面的 getPixelStride() 通常为 1（U 和 V 互相间隔存储 或 分别存储在独立平面中）或 2（UV交错存储，如U1V1 U2V2 U3V3 …）。

        // val frame = codec.getOutputFrame(index) // 获取frame对象

        val outputBuffer = codec.getOutputBuffer(index) ?: return
        val bufferFormat = codec.getOutputFormat(index)
        if (debugStartTimeMs < 0) {
            debugStartTimeMs = System.currentTimeMillis()
            Log.d(
                TAG, "colorFormat=${bufferFormat.colorFormat()}, " +
                        "format=${codec.getOutputImage(index)?.format?.toString(16)}, " +
                        "duration=${mediaInfo?.durationUs}, " +
                        "fps=${mediaInfo?.format?.fps()}"
            )
        }
        updateFrameListener.onUpdate(
            FrameData(
                buffer = outputBuffer,
                info = info,
                format = bufferFormat,
                frameType = getFrameType(info.presentationTimeUs),
                durationUs = mediaInfo?.durationUs ?: 0,
            ) {
                decoderHandler.post {
                    codec.releaseOutputBuffer(index, false)
                }
            })
    }

    private fun writeSample(codec: MediaCodec, index: Int, isSecure: Boolean = false): Boolean {
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

        if (isSecure) {
            extractor.getSampleCryptoInfo(cryptoInfo)
            codec.queueSecureInputBuffer(index, 0, cryptoInfo, presentationTimeUs, flags)
        } else {
            codec.queueInputBuffer(index, 0, size, presentationTimeUs, flags)
        }
        return size > 0
    }

    fun release() {
        decoderHandler.post {
            mediaCodec?.let {
                it.stop()
                it.release()
            }
            mediaCodec = null
            extractor.release()
        }
        handlerThread.quitSafely()
    }

    private fun getFrameType(pts: Long): FrameType {
        return when (pts) {
            firstPts -> FrameType.FIRST
            endPts -> FrameType.END
            else -> FrameType.MIDDLE
        }
    }

    companion object {
        private const val TAG = "ASyncVideoDecoder"
    }
}