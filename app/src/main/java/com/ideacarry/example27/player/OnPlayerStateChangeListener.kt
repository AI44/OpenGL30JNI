package com.ideacarry.example27.player

/**
 * Created by Raining on 2023/6/12
 *
 * 播放状态改变接口
 */
interface OnPlayerStateChangeListener {
    fun onChange(state: PlayerState)
}