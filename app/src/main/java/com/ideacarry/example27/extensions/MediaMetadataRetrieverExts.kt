package com.ideacarry.example27.extensions

import android.media.MediaFormat
import android.media.MediaMetadataRetriever

/**
 * Created by Raining on 2023/5/24
 *
 * [MediaMetadataRetriever]扩展方法
 */

fun parseInt(str: String?): Int {
    str ?: return 0
    return try {
        Integer.parseInt(str)
    } catch (e: NumberFormatException) {
        0
    }
}

fun MediaMetadataRetriever.width() = parseInt(extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH))

fun MediaMetadataRetriever.height() = parseInt(extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT))

fun MediaMetadataRetriever.rotation() = parseInt(extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION))

fun MediaMetadataRetriever.durationMs() = parseInt(extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION))

fun MediaMetadataRetriever.colorStandard() = extractMetadata(MediaMetadataRetriever.METADATA_KEY_COLOR_STANDARD)

fun MediaMetadataRetriever.correctWidth() = correctWidth(width(), height(), rotation())

fun MediaMetadataRetriever.correctHeight() = correctHeight(width(), height(), rotation())

fun MediaMetadataRetriever.yuvStandard(): YuvType {
    return when (colorStandard()) {
        MediaFormat.COLOR_STANDARD_BT601_PAL.toString() -> YuvType.BT_601
        MediaFormat.COLOR_STANDARD_BT601_NTSC.toString() -> YuvType.BT_601
        MediaFormat.COLOR_STANDARD_BT709.toString() -> YuvType.BT_709
        MediaFormat.COLOR_STANDARD_BT2020.toString() -> YuvType.BT_2020
        else -> YuvType.UNKNOWN
    }
}