package com.ideacarry.example27.decoder

import android.content.Context
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaCodecList
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import com.ideacarry.example27.extensions.YuvType
import com.ideacarry.example27.extensions.durationMs
import com.ideacarry.example27.extensions.durationUs
import com.ideacarry.example27.extensions.height
import com.ideacarry.example27.extensions.maxInputSize
import com.ideacarry.example27.extensions.rotation
import com.ideacarry.example27.extensions.width
import com.ideacarry.example27.extensions.yuvStandard
import java.io.IOException

/**
 * Created by Raining on 2023/1/5
 *
 * 解码辅助类
 */
object DecoderHelper {

    private val codecList by lazy {
        MediaCodecList(MediaCodecList.REGULAR_CODECS)
    }

    /**
     * assets头标识
     */
    const val ASSETS_HEADER = "file:///android_asset/"

    /**
     * 获取一个带assets头的路径
     */
    fun getPathForAssetsFile(assetsPath: String) = "$ASSETS_HEADER$assetsPath"

    /**
     * 获取assets路径
     */
    fun getAssetsPath(path: String) = path.replace(ASSETS_HEADER, "")

    /**
     * @param context 如果是assets文件则需要context
     */
    fun createMediaExtractor(path: String, context: Context? = null): MediaExtractor {
        val extractor = MediaExtractor()
        if (path.startsWith(ASSETS_HEADER)) {
            context?.assets?.openFd(getAssetsPath(path))?.let { afd ->
                extractor.setDataSource(afd.fileDescriptor, afd.startOffset, afd.declaredLength)
            }
        } else {
            extractor.setDataSource(path)
        }
        return extractor
    }

    private fun getMediaInfo(extractor: MediaExtractor, mimeHeader: String): MediaInfo? {
        repeat(extractor.trackCount) { index ->
            val format = extractor.getTrackFormat(index)
            val mime = format.getString(MediaFormat.KEY_MIME).orEmpty()
            if (mime.startsWith(mimeHeader)) {
                return MediaInfo(index, format)
            }
        }
        return null
    }

    fun getAudioMediaInfo(extractor: MediaExtractor): MediaInfo? {
        return getMediaInfo(extractor, "audio/")
    }

    fun getVideoMediaInfo(extractor: MediaExtractor): MediaInfo? {
        return getMediaInfo(extractor, "video/")
    }

    fun chooseDecoder(format: MediaFormat): String? {
        val codecList = MediaCodecList(MediaCodecList.REGULAR_CODECS)
        return codecList.findDecoderForFormat(format)
    }

    fun getVideoInfo(videoFilePath: String): VideoInfo {
        var w = 0
        var h = 0
        var rotation = 0
        var yuvType = YuvType.UNKNOWN
        var duration = 0
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(videoFilePath)
            w = retriever.width()
            h = retriever.height()
            rotation = retriever.rotation()
            yuvType = retriever.yuvStandard()
            duration = retriever.durationMs()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            retriever.release()
        }
        return VideoInfo(
            width = w,
            height = h,
            rotation = rotation,
            yuvType = yuvType,
            durationMs = duration,
        )
    }

    fun getYuvStandardFromVideo(videoFilePath: String): YuvType {
        val extractor = MediaExtractor()
        try {
            extractor.setDataSource(videoFilePath)
            val mediaInfo = getVideoMediaInfo(extractor) ?: return YuvType.UNKNOWN
            if (mediaInfo.index < 0) {
                // Not a video file
                return YuvType.UNKNOWN
            }
            val format = extractor.getTrackFormat(mediaInfo.index)
            return format.yuvStandard()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            extractor.release()
        }
        return YuvType.UNKNOWN
    }

    fun getEncoderYuvStandard(): List<YuvType> {
        val resultMap = mutableMapOf<YuvType, Boolean>()
        val codecList = MediaCodecList(MediaCodecList.REGULAR_CODECS)
        for (codecInfo in codecList.codecInfos) {
            if (codecInfo.isEncoder.not()) {
                continue
            }
            val capabilities = try {
                codecInfo.getCapabilitiesForType("video/avc")
            } catch (e: IllegalArgumentException) {
                // mimeType is not supported, try next codecInfo
                continue
            }
            val colorStandards = capabilities.colorFormats
            for (standard in colorStandards) {
                when (standard) {
                    MediaCodecInfo.CodecCapabilities.COLOR_TI_FormatYUV420PackedSemiPlanar ->
                        resultMap[YuvType.BT_2020] = true

                    MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar ->
                        resultMap[YuvType.BT_709] = true

                    MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible -> // BT.601 or BT.709
                        resultMap[YuvType.BT_601] = true
                }
            }
        }
        return resultMap.keys.toList()
    }

    fun createMediaCodec(format: MediaFormat): MediaCodec? {
        val mime = format.getString(MediaFormat.KEY_MIME) ?: return null
        try {
            return MediaCodec.createDecoderByType(mime)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        // 兜底方案，如果没有找到对应解码器则使用find
        try {
            return MediaCodec.createByCodecName(codecList.findDecoderForFormat(format))
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }
}

data class VideoInfo(
    val width: Int,
    val height: Int,
    val rotation: Int,
    val yuvType: YuvType,
    val durationMs: Int,
)

/**
 * 获取旋转后的宽高
 */
fun VideoInfo.correctWidth() = com.ideacarry.example27.extensions.correctWidth(width, height, rotation)

fun VideoInfo.correctHeight() = com.ideacarry.example27.extensions.correctHeight(width, height, rotation)

data class MediaInfo(
    val index: Int,
    val format: MediaFormat,
) {
    val maxInputSize by lazy {
        format.maxInputSize()
    }

    /**
     * 原始宽高
     */
    val width by lazy {
        format.width()
    }

    /**
     * 原始宽高
     */
    val height by lazy {
        format.height()
    }

    val durationUs by lazy {
        format.durationUs()
    }
}