package com.ideacarry.example27.decoder

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.os.Handler
import android.os.HandlerThread
import android.os.Process.THREAD_PRIORITY_AUDIO
import android.util.Log
import com.ideacarry.example27.extensions.audioChannelMask
import com.ideacarry.example27.extensions.audioEncoding
import com.ideacarry.example27.extensions.durationUs
import com.ideacarry.example27.extensions.sampleRate
import com.ideacarry.utils.msToS


/**
 * Created by Raining on 2023/5/12
 *
 * 音频解码
 *
 * @param ctx 如果路径是assets path则需要context
 * @param path 视频路径
 */
class AsyncAudioDecoder(ctx: Context, path: String) {

    private val handlerThread = HandlerThread("async_audio_decoder", THREAD_PRIORITY_AUDIO)
    private val decoderHandler by lazy {
        handlerThread.start()
        Handler(handlerThread.looper)
    }
    private val context = ctx.applicationContext
    private var isLoopPlay = true
    private val extractor by lazy {
        DecoderHelper.createMediaExtractor(path, context)
    }
    private val mediaInfo by lazy {
        DecoderHelper.getAudioMediaInfo(extractor)
    }
    private var mediaCodec: MediaCodec? = null
    private var audioTrack: AudioTrack? = null

    private val audioSessionId by lazy {
        context.getSystemService(AudioManager::class.java).generateAudioSessionId()
    }

    private fun createMediaCodec(): MediaCodec? {
        val info = mediaInfo ?: return null
        return DecoderHelper.createMediaCodec(info.format)
    }

    /**
     * mp3的帧包采样点个数是固定的吗？
     *
     * MP3帧包中的采样点个数取决于MPEG音频编码的版本和层。通常情况下，对于一个特定的版本和层，采样点个数是固定的。
     * 以下是不同版本和层的MP3帧包对应的采样点个数：
     *
     * MPEG-1（版本1）
     * Layer 1：384个采样点
     * Layer 2：1152个采样点
     * Layer 3：1152个采样点
     *
     * MPEG-2（版本2）和MPEG-2.5（版本2.5）
     * Layer 1：384个采样点
     * Layer 2：1152个采样点
     * Layer 3：576个采样点
     *
     * 值得注意的是，MP3格式通常指的是MPEG-1 Layer 3，即版本1的层3编码，对应的采样点个数为1152个。
     * 在实际应用中，这是最常见的MP3格式。所以在这种情况下，MP3帧包中的采样点个数是固定的。
     */
    private fun createAudioTrack(): AudioTrack? {
        val info = mediaInfo ?: return null
        val sampleRate = info.format.sampleRate()
        val channelMask = info.format.audioChannelMask()
        val encoding = info.format.audioEncoding()
        val minBufferSize = AudioTrack.getMinBufferSize(sampleRate, channelMask, encoding)
        Log.d(
            TAG,
            "sampleRate=$sampleRate, " +
                    "channelMask=$channelMask, " +
                    "encoding=$encoding, " +
                    "minBufferSize=$minBufferSize, " +
                    "duration=${info.format.durationUs()}"
        )
        val audioAttributes = AudioAttributes.Builder()
            .setLegacyStreamType(AudioManager.STREAM_MUSIC)
            .build()
        val audioFormat = AudioFormat.Builder()
            .setSampleRate(sampleRate)
            .setChannelMask(channelMask)
            .setEncoding(encoding)
            .build()
        return AudioTrack(audioAttributes, audioFormat, minBufferSize, AudioTrack.MODE_STREAM, audioSessionId)
    }

    init {
        start()
    }

    private fun start() {
        val info = mediaInfo
        if (info == null || info.index < 0) {
            return
        }
        if (audioTrack == null) {
            audioTrack = createAudioTrack()
        }
        audioTrack?.play()

        extractor.selectTrack(info.index)
        if (mediaCodec == null) {
            mediaCodec = createMediaCodec()
        }
        mediaCodec?.let {
            it.setCallback(object : MediaCodec.Callback() {
                override fun onInputBufferAvailable(codec: MediaCodec, index: Int) {
                    val result = writeSample(codec, index)
                    if (result) {
                        extractor.advance()
                    }
                }

                override fun onOutputBufferAvailable(codec: MediaCodec, index: Int, info: MediaCodec.BufferInfo) {
                    handleOnOutputBufferAvailable(codec, index, info)
                }

                override fun onError(codec: MediaCodec, e: MediaCodec.CodecException) {
                }

                override fun onOutputFormatChanged(codec: MediaCodec, format: MediaFormat) {
                }
            }, decoderHandler)
            it.configure(info.format, null, null, 0)
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
                break
            }
            // 读取结束
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

    private var debugStartTimeMs = -1L
    private fun handleOnOutputBufferAvailable(codec: MediaCodec, index: Int, info: MediaCodec.BufferInfo) {
        if ((info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
            Log.d(TAG, "decode complete time: ${(System.currentTimeMillis() - debugStartTimeMs).msToS()}")
            debugStartTimeMs = -1
        }
        val outputBuffer = codec.getOutputBuffer(index) ?: return
        if (debugStartTimeMs < 0) {
            debugStartTimeMs = System.currentTimeMillis()
        }
        audioTrack?.write(outputBuffer, info.size, AudioTrack.WRITE_BLOCKING)
        codec.releaseOutputBuffer(index, false)
    }

    fun release() {
        decoderHandler.post {
            mediaCodec?.let {
                it.stop()
                it.release()
            }
            mediaCodec = null
            extractor.release()
            audioTrack?.let {
                it.stop()
                it.release()
            }
            audioTrack = null
        }
        handlerThread.quitSafely()
    }

    companion object {
        private const val TAG = "AsyncAudioDecoder"
    }
}