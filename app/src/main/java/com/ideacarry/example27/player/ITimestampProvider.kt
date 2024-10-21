package com.ideacarry.example27.player

/**
 * Created by Raining on 2023/5/12
 *
 * 时间戳接口
 */
interface ITimestampProvider {

    fun getCurrentTimeUs(): Long

    /**
     * 用于计算当前帧的播放时间（简化曲线变速的实现），获取每帧的播放偏移时间，
     * 例如，当前时间是1,000,000(us)，presentationTimeUs是5,000,000(us)，那么返回4,000,000(us)。
     */
    fun calculateFrameDelayTimeUs(presentationTimeUs: Long): Long

    fun start()

    fun pause()

    fun seek(timeUs: Long)

    fun reset()
}