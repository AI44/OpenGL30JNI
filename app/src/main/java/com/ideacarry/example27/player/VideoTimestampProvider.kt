package com.ideacarry.example27.player

import android.os.SystemClock
import com.ideacarry.utils.msToUs

/**
 * Created by Raining on 2023/6/5
 *
 * 返回当前视频播放时间，用于视频解码和播放同步
 */
class VideoTimestampProvider : ITimestampProvider {

    private var lastDurationUs: Long = 0
    private var startTimeUs: Long = -1

    private fun getSystemCurrentTimeUs() = SystemClock.elapsedRealtime().msToUs()

    override fun getCurrentTimeUs(): Long {
        return lastDurationUs + if (startTimeUs >= 0) getSystemCurrentTimeUs() - startTimeUs else 0L
    }

    override fun calculateFrameDelayTimeUs(presentationTimeUs: Long): Long {
        val curTime = getCurrentTimeUs()
        start() // 如果当前没有启动计时器则启动
        return presentationTimeUs - curTime
    }

    override fun start() {
        if (startTimeUs < 0) {
            startTimeUs = getSystemCurrentTimeUs()
        }
    }

    override fun pause() {
        lastDurationUs = getCurrentTimeUs()
        startTimeUs = -1
    }

    override fun seek(timeUs: Long) {
        val correctTime = timeUs.coerceAtLeast(0)
        lastDurationUs = correctTime
        if (startTimeUs >= 0) {
            startTimeUs = getSystemCurrentTimeUs()
        }
    }

    override fun reset() {
        lastDurationUs = 0
        startTimeUs = -1
    }
}