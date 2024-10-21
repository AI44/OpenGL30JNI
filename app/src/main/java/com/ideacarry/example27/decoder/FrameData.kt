package com.ideacarry.example27.decoder

import android.media.MediaCodec
import android.media.MediaFormat
import com.ideacarry.example27.extensions.defaultFrameDurationMs
import com.ideacarry.utils.msToUs
import java.nio.ByteBuffer

/**
 * Created by Raining on 2023/07/18
 *
 * 帧数据
 *
 * @param buffer yuv数据buffer
 * @param info 数据描述信息
 * @param format 数据格式
 * @param renderFinish 渲染完成回调方法
 */
data class FrameData(
    val buffer: ByteBuffer,
    val info: MediaCodec.BufferInfo,
    val format: MediaFormat,
    val frameType: FrameType,
    val durationUs: Long,
    val renderFinish: (Boolean) -> Unit,
)

fun FrameData.currentDurationUs() = info.presentationTimeUs + format.defaultFrameDurationMs().msToUs()

fun FrameData.isFirstFrame() = frameType == FrameType.FIRST

fun FrameData.isEndFrame() = frameType == FrameType.END

enum class FrameType {
    FIRST, // 首帧
    END, // 末帧
    MIDDLE // 中间的帧
}