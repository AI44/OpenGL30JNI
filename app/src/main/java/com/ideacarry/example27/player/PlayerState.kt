package com.ideacarry.example27.player

/**
 * Created by Raining on 2023/1/6
 *
 * 播放状态
 */
enum class PlayerState {
    IDLE,
    PREPARING,
    PREPARED,
    START,
    PAUSE,
    ERROR,
    RELEASED,
}