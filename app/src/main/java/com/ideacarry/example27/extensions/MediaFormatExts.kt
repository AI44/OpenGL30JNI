package com.ideacarry.example27.extensions

import android.media.AudioFormat
import android.media.MediaCodecInfo
import android.media.MediaFormat

/**
 * Created by Raining on 2023/5/24
 *
 * [MediaFormat]扩展方法
 */

fun MediaFormat.getNonnullInteger(key: String): Int {
    return try {
        if (containsKey(key)) {
            getInteger(key)
        } else {
            0
        }
    } catch (_: Throwable) {
        0
    }
}

fun MediaFormat.getNonnullLong(key: String): Long {
    return try {
        if (containsKey(key)) {
            getLong(key)
        } else {
            0L
        }
    } catch (_: Throwable) {
        0
    }
}

fun MediaFormat.width() = getNonnullInteger(MediaFormat.KEY_WIDTH)

fun MediaFormat.height() = getNonnullInteger(MediaFormat.KEY_HEIGHT)

fun MediaFormat.maxInputSize() = getNonnullInteger(MediaFormat.KEY_MAX_INPUT_SIZE)

fun MediaFormat.durationUs() = getNonnullLong(MediaFormat.KEY_DURATION)

fun MediaFormat.rotation() = getNonnullInteger(MediaFormat.KEY_ROTATION)

fun MediaFormat.fps() = getNonnullInteger(MediaFormat.KEY_FRAME_RATE)

fun MediaFormat.defaultFrameDurationMs() = 1000L / fps().coerceAtLeast(1)

fun MediaFormat.correctWidth() = correctWidth(width(), height(), rotation())

fun MediaFormat.correctHeight() = correctHeight(width(), height(), rotation())

fun MediaFormat.yuvStandard(): YuvType {
    if (containsKey(MediaFormat.KEY_COLOR_STANDARD).not()) {
        return YuvType.UNKNOWN
    }
    return when (getInteger(MediaFormat.KEY_COLOR_STANDARD)) {
        MediaFormat.COLOR_STANDARD_BT601_NTSC,
        MediaFormat.COLOR_STANDARD_BT601_PAL -> YuvType.BT_601

        MediaFormat.COLOR_STANDARD_BT709 -> YuvType.BT_709
        MediaFormat.COLOR_STANDARD_BT2020 -> YuvType.BT_2020
        else -> YuvType.UNKNOWN
    }
}

fun MediaFormat.sampleRate() = getNonnullInteger(MediaFormat.KEY_SAMPLE_RATE)

fun MediaFormat.audioChannelMask() = when (getNonnullInteger(MediaFormat.KEY_CHANNEL_COUNT)) {
    1 -> AudioFormat.CHANNEL_OUT_MONO
    2 -> AudioFormat.CHANNEL_OUT_STEREO
    4 -> AudioFormat.CHANNEL_OUT_QUAD
    6 -> AudioFormat.CHANNEL_OUT_5POINT1
    8 -> AudioFormat.CHANNEL_OUT_7POINT1_SURROUND
    else -> AudioFormat.CHANNEL_OUT_MONO
}

fun MediaFormat.audioEncoding(): Int {
    if (containsKey(MediaFormat.KEY_PCM_ENCODING)) {
        return getInteger(MediaFormat.KEY_PCM_ENCODING)
    }
    return AudioFormat.ENCODING_PCM_16BIT // 默认编码格式为PCM 16位
}

/**
 * [MediaCodecInfo.CodecCapabilities]
 */
fun MediaFormat.colorFormat() = getInteger(MediaFormat.KEY_COLOR_FORMAT)

/**
 * 计算旋转后的宽
 */
fun correctWidth(w: Int, h: Int, rotate: Int): Int {
    if (rotate % 180 != 0) {
        return h
    }
    return w
}

/**
 * 计算旋转后的高
 */
fun correctHeight(w: Int, h: Int, rotate: Int): Int {
    if (rotate % 180 != 0) {
        return w
    }
    return h
}

enum class YuvType {
    BT_601,
    BT_709,
    BT_2020,
    UNKNOWN,
}
