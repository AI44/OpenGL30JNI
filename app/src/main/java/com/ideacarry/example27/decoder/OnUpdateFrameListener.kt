package com.ideacarry.example27.decoder

/**
 * Created by Raining on 2023/07/19
 *
 * 更新帧数据监听
 */
interface OnUpdateFrameListener {
    fun onUpdate(data: FrameData)
    fun onError() {}
}