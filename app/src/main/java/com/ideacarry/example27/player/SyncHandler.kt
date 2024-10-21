package com.ideacarry.example27.player

import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import android.os.Process.THREAD_PRIORITY_AUDIO
import android.os.SystemClock
import android.util.Log
import com.ideacarry.example27.decoder.ASyncVideoDecoder
import com.ideacarry.example27.decoder.AsyncAudioDecoder
import com.ideacarry.example27.decoder.FrameData
import com.ideacarry.example27.decoder.OnUpdateFrameListener
import com.ideacarry.example27.decoder.currentDurationUs
import com.ideacarry.example27.decoder.isEndFrame
import com.ideacarry.example27.decoder.isFirstFrame
import com.ideacarry.example27.extensions.defaultFrameDurationMs
import com.ideacarry.utils.usToMs

/**
 * Created by Raining on 2023/07/18
 *
 * 视频同步处理程序
 */
class SyncHandler(
    private val context: Context,
    private val timestampProvider: ITimestampProvider,
    private val videoRender: OnUpdateFrameListener,
) {
    private val timerThread = HandlerThread("sync_timer", THREAD_PRIORITY_AUDIO)
    private val timerHandler by lazy {
        timerThread.start()
        Handler(timerThread.looper)
    }

    private var videoDecoder: ASyncVideoDecoder? = null
    private val renderQueue = mutableListOf<FrameData>()

    private var audioDecoder: AsyncAudioDecoder? = null

    @Volatile
    private var playerState = PlayerState.IDLE
    private var playerStateChangeListener: OnPlayerStateChangeListener? = null

    fun setData(path: String) {
        updatePlayerState(PlayerState.PREPARING)
        videoDecoder = ASyncVideoDecoder(context, path, object : OnUpdateFrameListener {
            override fun onUpdate(data: FrameData) {
                timerHandler.post {
                    renderQueue.add(data)
                    // Log.d(TAG, "renderQueue size=${renderQueue.size}")
                    requestRender()
                }
            }

            override fun onError() {
                timerHandler.post {
                    updatePlayerState(PlayerState.ERROR)
                }
            }
        })
        audioDecoder = AsyncAudioDecoder(context, path)
    }

    private var isRunning = false
    private var lastRenderTime = 0L
    private var firstFrameTime = -1L
    private fun requestRender() {
        if (playerState == PlayerState.PREPARING) {
            updatePlayerState(PlayerState.PREPARED)
            // fixme 暂时改为自动开始
            updatePlayerState(PlayerState.START)
        }
        if (playerState != PlayerState.START || isRunning) {
            return
        }

        val data = renderQueue.removeFirstOrNull() ?: return
        val isEndFrame = data.isEndFrame()
        if (isEndFrame) {
            Log.d(TAG, "------------------ lastFrame, curDurationUs=${data.currentDurationUs()}")
        }
        val delay = timestampProvider.calculateFrameDelayTimeUs(data.info.presentationTimeUs).usToMs()
        val runnable = Runnable {
            lastRenderTime = getCurrentSystemTime()

            val isFirstFrame = data.isFirstFrame()
            if (isFirstFrame || firstFrameTime < 0) {
                Log.d(TAG, "firstFrame pts=${data.info.presentationTimeUs}")
                firstFrameTime = getCurrentSystemTime()
            }
            if (isEndFrame) {
                val renderTime = getCurrentSystemTime() - firstFrameTime
                Log.d(
                    TAG, "lastFrame renderTimeMs=$renderTime, " +
                            "player durationMs=${renderTime + data.format.defaultFrameDurationMs()}"
                )
                firstFrameTime = -1
            }
            videoRender.onUpdate(data)
            isRunning = false
            val needSyncTime = delay < 0 // 是否需要同步一次时间
            if (needSyncTime) {
                // 处理循环播放的时间退回
                timestampProvider.seek(data.info.presentationTimeUs)
            }
            // 继续循环
            requestRender()
        }
        if (delay <= 0) {
            runnable.run()
            return
        }
        isRunning = true
        timerHandler.postDelayed(runnable, delay)
    }

    private fun getCurrentSystemTime() = SystemClock.elapsedRealtime()

    private fun updatePlayerState(state: PlayerState) {
        playerState = state
        playerStateChangeListener?.onChange(state)
        when (state) {
            PlayerState.PAUSE -> timestampProvider.pause()
            else -> {}
        }
    }

    fun getPlayerState() = playerState

    fun setPlayerStateChangeListener(lst: OnPlayerStateChangeListener) {
        playerStateChangeListener = lst
    }

    fun release() {
        timerThread.quitSafely()
        videoDecoder?.release()
        audioDecoder?.release()
        updatePlayerState(PlayerState.RELEASED)
    }

    companion object {
        private const val TAG = "SyncHandler"
    }
}