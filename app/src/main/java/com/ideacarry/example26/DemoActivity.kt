package com.ideacarry.example26

import android.media.MediaCodecList
import android.media.MediaExtractor
import android.media.MediaFormat
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer

/**
 * Created by Raining on 2023/1/4
 *
 * #I# MediaCodec解码data
 */
class DemoActivity : AppCompatActivity() {

    private val codecList by lazy {
        MediaCodecList(MediaCodecList.REGULAR_CODECS)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                displayDecoders()
                decodeVideo()
            }
        }
    }

    private fun decodeVideo() {
        val afd = assets.openFd(MP4_PATH)
        val extractor = MediaExtractor()
        extractor.setDataSource(afd.fileDescriptor, afd.startOffset, afd.declaredLength)
        val count = extractor.trackCount
        Log.d(TAG, "trackCount = $count")
        var videoIndex = -1
        var videoFormat: MediaFormat? = null
        var audioIndex = -1
        var audioFormat: MediaFormat? = null
        repeat(count) { index ->
            val format = extractor.getTrackFormat(index)
            val mime = format.getString(MediaFormat.KEY_MIME).orEmpty()
            if (mime.startsWith("video/")) {
                videoIndex = index
                videoFormat = format
                Log.d(TAG, "videoIndex = $index, videoFormat = $format")
            } else if (mime.startsWith("audio/")) {
                audioIndex = index
                audioFormat = format
                Log.d(TAG, "audioIndex = $index, audioFormat = $format")
            }
        }
        if (videoIndex > -1) {
            //获取视频信息
            val width = videoFormat?.getInteger(MediaFormat.KEY_WIDTH)
            val height = videoFormat?.getInteger(MediaFormat.KEY_HEIGHT)
            val maxInputSize = videoFormat?.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE) ?: 0
            val duration = videoFormat?.getLong(MediaFormat.KEY_DURATION)
            Log.d(TAG, "video w = $width, h = $height, maxInputSize = $maxInputSize, duration = $duration")

            extractor.selectTrack(videoIndex) //选择轨道
            var inputDone = false
            val buffer = ByteBuffer.allocateDirect(maxInputSize)
            while (inputDone.not()) {
                val sampleSize = extractor.readSampleData(buffer, 0)
                val sampleTrackIndex = extractor.sampleTrackIndex
                val sampleTime = extractor.sampleTime
                Log.d(
                    TAG,
                    "video sampleTrackIndex = $sampleTrackIndex, sampleTime = $sampleTime, sampleSize = $sampleSize"
                )
                if (sampleSize < 0) {
                    inputDone = true
                } else {
                    extractor.advance()
                }
            }
        }
        if (audioIndex > -1) {
            val bitRate = audioFormat?.getInteger(MediaFormat.KEY_BIT_RATE)
            val sampleRate = audioFormat?.getInteger(MediaFormat.KEY_SAMPLE_RATE)
            val audioChannel = audioFormat?.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
            val duration = audioFormat?.getLong(MediaFormat.KEY_DURATION)
            val maxInputSize = audioFormat?.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE) ?: 0
            Log.d(
                TAG,
                "audio bitRate = $bitRate, sampleRate = $sampleRate, audioChannel = $audioChannel, duration = $duration"
            )

            extractor.selectTrack(audioIndex)
            var inputDone = false
            val buffer = ByteBuffer.allocateDirect(maxInputSize)
            while (inputDone.not()) {
                val sampleSize = extractor.readSampleData(buffer, 0)
                val sampleTrackIndex = extractor.sampleTrackIndex
                val sampleTime = extractor.sampleTime
                Log.d(
                    TAG,
                    "audio sampleTrackIndex = $sampleTrackIndex, sampleTime = $sampleTime, sampleSize = $sampleSize"
                )
                if (sampleSize < 0) {
                    inputDone = true
                } else {
                    extractor.advance()
                }
            }
        }
        extractor.release()
    }

    private fun displayDecoders() {
        val codecs = codecList.codecInfos
        codecs.forEach { codec ->
            if (codec.isEncoder) return@forEach
            Log.d(TAG, "displayDecoders: " + codec.name)
        }
    }

    companion object {
        private const val MP4_PATH = "example25/test.mp4"
        private const val TAG = "example26"
    }
}